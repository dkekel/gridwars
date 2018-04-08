package cern.ais.gridwars.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties("gridwars")
public class GridWarsProperties {

    private String workDir;

    public String getWorkDir() {
        return workDir;
    }

    public GridWarsProperties setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }
}
