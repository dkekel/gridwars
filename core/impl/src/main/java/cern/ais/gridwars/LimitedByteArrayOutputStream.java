package cern.ais.gridwars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


final class LimitedByteArrayOutputStream extends ByteArrayOutputStream {

    final static String OUTPUT_EXCEEDED_WARNING_MSG = "Output has exceeded the allowed maximum of " +
        "bytes. All further output will be discarded. Byte limit: ";

    private final int byteOutputLimit;
    private boolean printedLimitReachedWarning = false;

    LimitedByteArrayOutputStream(int byteOutputLimit) {
        super(byteOutputLimit + 255); // Give it some more space for the warning message
        this.byteOutputLimit = byteOutputLimit;
    }

    @Override
    public void write(int b) {
        onlyWriteIfAllowed(1, () -> super.write(b));
    }

    @Override
    public void write(byte[] b) {
        onlyWriteIfAllowed(b.length, () -> super.write(b));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        onlyWriteIfAllowed(len, () -> super.write(b, off, len));
    }

    boolean hasOutput() {
        return size() > 0;
    }

    private void onlyWriteIfAllowed(int bytesCountAboutToBeWritten, DeferredWriteAction writeAction) {
        if ((hasReachedLimit() || willReachLimit(bytesCountAboutToBeWritten))) {
            if (!printedLimitReachedWarning) {;
                final byte[] warningMessageBytes = (OUTPUT_EXCEEDED_WARNING_MSG + byteOutputLimit).getBytes();
                super.write(warningMessageBytes, 0, warningMessageBytes.length);
                printedLimitReachedWarning = true;
            }

            // ignore the output and do nothing
        } else {
            try {
                writeAction.doWrite();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean hasReachedLimit() {
        return size() >= byteOutputLimit;
    }

    private boolean willReachLimit(int bytesAboutToBeWritten) {
        return (size() + bytesAboutToBeWritten) > byteOutputLimit;
    }

    @FunctionalInterface
    private interface DeferredWriteAction {
        void doWrite() throws IOException;
    }
}
