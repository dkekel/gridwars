package cern.ais.gridwars.runtime;

public final class LogUtils {

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    public static void error(String message, Throwable cause) {
        error(message);
        cause.printStackTrace();
    }

    private LogUtils() {
    }
}
