package cern.ais.gridwars.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties("gridwars")
public class GridWarsProperties {

    private String workdir;
    private Matches matches = new Matches();
    private Mail mail = new Mail();

    public String getWorkdir() {
        return workdir;
    }

    public GridWarsProperties setWorkdir(String workdir) {
        this.workdir = workdir;
        return this;
    }

    public Matches getMatches() {
        return matches;
    }

    public GridWarsProperties setMatches(Matches matches) {
        this.matches = matches;
        return this;
    }

    public Mail getMail() {
        return mail;
    }

    public GridWarsProperties setMail(Mail mail) {
        this.mail = mail;
        return this;
    }

    public static class Matches {
        private Integer number;
        private Integer workerCount;

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
    }

    public static class Mail {
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
