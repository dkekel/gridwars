package cern.ais.gridwars;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.stream.Stream;


/**
 * Used to communicate the match result to the GridWars controller
 *
 * The result is communicated to the controller process using a properties file. Yes, Java property files
 * are so 1999, but it's important that the match runtime implementation has a very lightweight class path
 * and only used native JRE classes. So I decided against including a "insert fancy JSON lib name here" dependency.
 */
public final class MatchResult {

    public enum Outcome { BOT_1_WON, BOT_2_WON, DRAW, ERROR }

    private static final String OUTCOME_KEY = "outcome";
    private static final String TURNS_KEY = "turns";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    private Outcome outcome;
    private Integer turns;
    private String errorMessage;

    public Outcome getOutcome() {
        return outcome;
    }

    public MatchResult setOutcome(Outcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public Integer getTurns() {
        return turns;
    }

    public MatchResult setTurns(Integer turns) {
        this.turns = turns;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public MatchResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public void storeToFile(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Properties props = toProperties();
            props.store(fos, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store match result to file: " + filePath, e);
        }
    }

    private Properties toProperties() {
        Properties props = new Properties();
        props.setProperty(OUTCOME_KEY, outcomeToString(getOutcome()));
        props.setProperty(TURNS_KEY, integerToString(getTurns()));
        props.setProperty(ERROR_MESSAGE_KEY, escapeNullToBlank(getErrorMessage()));
        return props;
    }

    private String outcomeToString(Outcome outcome) {
        return (outcome == null) ? "" : outcome.name();
    }

    private String integerToString(Integer integer) {
        return (integer == null) ? "" : integer.toString();
    }

    private String escapeNullToBlank(String value) {
        return (value == null) ? "" : value;
    }

    public static MatchResult loadFromFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            Properties props = new Properties();
            props.load(fis);
            return new MatchResult().fromProps(props);
        } catch (Exception e) {
            throw new RuntimeException("Loading match result from file failed: " + filePath, e);
        }
    }

    private MatchResult fromProps(Properties props) {
        setOutcome(toOutcome(props.getProperty(OUTCOME_KEY)));
        setTurns(toTurn(props.getProperty(TURNS_KEY)));
        setErrorMessage(props.getProperty(ERROR_MESSAGE_KEY));
        return this;
    }

    private Outcome toOutcome(String outcomeName) {
        return Stream.of(Outcome.values())
            .filter(outcome -> outcome.name().equals(outcomeName))
            .findFirst()
            .orElse(null);
    }

    private Integer toTurn(String turnsStr) {
        try {
            return Integer.parseInt(turnsStr);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public void clear() {
        setOutcome(null);
        setTurns(null);
        setErrorMessage(null);
    }
}
