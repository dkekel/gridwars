package cern.ais.gridwars.web.worker;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MatchWorker implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final Lock lock = new ReentrantLock();
    private final Condition newMatchesAvailableCondition = lock.newCondition();
    private final MatchExecutor matchExecutor;
    private final MatchService matchService;
    private final int workerNumber;
    private volatile boolean running = false;

    public MatchWorker(MatchService matchService, int workerNumber, GridWarsProperties gridWarsProperties) {
        this.matchService = Objects.requireNonNull(matchService);
        this.workerNumber = workerNumber;
        this.matchExecutor = new MatchExecutor(gridWarsProperties);
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

            if (match.isPresent()) {
                if (running) {
                    executeMatch(match.get());
                }
                // TODO if the match was taken but the worker was shutdown, the match must be returned into the pending queue
            } else {
                if (running) {
                    logDebug("no more pending matches available, sleeping until matches are available");
                    newMatchesAvailableCondition.await();
                    logDebug("woke up and will check for pending matches");
                }
            }
        }
    }

    private Optional<Match> takeNextPendingMatch() {
        try {
            logDebug("checking for a pending match");
            return matchService.takeNextPendingMatch();
        } catch (Exception e) {
            LOG.error("Checking for next pending match failed: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void executeMatch(Match match) {
        try {
            logInfo("start executing pending match: {} ...", match.getId());

            Match executedMatch = matchExecutor.executeMatch(match);
            matchService.updateMatch(executedMatch);

            logInfo("... finished executing pending match in {} ms: {}", executedMatch.getDuration(),
                executedMatch.getId());
        } catch (Exception e) {
            LOG.error("Execution of match {} failed: {}", match.getId(), e.getMessage(), e);
        }
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
