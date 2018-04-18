package cern.ais.gridwars.util;

import java.io.PrintStream;


// Enums are the elite way to implement a singleton in Java. Watch in awe and learn! ;)
public enum StdOutputSwitcher {

    INSTANCE;

    private final PrintStream oldOut = System.out;
    private final PrintStream oldErr = System.err;

    public void switchToBotPrintWriter(BotPrintWriter newOut) {
        System.setOut(newOut);
        System.setErr(newOut);
    }

    public void restoreInitial() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }
}
