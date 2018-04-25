package cern.ais.gridwars;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Simply ignores the output by (imaginarily) redirecting it to /dev/null, can be used silence stdout/stderr output.
 */
final class DevNullOutputStream extends OutputStream {

    @Override
    public void write(int i) throws IOException {
        // ignore
    }
}
