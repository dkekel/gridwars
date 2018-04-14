package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.worker.MatchWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class MatchWorkerService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final List<MatchWorker> matchWorkers = new LinkedList<>();
    private final TaskExecutor taskExecutor;
    private final MatchService matchService;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public MatchWorkerService(TaskExecutor taskExecutor, MatchService matchService,
                              GridWarsProperties gridWarsProperties) {
        this.taskExecutor = Objects.requireNonNull(taskExecutor);
        this.matchService = Objects.requireNonNull(matchService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public void startAllMatchWorkers() {
        LOG.debug("Starting all workers that are not running ...");

        matchWorkers.forEach(worker -> {
            if (MatchWorker.Status.STOPPED == worker.getStatus()) {
                taskExecutor.execute(worker);
                LOG.debug("... started worker {}", worker.getWorkerNumber());
            }
        });
    }

    public void stopAllMatchWorkers() {
        matchWorkers.forEach(MatchWorker::stop);
        LOG.debug("Sent STOP signal to all workers");
    }

    public void wakeUpAllMatchWorkers() {
        matchWorkers.forEach(MatchWorker::wakeUp);
        LOG.debug("Woke up all workers");
    }

    public List<MatchWorkerStatus> getMatchWorkerStatuses() {
        return matchWorkers.stream().map(MatchWorkerStatus::fromMatchWorker).collect(Collectors.toList());
    }

    @PostConstruct
    protected void init() {
        createMatchWorkers();
        startAllMatchWorkers();
        LOG.debug("MatchExecutionService initialised, worker count: {}",  matchWorkers.size());
    }

    private void createMatchWorkers() {
        for (int i = 0; i < gridWarsProperties.getMatches().getWorkerCount(); i++) {
            createMatchWorker(i + 1);
        }
    }

    private void createMatchWorker(int workerNumber) {
        MatchWorker matchWorker = new MatchWorker(matchService, workerNumber, gridWarsProperties);
        matchWorkers.add(matchWorker);
    }

    @PreDestroy
    protected void destroy() {
        stopAllMatchWorkers();
        disposeAllMatchWorkers();
        LOG.debug("MatchExecutionService destroyed");
    }

    private void disposeAllMatchWorkers() {
        matchWorkers.clear();
    }

    public static final class MatchWorkerStatus {
        public final int number;
        public final MatchWorker.Status status;

        private static MatchWorkerStatus fromMatchWorker(MatchWorker matchWorker) {
            return new MatchWorkerStatus(matchWorker.getWorkerNumber(), matchWorker.getStatus());
        }

        private MatchWorkerStatus(int number, MatchWorker.Status status) {
            this.number = number;
            this.status = status;
        }
    }
}
