package cern.ais.gridwars;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Stores/loads the binary match turn data to/from the file system
 *
 * The turn data of a match is list of turns as a byte array. These bytes are compressed using the GZIP
 * algorithm and stored in a specified file path.
 *
 * When loading the data back from the file, it can be loaded in the compressed GZIP format or uncompressed.
 * The raw turn data is usually only used in the browser to replay the match. We can leverage the fact
 * that the browsers accept GZIP compressed responses and simply load the data compressed from the file system.
 * Setting the correct response compression headers, the browser will take care of uncompressing the bytes. As
 * a result, we save a lot of disk and network i/o and also CPU, because we don't need to ever uncompress the
 * bytes on the server-side. Neat!
 */
public final class MatchTurnDataSerializer {

    // See: cern.ais.gridwars.Game:325
    static final int BYTES_PER_TURN_STATE = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4;
    private static final double MEGA_BYTE_FACTOR = 1024d * 1024d;
    private static final int GZIP_STREAM_BUFFER_SIZE = (1 << 16);

    int serializeToFile(List<byte[]> turnStates, String filePath) {
        int totalBytesWritten = 0;

        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(filePath), GZIP_STREAM_BUFFER_SIZE)) {
            for (byte[] turnState : turnStates) {
                gos.write(turnState);
                totalBytesWritten += turnState.length;
            }

            return totalBytesWritten;
        } catch (Exception e) {
           throw new MatchTurnStateSerializerException("Failed to serialize turn data to file: " + filePath, e);
        }
    }

    double calculateTurnStatesSizeInMb(int turnCount) {
        return ((double) turnCount * (double) BYTES_PER_TURN_STATE) / MEGA_BYTE_FACTOR;
    }

    public Optional<InputStream> deserializeUncompressedFromFile(String filePath) {
        return deserializeFromFile(filePath, true);
    }

    public Optional<InputStream> deserializeCompressedFromFile(String filePath) {
        return deserializeFromFile(filePath, false);
    }

    private Optional<InputStream> deserializeFromFile(String filePath, boolean uncompress) {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead() || (file.length() == 0)) {
            return Optional.empty();
        }

        try {
            // IMPORTANT: this input stream is return to the caller code, which will read from it, so it must
            // not be closed here!
            FileInputStream fis = new FileInputStream(file);
            return uncompress
                ? Optional.of(new GZIPInputStream(fis, GZIP_STREAM_BUFFER_SIZE))
                : Optional.of(fis);
        } catch (IOException e) {
            throw new MatchTurnStateSerializerException("Failed to deserialize turn states from file: " + filePath, e);
        }
    }

    public static class MatchTurnStateSerializerException extends RuntimeException {

        public MatchTurnStateSerializerException(String message) {
            super(message);
        }

        public MatchTurnStateSerializerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
