package cern.ais.gridwars.runtime;

import cern.ais.gridwars.Game;
import cern.ais.gridwars.Player;
import cern.ais.gridwars.bot.PlayerBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static cern.ais.gridwars.runtime.LogUtils.*;


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
public class MatchRuntime {

    // TODO determine adequate match timeout
    // TODO should I make this configurable via sys prop??
    private static final long MATCH_TIME_OUT_MILLIS = 60 * 1000; // 60 seconds
    private static final long MEGA_BYTE_FACTOR = 1024 * 1024;

    private final BotClassLoader botClassLoader = new BotClassLoader();
    private final MatchTurnDataSerializer matchTurnDataSerializer = new MatchTurnDataSerializer();
    private final MatchResult matchResult = new MatchResult();
    private final List<Player> players = new ArrayList<>(2);
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
            return botClassLoader.loadAndInstantiateBot(botJarPath, botClassName);
        } catch (Exception e) {
            error("Failed to load and instantiate bot " + botNumber + ": " + e.getMessage());
            throw e;
        }
     }

    private void playMatch(PlayerBot bot1, PlayerBot bot2) {
        info("Starting match ...");
        matchResult.clear();
        players.clear();
        turnStates.clear();

        players.add(createPlayer(0, bot1, MatchFile.BOT_1_OUTPUT.fileName));
        players.add(createPlayer(1, bot2, MatchFile.BOT_2_OUTPUT.fileName));

        Game game = new Game(players, (player, turn, movementCommands, binaryGameStatus) ->
            turnStates.add(binaryGameStatus.array())
        );

        long matchStartTimeMillis = System.currentTimeMillis();
        try {
            game.startUp();

            while (!game.done()) {
                if ((System.currentTimeMillis() - matchStartTimeMillis) > MATCH_TIME_OUT_MILLIS) {
                    throw new TimeoutException();
                }
                game.nextTurn();
            }

            populateSuccessfulMatchResult(game.getWinner());
        } catch (TimeoutException ignored) {
            String errorMessage = "Match execution took too long and timed out after " + MATCH_TIME_OUT_MILLIS + " ms";
            error(errorMessage);
            populateErrorMatchResult(errorMessage);
        }
        matchDurationMillis = System.currentTimeMillis() - matchStartTimeMillis;

        info("... finished match");
    }

    private Player createPlayer(int playerIndex, PlayerBot bot, String outputFileName) {
        try {
            return new Player(playerIndex, bot, new File(outputFileName), playerIndex);
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

    private void persistTurnStates() {
        info("Persisting turn states data of size [mb]: " + matchTurnDataSerializer.calculateTurnStatesSizeInMb(turnStates.size()));
        long start = System.currentTimeMillis();
        matchTurnDataSerializer.serializeToFile(turnStates, MatchFile.TURN_DATA.fileName);
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

        for (Player player : players) {
            try {
                player.getOutputStream().close();
            } catch (IOException ignored) {
            }
        }
        players.clear();
    }

    public static class MatchExecutionException extends RuntimeException {

        MatchExecutionException(String message) {
            super(message);
        }

        MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
