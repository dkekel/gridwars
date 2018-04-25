package cern.ais.gridwars;

import java.io.File;
import java.nio.file.Paths;

public enum MatchFile {

    STDOUT("stdout.log", "Standard Output", false),
    STDERR("stderr.log", "Error Output", false),
    RESULT("result.properties", "Result Properties", false),
    BOT_1_OUTPUT("bot1.log.gz", "Bot 1 Output", true),
    BOT_2_OUTPUT("bot2.log.gz", "Bot 2 Output", true),
    TURN_DATA("turns.gz", "Turn Data", true);

    public final String fileName;
    public final String description;
    public final boolean compressed;

    MatchFile(String fileName, String description, boolean compressed) {
        this.fileName = fileName;
        this.description = description;
        this.compressed = compressed;
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
