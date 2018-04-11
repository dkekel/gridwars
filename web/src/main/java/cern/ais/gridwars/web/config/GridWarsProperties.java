package cern.ais.gridwars.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties("gridwars")
public class GridWarsProperties {

    private String workdir;
    private Matches matches = new Matches();

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
}
