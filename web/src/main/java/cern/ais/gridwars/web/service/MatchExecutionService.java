package cern.ais.gridwars.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;


@Service
public class MatchExecutionService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final TaskExecutor taskExecutor;
    private final ApplicationContext applicationContext;
    private final Integer workerCount;

    @Autowired
    public MatchExecutionService(TaskExecutor taskExecutor, ApplicationContext applicationContext,
        @Value("${gridwars.matches.workerCount}") Integer workerCount) {
        this.taskExecutor = Objects.requireNonNull(taskExecutor);
        this.applicationContext = Objects.requireNonNull(applicationContext);
        this.workerCount = Objects.requireNonNull(workerCount);
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < workerCount; i++) {
            MatchWorker matchWorker = applicationContext.getBean(MatchWorker.class);
            matchWorker.setWorkerNum(i + 1);
            taskExecutor.execute(matchWorker);
        }

        LOG.debug("MatchExecutionService initialised, worker count: {}", workerCount);
    }

    @PreDestroy
    public void destroy() {
        LOG.debug("MatchExecutionService destroyed");
    }
}
