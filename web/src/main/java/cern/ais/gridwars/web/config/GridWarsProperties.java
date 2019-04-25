package cern.ais.gridwars.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;


@Configuration
@ConfigurationProperties("gridwars")
public class GridWarsProperties {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final Directories directories = new Directories();
    private final Registration registration = new Registration();
    private final Matches matches = new Matches();
    private final Mail mail = new Mail();
    private final OAuth oAuth = new OAuth();
    private final Jwt jwt = new Jwt();

    @PostConstruct
    public void init() {
        directories.initDirs();
    }

    public Directories getDirectories() {
        return directories;
    }

    public Registration getRegistration() {
        return registration;
    }

    public Matches getMatches() {
        return matches;
    }

    public Mail getMail() {
        return mail;
    }

    public OAuth getOAuth() {
        return oAuth;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public class Directories {
        private String baseWorkDir;
        private String botJarDir;
        private String matchesDir;
        private String runtimeDir;

        private void initDirs() {
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

    public class Registration {
        private Boolean enabled;
        private String registrationPassword;

        public Boolean getEnabled() {
            return enabled;
        }

        public Registration setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getRegistrationPassword() {
            return registrationPassword;
        }

        public Registration setRegistrationPassword(String registrationPassword) {
            this.registrationPassword = registrationPassword;
            return this;
        }
    }

    public class Matches {
        private Boolean botUploadEnabled;
        private Integer matchCountPerOpponent;
        private Integer workerCount;
        private String workerHeapSize;
        private Integer executionTimeoutSeconds;

        public Boolean getBotUploadEnabled() {
            return botUploadEnabled;
        }

        public Matches setBotUploadEnabled(Boolean botUploadEnabled) {
            this.botUploadEnabled = botUploadEnabled;
            return this;
        }

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
        private String baseUrl;

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

        public String getBaseUrl() {
            return baseUrl;
        }

        public Mail setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
    }

    public class OAuth {
        private String clientId;
        private String clientSecret;
        private String grantType;
        private String responseType;
        private String authorizeUrl;
        private String tokenUrl;
        private String checkTokenUrl;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getGrantType() {
            return grantType;
        }

        public void setGrantType(String grantType) {
            this.grantType = grantType;
        }

        public String getResponseType() {
            return responseType;
        }

        public void setResponseType(String responseType) {
            this.responseType = responseType;
        }

        public String getAuthorizeUrl() {
            return authorizeUrl;
        }

        public void setAuthorizeUrl(String authorizeUrl) {
            this.authorizeUrl = authorizeUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getCheckTokenUrl() {
            return checkTokenUrl;
        }

        public void setCheckTokenUrl(final String checkTokenUrl) {
            this.checkTokenUrl = checkTokenUrl;
        }
    }

    public class Jwt {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }
    }
}
