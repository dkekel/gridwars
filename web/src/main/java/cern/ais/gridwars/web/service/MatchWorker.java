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


@Component
@Scope("prototype")
public class MatchWorker implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final MatchService matchService;
    private int workerNum;

    @Autowired
    public MatchWorker(MatchService matchService) {
        this.matchService = Objects.requireNonNull(matchService);
    }

    public void setWorkerNum(int workerNum) {
        this.workerNum = workerNum;
    }

    @Override
    public synchronized void run() {
        LOG.debug("Worker [{}]: started", workerNum);
        enterMainEventLoop();
        LOG.debug("Worker [{}]: finished", workerNum);
    }

    private void enterMainEventLoop(){
        while (true) {
            Optional<Match> match = takeNextPendingMatch();

            if (match.isPresent()) {
                executeMatch(match.get());
            } else {
                LOG.debug("Worker [{}]: No more pending matches available, going to sleep", workerNum);
                try {
                    wait();
                } catch (InterruptedException ignored) {
                    // Received quit signal
                }
                LOG.debug("Worker [{}]: woken up and will check for pending matches", workerNum);
            }
        }
    }

    private Optional<Match> takeNextPendingMatch() {
        LOG.debug("Worker [{}]: checking for a pending match", workerNum);
        return matchService.takeNextPendingMatch();
    }

    private void executeMatch(Match match) {
        LOG.debug("Worker [{}]: executing pending match: {}", workerNum, match.getId());
        match.setEnded(Instant.now());
        match.setStatus(Match.Status.FINISHED);
        match.setOutcome(Match.Outcome.WIN);
        matchService.updateMatch(match);
    }
}
