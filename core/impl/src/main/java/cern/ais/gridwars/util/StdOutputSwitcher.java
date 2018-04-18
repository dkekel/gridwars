package cern.ais.gridwars.util;

import java.io.FileOutputStream;
import java.io.PrintStream;


public final class StdOutputSwitcher {

    private static final StdOutputSwitcher instance = new StdOutputSwitcher();
    private final PrintStream oldOut = System.out;
    private final PrintStream oldErr = System.err;

    public static StdOutputSwitcher getInstance() {
        return instance;
    }

    private StdOutputSwitcher() {
    }

    public void switchToBotPrintWriter(BotPrintWriter newOut) {
        System.setOut(newOut);
        System.setErr(newOut);
    }

    public void switchToFile(FileOutputStream newOut) {
        System.setOut(new PrintStream(newOut));
        System.setErr(new PrintStream(newOut));
    }

    public void restoreInitial() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }
}
