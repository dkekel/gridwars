package cern.ais.gridwars.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Constructs a single path from the provided paths elements by using the system
     * dependent file separator char ("/" or "\").
     *
     * E.g.: "/bla/blubb", "foo" -> "bla/blubb/foo"
     */
    public static String joinFilePathsToSinglePath(String first, String... paths) {
        return Paths.get(first, paths).toString();
    }

    /**
     * Creates a collection of paths separated by the system dependent path separator
     * char (":" or "\;").
     *
     * E.g.: "/bla/blubb", "/foo/bar" -> "/bla/blubb:/foo/bar"
     */
    public static String joinFilePathsToSeparatedPaths(String... paths) {
        return Stream.of(paths).collect(Collectors.joining(File.pathSeparator));
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
