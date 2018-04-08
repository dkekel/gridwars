package cern.ais.gridwars.web.util;

import java.nio.file.Paths;

public abstract class FileUtils {

    public static String joinFilePaths(String first, String... paths) {
        return Paths.get(first, paths).toString();
    }

    private FileUtils() {
    }
}
