package cern.ais.gridwars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 * Used to redirect the sysout and syserr output from the bots to a file.
 *
 * The implementation also uses an internal counter in order to limit the amount of bytes that are written
 * to the output file. This is done to not have bots pile up the file system with output. If the
 * output limit has been reached, new print requests will simply be ignored.
 *
 * A single info message will be printed to the output before to inform about the fact that the
 * output will be ignored from now on.
 */
final class BotPrintWriter extends PrintStream {

    BotPrintWriter(File outputFile, int writtenBytesLimit, boolean append) throws FileNotFoundException {
        super(new BotPrintWriter.LimitedSizeFileOutputStream(outputFile, writtenBytesLimit, append));
    }

    private static final class LimitedSizeFileOutputStream extends FileOutputStream {

        private final int writtenBytesLimit;
        private int writtenByteCount = 0;
        private boolean printedLimitReachedWarning = false;

        private LimitedSizeFileOutputStream(File file, int writtenBytesLimit, boolean append) throws FileNotFoundException {
            super(file, append);
            this.writtenBytesLimit = writtenBytesLimit;
        }

        @Override
        public void write(int b) throws IOException {
            onlyWriteIfAllowed(1, () -> {
                super.write(b);
                increaseWrittenByCount(1);
            });
        }

        @Override
        public void write(byte[] b) throws IOException {
            onlyWriteIfAllowed(b.length, () -> {
                super.write(b);
                increaseWrittenByCount(b.length);
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            onlyWriteIfAllowed(len, () -> {
                super.write(b, off, len);
                increaseWrittenByCount(len);
            });
        }

        private void onlyWriteIfAllowed(int bytesAboutToBeWritten, DeferredWriteAction writeAction) throws IOException {
            if ((hasReachedLimit() || willReachLimit(bytesAboutToBeWritten))) {
                if (!printedLimitReachedWarning) {
                    String warningMessage = "Print output has exceeded the allowed maximum of " + writtenBytesLimit +
                        " bytes. All further output will be discarded.";
                    super.write(warningMessage.getBytes());
                    printedLimitReachedWarning = true;
                }

                // ignore the output and do nothing
            } else {
                writeAction.doWrite();
            }
        }

        private boolean hasReachedLimit() {
            return writtenByteCount >= writtenBytesLimit;
        }

        private boolean willReachLimit(int bytesAboutToBeWritten) {
            return (writtenByteCount + bytesAboutToBeWritten) > writtenBytesLimit;
        }

        private void increaseWrittenByCount(int writtenBytes) {
            writtenByteCount += writtenBytes;
        }

        @FunctionalInterface
        private interface DeferredWriteAction {
            void doWrite() throws IOException;
        }
    }
}
