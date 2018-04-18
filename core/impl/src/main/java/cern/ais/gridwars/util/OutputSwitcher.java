package cern.ais.gridwars.util;

import java.io.FileOutputStream;
import java.io.PrintStream;


public final class OutputSwitcher {

    private static final OutputSwitcher instance = new OutputSwitcher();
    private final PrintStream oldOut = System.out;
    private final PrintStream oldErr = System.err;

    private OutputSwitcher() {
    }

    public void switchToFile(FileOutputStream newOut) {
        System.setOut(new PrintStream(newOut));
        System.setErr(new PrintStream(newOut));
    }

    public void restoreInitial() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    public static OutputSwitcher getInstance() {
        return instance;
    }
}
