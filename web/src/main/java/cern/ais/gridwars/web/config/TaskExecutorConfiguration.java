package cern.ais.gridwars.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class TaskExecutorConfiguration {

    @Autowired
    private GridWarsProperties gridWarsProperties;

    @Bean
    public TaskExecutor taskExecutor() {
        int workerCount = gridWarsProperties.getMatches().getWorkerCount();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(workerCount);
        executor.setMaxPoolSize(workerCount);
        executor.setThreadNamePrefix("match_worker_");
        executor.initialize();
        return executor;
    }
}
