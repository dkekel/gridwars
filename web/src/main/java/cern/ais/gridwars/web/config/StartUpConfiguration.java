package cern.ais.gridwars.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * This configuration does nothing except printing some useful information during the start of the app.
 */
@Configuration
public class StartUpConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void logSystemInfo() {
        logMemoryUsage();
    }

    private void logMemoryUsage() {
        final Runtime runtime = Runtime.getRuntime();
        final long MEGA_BYTE_FACTOR = 1024 * 1024;
        final long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE_FACTOR;
        final long maxMemoryMb = runtime.maxMemory() / MEGA_BYTE_FACTOR;
        LOG.info("Memory usage [MB]: " + usedMemoryMb + " / " + maxMemoryMb);
    }
}
