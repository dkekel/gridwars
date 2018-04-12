package cern.ais.gridwars.web.config;

import cern.ais.gridwars.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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
            botJarDir = FileUtils.joinFilePaths(baseWorkDir, "bots");
            matchesDir = FileUtils.joinFilePaths(baseWorkDir, "matches");
            runtimeDir = FileUtils.joinFilePaths(baseWorkDir, "runtime");

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
        private Integer number;
        private Integer workerCount;
        private Integer executionTimeoutSeconds;
        private String matchRuntimeMainClassName;

        public Integer getNumber() {
            return number;
        }

        public Matches setNumber(Integer number) {
            this.number = number;
            return this;
        }

        public Integer getWorkerCount() {
            return workerCount;
        }

        public Matches setWorkerCount(Integer workerCount) {
            this.workerCount = workerCount;
            return this;
        }

        public Integer getExecutionTimeoutSeconds() {
            return executionTimeoutSeconds;
        }

        public Matches setExecutionTimeoutSeconds(Integer executionTimeoutSeconds) {
            this.executionTimeoutSeconds = executionTimeoutSeconds;
            return this;
        }

        public String getMatchRuntimeMainClassName() {
            return matchRuntimeMainClassName;
        }

        public Matches setMatchRuntimeMainClassName(String matchRuntimeMainClassName) {
            this.matchRuntimeMainClassName = matchRuntimeMainClassName;
            return this;
        }
    }

    public class Mail {
        private String from;
        private String bcc;

        public String getFrom() {
            return from;
        }

        public Mail setFrom(String from) {
            this.from = from;
            return this;
        }

        public String getBcc() {
            return bcc;
        }

        public Mail setBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }
    }
}
