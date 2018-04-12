package cern.ais.gridwars.runtime;

/**
 * Simple logging implementation that logs to stdout and stderr
 *
 * We don't need a power of a full-blown logging framework, so let's keep the classpath
 * small and use this simple implementation ;).
 */
public final class LogUtils {

    public static void info(String message) {
        System.out.println(createLogPrefix("INFO") + message);
    }

    public static void error(String message) {
        System.err.println(createLogPrefix("ERROR") + message);
    }

    public static void error(String message, Throwable cause) {
        error(message);
        cause.printStackTrace();
    }

    private static String createLogPrefix(String logLevel) {
        return "[" + logLevel + "] [" + System.currentTimeMillis() + "] ";
    }

    private LogUtils() {
    }
}
