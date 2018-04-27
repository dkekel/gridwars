package cern.ais.gridwars;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MatchTurnDataSerializerTests {

    @Test
    public void serializationAndDeserializationShouldWork() throws IOException {
        final File turnsFile = createTurnsTmpFile();
        final List<byte[]> expectedTurnStates = generateTestTurnData();
        final MatchDataSerializer serializer = new MatchDataSerializer();
        final int expectedBytesCount = serializer.serializeTurnDataToFile(expectedTurnStates, turnsFile.getAbsolutePath());
        final Optional<InputStream> deserializedTurnStates = serializer.deserializeUncompressedFromFile(turnsFile.getAbsolutePath());

        assertTrue(deserializedTurnStates.isPresent());
        assertThatDeserializedTurnStatesBytesMatchExpectedTurnStatesBytes(expectedTurnStates, expectedBytesCount,
           deserializedTurnStates.get());
    }

    private File createTurnsTmpFile() throws IOException {
        File tmpFile = File.createTempFile("turns", null);
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    private List<byte[]> generateTestTurnData() {
        final Random random = new Random();
        final List<byte[]> turnStates = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            byte[] turnState = new byte[MatchDataSerializer.BYTES_PER_TURN_STATE];
            random.nextBytes(turnState);
            turnStates.add(turnState);
        }

        return turnStates;
    }

    private void assertThatDeserializedTurnStatesBytesMatchExpectedTurnStatesBytes(List<byte[]> expectedTurnStates,
            int expectedDeserializedBytesCount, InputStream deserializedTurnStatesStream) throws IOException {
        int actualBytesReadCount = 0;
        int lastByteRead = 0;
        Iterator<byte[]> expectedTurnStatesIterator = expectedTurnStates.iterator();
        byte[] currentExpectedTurnState = expectedTurnStatesIterator.next();

        while (lastByteRead >= 0) {
            lastByteRead = deserializedTurnStatesStream.read();

            if (lastByteRead >= 0) {
                assertEquals(
                    currentExpectedTurnState[actualBytesReadCount % currentExpectedTurnState.length],
                    (byte) lastByteRead
                );

                actualBytesReadCount++;

                if ((actualBytesReadCount % currentExpectedTurnState.length == 0) && expectedTurnStatesIterator.hasNext()) {
                    currentExpectedTurnState = expectedTurnStatesIterator.next();
                }
            }
        }

        assertEquals(expectedDeserializedBytesCount, actualBytesReadCount);
    }
}
