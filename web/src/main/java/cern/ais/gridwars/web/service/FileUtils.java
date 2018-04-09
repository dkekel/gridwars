package cern.ais.gridwars.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

public final class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static String joinFilePaths(String first, String... paths) {
        return Paths.get(first, paths).toString();
    }

    public static void deleteFile(File file) {
        if (file != null) {
            if (!file.delete()) {
                LOG.warn("File could not be deleted: {}", file.getAbsolutePath());
            }
        }
    }

    private FileUtils() {
    }
}
