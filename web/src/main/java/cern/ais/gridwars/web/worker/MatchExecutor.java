package cern.ais.gridwars.web.worker;


import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.service.FileUtils;

import java.io.File;
import java.time.Instant;
import java.util.Random;

public class MatchExecutor {

    private final String matchWorkDir;
    private File matchDir;

    public MatchExecutor(GridWarsProperties gridWarsProperties) {
        this.matchWorkDir = determineMatchWorkDir(gridWarsProperties.getWorkdir());
    }

    private String determineMatchWorkDir(String workDir) {
        return FileUtils.joinFilePaths(workDir, "matches");
    }

    public Match executeMatch(Match match) {
        try {
           createMatchDir(match);
           return doExecuteMatch(match);
        } finally {
            cleanUp();
        }
    }

    private void createMatchDir(Match match) {
        String matchDirPath = FileUtils.joinFilePaths(matchWorkDir, match.getId());
        matchDir = new File(matchDirPath);

        if (!matchDir.exists()) {
            if (!matchDir.mkdir()) {
                throw new MatchExecutionException("Could not create match dir: " + matchDirPath);
            }
        } else {
            if (!matchDir.isDirectory()) {
                throw new MatchExecutionException("Match dir file exist, but is not a directory: " + matchDirPath);
            }

            clearDirectory(matchDir);
        }
    }

    private void clearDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                FileUtils.deleteFile(file);
            }
        }
    }

    private Match doExecuteMatch(Match match) {
        long startTimeMillis = System.currentTimeMillis();

        // TODO implement remote process execution, create some fake match results here.
        Random random = new Random();
        match.setEnded(Instant.now());
        match.setStatus(Match.Status.FINISHED);
        match.setOutcome(Match.Outcome.values()[random.nextInt(Match.Outcome.values().length)]);
        match.setTurns(random.nextInt(3000));

        long durationMillis = System.currentTimeMillis() - startTimeMillis;
        match.setDuration(durationMillis);

        return match;
    }

    private void cleanUp() {
        matchDir = null;
    }

    public static final class MatchExecutionException extends RuntimeException {

        public MatchExecutionException(String message) {
            super(message);
        }

        public MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
