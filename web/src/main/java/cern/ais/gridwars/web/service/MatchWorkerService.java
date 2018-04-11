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

    public void wakeUpAllMatchWorkers() {
        matchWorkers.forEach(MatchWorker::wakeUp);
    }

    @PostConstruct
    protected void init() {
        createMatchWorkers();
        startAllMatchWorkers();

        LOG.debug("MatchExecutionService initialised, worker count: {}",
            gridWarsProperties.getMatches().getWorkerCount());
    }

    private void createMatchWorkers() {
        int workerCount = gridWarsProperties.getMatches().getWorkerCount();

        for (int i = 0; i < workerCount; i++) {
            createMatchWorker(i + 1);
        }
    }

    private void createMatchWorker(int workerNumber) {
        MatchWorker matchWorker = new MatchWorker(matchService, workerNumber, gridWarsProperties);
        matchWorkers.add(matchWorker);
    }

    private void startAllMatchWorkers() {
        matchWorkers.forEach(taskExecutor::execute);
    }

    @PreDestroy
    protected void destroy() {
        shutdownAllMatchWorkers();
        LOG.debug("MatchExecutionService destroyed");
    }

    private void shutdownAllMatchWorkers() {
        matchWorkers.forEach(MatchWorker::shutdown);
        matchWorkers.clear();
    }
}
