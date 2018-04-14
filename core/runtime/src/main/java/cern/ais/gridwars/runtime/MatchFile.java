package cern.ais.gridwars.runtime;

import java.io.File;
import java.nio.file.Paths;

public enum MatchFile {

    STDOUT("stdout.log"),
    STDERR("stderr.log"),
    RESULT("result.properties"),
    BOT_1_OUTPUT("bot1.log"),
    BOT_2_OUTPUT("bot2.log"),
    TURN_DATA("turns.gz");

    final String fileName;

    MatchFile(String fileName) {
        this.fileName = fileName;
    }

    public File toFile(String baseDir) {
        return new File(toAbsolutePath(baseDir));
    }

    public File toFile(File baseDirFile) {
        return new File(baseDirFile, fileName);
    }

    public String toAbsolutePath(String baseDir) {
        return Paths.get(baseDir, fileName).toString();
    }

    public String toAbsolutePath(File baseDirFile) {
        return toAbsolutePath(baseDirFile.getAbsolutePath());
    }

    public boolean existsAndHasContent(String baseDir) {
        File file = toFile(baseDir);
        return file.exists() && file.canRead() && (file.length() > 0);
    }
}
