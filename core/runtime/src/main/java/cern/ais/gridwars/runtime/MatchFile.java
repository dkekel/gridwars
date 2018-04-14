package cern.ais.gridwars.runtime;

import java.io.File;
import java.nio.file.Paths;

public enum MatchFile {

    STDOUT("stdout.log", "Standard Output"),
    STDERR("stderr.log", "Error Output"),
    RESULT("result.properties", "Result Properties"),
    BOT_1_OUTPUT("bot1.log", "Bot 1 Output"),
    BOT_2_OUTPUT("bot2.log", "Bot 2 Output"),
    TURN_DATA("turns.gz", "Turn Data");

    public final String fileName;
    public final String description;

    MatchFile(String fileName, String description) {
        this.fileName = fileName;
        this.description = description;
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
