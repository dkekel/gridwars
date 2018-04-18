package cern.ais.gridwars.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


/**
 * Used to redirect the sysout and syserr output from the bots to a file
 *
 * The class also uses an internal counter in order to limit the amount of data that is written
 * to the output file. This is done to not have bots pile up the file system with output. If the
 * output limit has been reached, new print requests will simply be ignored.
 *
 * A single info message will be printed to the output before to inform about the fact that the
 * output will be ignored from now on.
 */
public class BotPrintWriter extends PrintStream {

    private final int writtenBytesLimit;
    private int writtenByteCount = 0;
    private boolean printedSaturationWarning = false;
    private boolean enforceWritingToOutput = false;

    public BotPrintWriter(File outputFile, int writtenBytesLimit) throws FileNotFoundException {
        super(outputFile);
        this.writtenBytesLimit = writtenBytesLimit;
    }

    @Override
    public void write(int c) {
        onlyWriteIfAllowed(() -> {
            super.write(c);
            increaseWrittenByCount(1);
        });
    }

    @Override
    public void write(char[] buf, int off, int len) {
        onlyWriteIfAllowed(() -> {
            super.write(buf, off, len);
            increaseWrittenByCount(len);
        });
    }

    @Override
    public void write(String s, int off, int len) {
        onlyWriteIfAllowed(() -> {
            super.write(s, off, len);
            increaseWrittenByCount(len);
        }
    }

    private void onlyWriteIfAllowed(Runnable writerRunnable) {
        if (hasReachedLimit() && !enforceWritingToOutput) {
            // TODO print a warning message to the output
            // ignore write action...
        } else {
            writerRunnable.run();
        }
    }

    private boolean hasReachedLimit() {
        return writtenByteCount >= writtenBytesLimit;
    }

    private void increaseWrittenByCount(int writtenCharacters) {
        writtenByteCount += (writtenCharacters * 2); // a char in Java has 2 bytes
    }

    /**
     * Enable writing to output even if the output limit has already been reached. This is useful
     * for printing error messages from the match execution runtime to the bot output.
     */
    public void setEnforceWritingToOutput(boolean enforceWritingToOutput) {
        this.enforceWritingToOutput = enforceWritingToOutput;
    }
}
