package cern.ais.gridwars;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BotPrintWriterTests {

    private static final String TEXT_OUTPUT = "dakfjasdlkfjasdkljfölkasdjfölkasdjfölkasdjfölasdkf";
    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        outputFile = createTestOutputFile();
    }

    @AfterEach
    void tearDown() {
        if (outputFile != null) {
            outputFile.delete();
            outputFile = null;
        }
    }

    @Test
    public void writingSingleOutputBelowLimitShouldWork() throws IOException {
        final int maxBytes = TEXT_OUTPUT.getBytes().length + 1;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(TEXT_OUTPUT);
        botPrintWriter.flush();
        final String actualFileContent = getFileTextContent(outputFile);

        assertEquals(TEXT_OUTPUT, actualFileContent);
    }

    @Test
    public void writingSingleOutputOfExactLimitShouldWork() throws IOException {
        final int maxBytes = TEXT_OUTPUT.getBytes().length;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(TEXT_OUTPUT);
        botPrintWriter.flush();
        final String actualFileContent = getFileTextContent(outputFile);

        assertEquals(TEXT_OUTPUT, actualFileContent);
    }

    @Test
    public void writingSingleOutputOverLimitShouldCauseWarning() throws IOException {
        final int maxBytes = TEXT_OUTPUT.getBytes().length - 1;
        BotPrintWriter botPrintWriter = new BotPrintWriter(outputFile, maxBytes, false);

        botPrintWriter.print(TEXT_OUTPUT);
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
