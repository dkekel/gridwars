package cern.ais.gridwars.runtime;

import cern.ais.gridwars.GameConstants;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class MatchTurnStateSerializer {

    // See: cern.ais.gridwars.Game:325
    static final int BYTES_PER_TURN_STATE = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4;
    private static final double MEGA_BYTE_FACTOR = 1024d * 1024d;
    private static final int GZIP_STREAM_BUFFER_SIZE = (1 << 16);

    public int serializeToFile(List<byte[]> turnStates, String filePath) {
        int totalBytesWritten = 0;

        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(filePath), GZIP_STREAM_BUFFER_SIZE)) {
            for (byte[] turnState : turnStates) {
                gos.write(turnState);
                totalBytesWritten += turnState.length;
            }

            return totalBytesWritten;
        } catch (Exception e) {
           throw new MatchTurnStateSerializerException("Failed to serialize turn states to file: " + filePath, e);
        }
    }

    public double calculateTurnStatesSizeInMb(int turnCount) {
        return ((double) turnCount * (double) BYTES_PER_TURN_STATE) / MEGA_BYTE_FACTOR;
    }

    public InputStream deserializeFromFile(String filePath) {
        try {
            return new GZIPInputStream(new FileInputStream(filePath), GZIP_STREAM_BUFFER_SIZE);
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
