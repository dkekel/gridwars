package cern.ais.gridwars.util;

import java.io.PrintStream;


/**
 * Redirects the default stdout and stderr stream to the specified BotPrintWriter. After the bot
 * output is done, the original output streams should be restored.
 */
public enum StdOutputSwitcher {

    // Enums are the elite way to implement a singleton in Java. Watch in awe and learn! ;)
    INSTANCE;

    private final PrintStream oldOut = System.out;
    private final PrintStream oldErr = System.err;

    public void switchToBotPrintWriter(BotPrintWriter newOut) {
        System.setOut(newOut);
        System.setErr(newOut);
    }

    public void restoreOriginal() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }
}
