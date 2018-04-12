package cern.ais.gridwars.runtime;

/**
 * Simple logging implementation that logs to stdout
 *
 * We don't need a power of a full-blown logging framework, so let's keep the classpath
 * small and use this simple implementation ;).
 */
final class LogUtils {

    static void info(String message) {
        System.out.println(createLogPrefix("INFO") + message);
    }

    static void error(String message) {
        System.out.println(createLogPrefix("ERROR") + message);
    }

    static void error(String message, Throwable cause) {
        error(message);
        cause.printStackTrace(System.out);
    }

    private static String createLogPrefix(String logLevel) {
        return "[" + logLevel + "] [" + System.currentTimeMillis() + "] ";
    }

    private LogUtils() {
    }
}
