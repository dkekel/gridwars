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

    public enum Status { IDLE, RUNNING, STOPPED }

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
        this.matchExecutor = new MatchExecutor(Objects.requireNonNull(gridWarsProperties));
    }

    public Status getStatus() {
        if (!running) {
            return Status.STOPPED;
        } else if (lock.tryLock()) {
            try {
                return Status.IDLE;
            } finally {
                lock.unlock();
            }
        } else {
            return Status.RUNNING;
        }
    }

    public int getWorkerNumber() {
        return workerNumber;
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
                } else {
                    // In case the worker took a pending match but was shut down before it could be executed, it's
                    // important to return the match back to the pending queue. Otherwise it would still be marked
                    // as "running" but never executed, because no other worker would ever take the match.
                    logDebug("worker obtained a pending match but was shut down before match could be " +
                        "executed, return match back to pending queue");
                    matchService.returnMatchToPendingQueue(match.get());
                }
            } else {
                if (running) {
                    logDebug("no more pending matches available, going to sleep ...");
                    newMatchesAvailableCondition.await();
                    logDebug("... woke up and will check for pending matches or shutdown");
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

            logInfo("... finished executing pending match in {} ms: {}", executedMatch.getDurationMillis(),
                executedMatch.getId());
        } catch (Exception e) {
            LOG.error("Execution of match {} failed: {}", match.getId(), e.getMessage(), e);

            // TODO populate and persist failed match...
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

    public void stop() {
        if (running) {
            running = false;
            wakeUp();
        }
    }

    private void logDebug(String message, Object... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Worker [" + workerNumber + "]: " + message, params);
        }
    }

    private void logInfo(String message, Object... params) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Worker [" + workerNumber + "]: " + message,params);
        }
    }
}
