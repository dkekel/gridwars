package cern.ais.gridwars;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class LimitedByteArrayOutputStreamTests {

    private static final String TEXT_OUTPUT = "dakfjasdlkfjasdkljfölkasdjfölkasdjfölkasdjfölasdkf";

    @Test
    public void writingSingleOutputBelowLimitShouldWork() {
        final int maxBytes = TEXT_OUTPUT.getBytes().length + 1;
        final LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(maxBytes);

        lbaos.write(TEXT_OUTPUT.getBytes());

        assertArrayEquals(TEXT_OUTPUT.getBytes(), lbaos.toByteArray());
    }

    @Test
    public void writingSingleOutputOfExactLimitShouldWork() {
        final int maxBytes = TEXT_OUTPUT.getBytes().length;
        final LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(maxBytes);

        lbaos.write(TEXT_OUTPUT.getBytes());

        assertArrayEquals(TEXT_OUTPUT.getBytes(), lbaos.toByteArray());
    }

    @Test
    public void writingSingleOutputOverLimitShouldCauseWarning() {
        final int maxBytes = TEXT_OUTPUT.getBytes().length - 1;
        final LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(maxBytes);

        lbaos.write(TEXT_OUTPUT.getBytes());

        final byte[] expectedBytes = (LimitedByteArrayOutputStream.OUTPUT_EXCEEDED_WARNING_MSG + maxBytes).getBytes();
        assertArrayEquals(expectedBytes, lbaos.toByteArray());
    }

    @Test
    public void writingSplittedOutputOfExactLengthShouldWork() {
        final byte[] expectedBytes = "aaaaaaaaaa".getBytes();
        final int maxBytes = expectedBytes.length;
        final LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(maxBytes);

        lbaos.write(expectedBytes);

        assertArrayEquals(expectedBytes, lbaos.toByteArray());
    }

    @Test
    public void writingSplittedOutputOverLimitShouldCauseWarning() {
        final byte[] writtenBytes = "aaaaaaaaaa".getBytes();
        final int maxBytes = writtenBytes.length - 1;
        final LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(maxBytes);

        for (int i = 0; i < writtenBytes.length; i++) {
            lbaos.write(writtenBytes[i]);
        }

        final byte[] expectedBytes = ("aaaaaaaaa" + (LimitedByteArrayOutputStream.OUTPUT_EXCEEDED_WARNING_MSG + maxBytes)).getBytes();
        assertArrayEquals(expectedBytes, lbaos.toByteArray());
    }
}
