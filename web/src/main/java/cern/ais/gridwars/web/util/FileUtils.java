package cern.ais.gridwars.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public final class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static void clearDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteFile(file);
            }
        }
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
