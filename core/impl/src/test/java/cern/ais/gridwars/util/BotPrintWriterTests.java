package cern.ais.gridwars.util;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BotPrintWriterTests {

    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        outputFile = createTestOutputFile();
    }

    @AfterEach
    void tearDown() {
        outputFile.delete();
        outputFile = null;
    }

    @Test
    public void writingOneLongOutputBelowLimitShouldWork() throws IOException {
        final String expectedString = "dakfjasdlkfjasdkljfölkasdjfölkasdjfölkasdjfölasdkf";
        final int maxBytes = expectedString.getBytes().length + 1;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(expectedString);
        botPrintWriter.flush();
        final String actualFileContent = getFileTextContent(outputFile);

        assertEquals(expectedString, actualFileContent);
    }

    @Test
    public void writingOneLongOutputOfExactLimitShouldWork() throws IOException {
        final String expectedString = "dakfjasdlkfjasdkljfölkasdjfölkasdjfölkasdjfölasdkf";
        final int maxBytes = expectedString.getBytes().length;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(expectedString);
        botPrintWriter.flush();
        final String actualFileContent = getFileTextContent(outputFile);

        assertEquals(expectedString, actualFileContent);
    }

    @Test
    public void writingOverLimitShouldCauseWarning() throws IOException {
        final String expectedString = "dakfjasdlkfjasdkljfölkasdjfölkasdjfölkasdjfölasdkf";
        final int maxBytes = expectedString.getBytes().length - 1;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(expectedString);
        botPrintWriter.flush();

        final String expectedWarning = "Print output has exceeded the allowed maximum of " + maxBytes +
            " bytes. All further output will be discarded.";
        final String actualText = getFileTextContent(outputFile);

        assertTrue(actualText.endsWith(expectedWarning));
    }

    @Test
    public void writingSplittedOutputOfExactLengthShouldWork() throws IOException {
        final int stringLength = 10;
        final String baseChar = "a";
        final int maxBytes = stringLength * baseChar.getBytes().length;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        String expectedString = "";
        for (int i = 0; i < stringLength; i++) {
            botPrintWriter.print(baseChar);
            expectedString += baseChar;
        }
        botPrintWriter.flush();

        final String actualText = getFileTextContent(outputFile);

        assertEquals(expectedString, actualText);
    }

    @Test
    public void writingSplittedOutputOverLimitShouldCauseWarning() throws IOException {
        final int stringLength = 10;
        final String baseChar = "a";
        final int maxBytes = (stringLength * baseChar.getBytes().length) - 1;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        for (int i = 0; i < stringLength; i++) {
            botPrintWriter.print(baseChar);
        }
        botPrintWriter.flush();

        final String expectedWarning = "Print output has exceeded the allowed maximum of " + maxBytes +
            " bytes. All further output will be discarded.";
        final String actualText = getFileTextContent(outputFile);

        assertTrue(actualText.endsWith(expectedWarning));
    }

    private File createTestOutputFile() throws IOException {
        File outputFile = File.createTempFile("output_limit_test", ".txt");
        outputFile.deleteOnExit();
        return outputFile;
    }

    private String getFileTextContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
