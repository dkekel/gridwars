package cern.ais.gridwars.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;

// TODO dump all config values to the logs in PostConstruct to see what options apply

@Configuration
@ConfigurationProperties("gridwars")
public class GridWarsProperties {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final Directories directories = new Directories();
    private final Matches matches = new Matches();
    private final Mail mail = new Mail();

    @PostConstruct
    public void init() {
        directories.initDirs();
    }

    public Directories getDirectories() {
        return directories;
    }

    public Matches getMatches() {
        return matches;
    }

    public Mail getMail() {
        return mail;
    }

    public class Directories {
        private String baseWorkDir;
        private String botJarDir;
        private String matchesDir;
        private String runtimeDir;

        protected void initDirs() {
            botJarDir = Paths.get(baseWorkDir, "bots").toString();
            matchesDir = Paths.get(baseWorkDir, "matches").toString();
            runtimeDir = Paths.get(baseWorkDir, "runtime").toString();

            if (LOG.isInfoEnabled()) {
                LOG.info("Base work dir: {}", baseWorkDir);
                LOG.info("Bot jar dir: {}", botJarDir);
                LOG.info("Matches dir: {}", matchesDir);
                LOG.info("Runtime dir: {}", runtimeDir);
            }
        }

        public String getBaseWorkDir() {
            return baseWorkDir;
        }

        public Directories setBaseWorkDir(String baseWorkDir) {
            this.baseWorkDir = baseWorkDir;
            return this;
        }

        public String getBotJarDir() {
            return botJarDir;
        }

        public Directories setBotJarDir(String botJarDir) {
            this.botJarDir = botJarDir;
            return this;
        }

        public String getMatchesDir() {
            return matchesDir;
        }

        public Directories setMatchesDir(String matchesDir) {
            this.matchesDir = matchesDir;
            return this;
        }

        public String getRuntimeDir() {
            return runtimeDir;
        }

        public Directories setRuntimeDir(String runtimeDir) {
            this.runtimeDir = runtimeDir;
            return this;
        }
    }

    public class Matches {
        private Integer matchCountPerOpponent;
        private Integer workerCount;
        private String workerHeapSize;
        private Integer executionTimeoutSeconds;

        public Integer getMatchCountPerOpponent() {
            return matchCountPerOpponent;
        }

        public Matches setMatchCountPerOpponent(Integer matchCountPerOpponent) {
            this.matchCountPerOpponent = matchCountPerOpponent;
            return this;
        }

        public Integer getWorkerCount() {
            return workerCount;
        }

        public Matches setWorkerCount(Integer workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public String getWorkerHeapSize() {
            return workerHeapSize;
        }

        public Matches setWorkerHeapSize(String workerHeapSize) {
            this.workerHeapSize = workerHeapSize;
            return this;
        }

        public Integer getExecutionTimeoutSeconds() {
            return executionTimeoutSeconds;
        }

        public Matches setExecutionTimeoutSeconds(Integer executionTimeoutSeconds) {
            this.executionTimeoutSeconds = executionTimeoutSeconds;
            return this;
        }
    }

    public class Mail {
        private Boolean enabled;
        private String from;
        private String bccRecipient;
        private String toRecipientOverride;

        public Boolean getEnabled() {
            return enabled;
        }

        public Mail setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getFrom() {
            return from;
        }

        public Mail setFrom(String from) {
            this.from = from;
            return this;
        }

        public String getBccRecipient() {
            return bccRecipient;
        }

        public Mail setBccRecipient(String bccRecipient) {
            this.bccRecipient = bccRecipient;
            return this;
        }

        public String getToRecipientOverride() {
            return toRecipientOverride;
        }

        public Mail setToRecipientOverride(String toRecipientOverride) {
            this.toRecipientOverride = toRecipientOverride;
            return this;
        }
    }
}
