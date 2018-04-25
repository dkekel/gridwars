package cern.ais.gridwars;

import cern.ais.gridwars.api.bot.PlayerBot;

import java.io.File;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static cern.ais.gridwars.LogUtils.*;


/**
 * Executes a GridWars match by dynamically loading the bot classes, playing the match, and persisting
 * the result to files
 *
 * This class is used in separate JVM processes that are created and controlled by the GridWars controller
 * web app. It's important to run the matches in isolated JVM processes to prevent the main controller app
 * JVM from failing. Also, this gives us more control over the memory, the available class path, reflections,
 * etc.
 *
 * This runtime class is used in a very lightweight class path environment. Therefore, it should not make
 * use of any 3rd party libraries and stick to default JDK APIs. The class path will be set up with the
 * GridWars API and implementation and with the player bot jars.
 */
final class MatchRuntime {

    private static final long MEGA_BYTE_FACTOR = 1024 * 1024;

    private final BotClassLoader botClassLoader = new BotClassLoader();
    private final MatchDataSerializer matchTurnDataSerializer = new MatchDataSerializer();
    private final MatchResult matchResult = new MatchResult();
    private final List<Player> players = new ArrayList<>(2);
    private final List<LimitedByteArrayOutputStream> playerOutputStreams = new ArrayList<>(2);
    private final List<byte[]> turnStates = new LinkedList<>();
    private final String bot1JarPath;
    private final String bot2JarPath;
    private final String bot1ClassName;
    private final String bot2ClassName;
    private long matchDurationMillis;

    public static void main(String[] args) {
        new MatchRuntime().executeMatch();
    }

    private MatchRuntime() {
        bot1JarPath = retrieveMandatorySysProp(MatchRuntimeConstants.BOT_1_JAR_PATH_SYS_PROP_KEY);
        bot2JarPath = retrieveMandatorySysProp(MatchRuntimeConstants.BOT_2_JAR_PATH_SYS_PROP_KEY);
        bot1ClassName = retrieveMandatorySysProp(MatchRuntimeConstants.BOT_1_CLASS_NAME_SYS_PROP_KEY);
        bot2ClassName = retrieveMandatorySysProp(MatchRuntimeConstants.BOT_2_CLASS_NAME_SYS_PROP_KEY);

        logEnvironmentInfo();
    }

    private void logEnvironmentInfo() {
        info("=====================================================================================");
        info("Time: " + LocalDateTime.now().toString());
        info("Work dir: " + FileSystems.getDefault().getPath("").toAbsolutePath().toString());
        info("Java home: " + System.getProperty("java.home"));
        info("Class path: " + System.getProperty("java.class.path"));
        info("Bot 1 jar path: " + bot1JarPath);
        info("Bot 2 jar path: " + bot2JarPath);
        info("Bot 1 class name: " + bot1ClassName);
        info("Bot 2 class name: " + bot2ClassName);
        info("=====================================================================================");
    }

    private void logMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        final long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE_FACTOR;
        final long maxMemoryMb = runtime.maxMemory() / MEGA_BYTE_FACTOR;
        info("Memory usage [MB}: " + usedMemoryMb + " / " + maxMemoryMb);
    }

    private String retrieveMandatorySysProp(String sysPropKey) {
        String value = System.getProperty(sysPropKey);

        if ((value == null) || value.isEmpty()) {
            throw new MatchExecutionException("Missing required system property: " + sysPropKey);
        }

        return value;
    }

    private void executeMatch() {
        try {
            info("Start match execution ...");
            logMemoryInfo();

            try {
                loadBotsAndPlayMatch();
                logMatchResult();
                persistTurnStates();
            } catch (Exception e) {
                error("Match execution failed: " + e.getMessage(), e);
                populateErrorMatchResult(e.getMessage());
            }

            persistMatchResult();
            logMemoryInfo();
            info("... finished match execution");
        } finally {
            cleanUp();
        }
    }

    private void loadBotsAndPlayMatch() {
        PlayerBot bot1 = loadAndInstantiateBotClass(bot1JarPath, bot1ClassName, 1);
        PlayerBot bot2 = loadAndInstantiateBotClass(bot2JarPath, bot2ClassName, 2);
        playMatch(bot1, bot2);
    }

    private PlayerBot loadAndInstantiateBotClass(String botJarPath, String botClassName, int botNumber) {
        info("Load and instantiate bot " + botNumber + " class \"" + botClassName +"\" from jar: " + botJarPath);

        try {
            final long startMillis = System.currentTimeMillis();
            PlayerBot bot = botClassLoader.loadAndInstantiateBot(botJarPath, botClassName);
            final long durationMillis = System.currentTimeMillis() - startMillis;

            info("Instantiation of " + botNumber + " class \"" + botClassName +"\" took [ms]: " + durationMillis);

            return bot;
        } catch (Exception e) {
            throw new MatchExecutionException("Failed to load and instantiate bot  " + botNumber + ": " + botClassName, e);
        }
     }

    private void playMatch(PlayerBot bot1, PlayerBot bot2) {
        info("Starting match ...");
        matchResult.clear();
        playerOutputStreams.clear();
        players.clear();
        turnStates.clear();

        playerOutputStreams.add(createPlayerOutputStream());
        playerOutputStreams.add(createPlayerOutputStream());
        players.add(createPlayer(0, bot1, playerOutputStreams.get(0)));
        players.add(createPlayer(1, bot2, playerOutputStreams.get(1)));

        final Game game = new Game(players, (player, turn, binaryGameStatus) ->
            turnStates.add(binaryGameStatus.get().array())
        );

        long matchStartTimeMillis = System.currentTimeMillis();
        try {
            game.startUp();

            while (!game.isFinished()) {
                if ((System.currentTimeMillis() - matchStartTimeMillis) > GameConstants.MATCH_TIMEOUT_MS) {
                    throw new TimeoutException();
                }
                game.nextTurn();
            }

            populateSuccessfulMatchResult(game.getWinner());
        } catch (TimeoutException ignored) {
            String errorMessage = "Match execution timed out after " + GameConstants.MATCH_TIMEOUT_MS + " ms";
            error(errorMessage);
            populateErrorMatchResult(errorMessage);
        } finally {
            persistPlayerOutputs();
            game.cleanUp();
        }
        matchDurationMillis = System.currentTimeMillis() - matchStartTimeMillis;

        info("... finished match");
    }

    private LimitedByteArrayOutputStream createPlayerOutputStream() {
        return new LimitedByteArrayOutputStream(GameConstants.BOT_PRINT_OUTPUT_BYTE_LIMIT);
    }

    private Player createPlayer(int playerIndex, PlayerBot bot, LimitedByteArrayOutputStream botOutputStream) {
        try {
            return new Player(playerIndex, bot, botOutputStream);
        } catch (Exception e) {
            throw new MatchExecutionException("Failed to create player " + (playerIndex + 1), e);
        }
    }

    private void populateSuccessfulMatchResult(Player winner) {
        if (players.get(0).equals(winner)) {
            matchResult.setOutcome(MatchResult.Outcome.BOT_1_WON);
        } else if (players.get(1).equals(winner)) {
            matchResult.setOutcome(MatchResult.Outcome.BOT_2_WON);
        } else {
            // winner == null means a draw... yeah, intuitive API, I know...
            matchResult.setOutcome(MatchResult.Outcome.DRAW);
        }

        matchResult.setTurns(turnStates.size());
    }

    private void populateErrorMatchResult(String errorMessage) {
        matchResult.setOutcome(MatchResult.Outcome.ERROR);
        matchResult.setErrorMessage(errorMessage);
    }

    private void persistPlayerOutputs() {
        persistPlayerOutput(0, MatchFile.BOT_1_OUTPUT);
        persistPlayerOutput(1, MatchFile.BOT_2_OUTPUT);
    }

    private void persistPlayerOutput(int playerIndex, MatchFile outputMatchFile) {
        LimitedByteArrayOutputStream outputStream = playerOutputStreams.get(playerIndex);
        if (!outputStream.hasOutput()) {
            info("Player " + (playerIndex + 1) + " did not create any output");
            return;
        }

        info("Persisting output of player " + (playerIndex + 1) + " with size [KB]: " + (outputStream.size() / 1024.0));

        long start = System.currentTimeMillis();
        matchTurnDataSerializer.serializeBytesToFile(outputStream.toByteArray(), outputMatchFile.fileName);
        info("Persisting output of player " + (playerIndex + 1) + " took [ms]: " + (System.currentTimeMillis() - start));
    }

    private void persistTurnStates() {
        info("Persisting turn states data of size [MB]: " +
            matchTurnDataSerializer.calculateTurnStatesSizeInMb(turnStates.size()));
        long start = System.currentTimeMillis();
        matchTurnDataSerializer.serializeTurnDataToFile(turnStates, MatchFile.TURN_DATA.fileName);
        info("Persisting turn states data took [ms]: " + (System.currentTimeMillis() - start));
    }

    private void persistMatchResult() {
        matchResult.storeToFile(MatchFile.RESULT.fileName);
    }

    private void logMatchResult() {
        info("Match finished after " + matchResult.getTurns() + " turns in " + matchDurationMillis +
            " ms with outcome: " + matchResult.getOutcome());
    }

    private void cleanUp() {
        matchResult.clear();
        turnStates.clear();
        playerOutputStreams.clear();
        players.clear();
    }

    static class MatchExecutionException extends RuntimeException {

        MatchExecutionException(String message) {
            super(message);
        }

        MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
