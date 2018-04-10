package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Component
@Scope("prototype")
public class MatchWorker implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final Lock lock = new ReentrantLock();
    private final Condition newMatchesAvailableCondition = lock.newCondition();
    private volatile boolean running = false;
    private final MatchService matchService;
    private int workerNumber;

    @Autowired
    public MatchWorker(MatchService matchService) {
        this.matchService = Objects.requireNonNull(matchService);
    }

    public void setWorkerNumber(int workerNumber) {
        this.workerNumber = workerNumber;
    }

    @Override
    public void run() {
        if (running) {
            return;
        }

        lock.lock();
        try {
            running = true;
            logInfo("started");
            runMainEventLoop();
            logInfo("was shut down");
        } catch (InterruptedException ignored) {
            logInfo("received terminate signal");
        } finally {
            lock.unlock();
        }

        running = false;
        logInfo("terminated");
    }

    private void runMainEventLoop() throws InterruptedException {
        while (running) {
            Optional<Match> match = takeNextPendingMatch();

            // TODO if the match was taken but the worker was shutdown, the match must be returned into the pending queue

            if (running) {
                if (match.isPresent()) {
                    executeMatch(match.get());
                } else {
                    logDebug("no more pending matches available, sleeping until matches are available");
                    newMatchesAvailableCondition.await();
                    logDebug("woke up and will check for pending matches");
                }
            }
        }
    }

    private Optional<Match> takeNextPendingMatch() {
        logDebug("checking for a pending match");
        return matchService.takeNextPendingMatch();
    }

    private void executeMatch(Match match) {
        logInfo("executing pending match: {}", match.getId());

        // TODO implement remote process execution...
        match.setEnded(Instant.now());
        match.setStatus(Match.Status.FINISHED);
        match.setOutcome(Match.Outcome.WIN);
        matchService.updateMatch(match);
    }

    public void wakeUp() {
        // If the lock can't be acquired, the worker is not sleeping and therefore doesn't need to be woken up.
        if (lock.tryLock()) {
            try {
                newMatchesAvailableCondition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    public void shutdown() {
        if (running) {
            running = false;
            wakeUp();
        }
    }

    private void logDebug(String message, Object... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Worker [{}]: " + message, workerNumber, params);
        }
    }

    private void logInfo(String message, Object... params) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Worker [{}]: " + message, workerNumber, params);
        }
    }
}
