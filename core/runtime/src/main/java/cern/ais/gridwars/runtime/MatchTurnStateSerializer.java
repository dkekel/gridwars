package cern.ais.gridwars.runtime;

import cern.ais.gridwars.GameConstants;

import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;


public class MatchTurnStateSerializer {

    // See: cern.ais.gridwars.Game:325
    private static final int BYTES_PER_TURN = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4;
    private static final double MEGA_BYTE_FACTOR = 1024d * 1024d;

    public void serializeToFile(List<byte[]> turnStates, String filePath) {
        try (GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(filePath))) {
            for (byte[] turnState : turnStates) {
                zip.write(turnState, 0, turnState.length);
            }
        } catch (Exception e) {
           throw new MatchTurnStateSerializerException("Failed to serialize turn states to file: " + filePath, e);
        }
    }

    public double calculateTurnStatesSizeInMb(int turnCount) {
        return ((double) turnCount * (double) BYTES_PER_TURN) / MEGA_BYTE_FACTOR;
    }

    public List<byte[]> deserializeFromFile(String filePath) {
        return null;
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
