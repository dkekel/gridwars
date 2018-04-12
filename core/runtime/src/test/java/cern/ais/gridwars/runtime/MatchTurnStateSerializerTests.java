package cern.ais.gridwars.runtime;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MatchTurnStateSerializerTests {

    @Test
    public void serializationAndDeserializationShouldWork() throws IOException {
        final File turnsFile = createTurnsTmpFile();
        final List<byte[]> expectedTurnStates = generateTestTurnData();
        final MatchTurnStateSerializer serializer = new MatchTurnStateSerializer();
        final int expectedBytesCount = serializer.serializeToFile(expectedTurnStates, turnsFile.getAbsolutePath());

        assertThatDeserializedTurnStatesBytesMatchExpectedTurnStatesBytes(expectedTurnStates, expectedBytesCount,
            serializer.deserializeFromFile(turnsFile.getAbsolutePath()));
    }

    private File createTurnsTmpFile() throws IOException {
        File tmpFile = File.createTempFile("turns", null);
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    private List<byte[]> generateTestTurnData() {
        final Random random = new Random();
        final List<byte[]> turnStates = new LinkedList<>();

        for (int i = 0; i < 30; i++) {
            byte[] turnState = new byte[MatchTurnStateSerializer.BYTES_PER_TURN_STATE];
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
