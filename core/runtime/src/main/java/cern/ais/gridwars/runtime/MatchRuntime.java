package cern.ais.gridwars.runtime;

import cern.ais.gridwars.Game;
import cern.ais.gridwars.Player;
import cern.ais.gridwars.bot.PlayerBot;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
 * This runtime class is used in a very lightweight class path environment. Therfore, it should not make
 * use of any 3rd party libraries and stick to default JDK APIs. The class path will be set up with the
 * GridWars API and implementation and with the player bot jars.
 */
public class MatchRuntime {

    // TODO determine adequate match timeout
    private static final long MATCH_TIME_OUT_MILLIS = 60 * 1000; // 60 seconds

    private final BotClassLoader botClassLoader = new BotClassLoader();
    private final MatchResult matchResult = new MatchResult();
    private final List<Player> players = new ArrayList<>(2);
    private final String bot1JarPath;
    private final String bot2JarPath;
    private final String bot1ClassName;
    private final String bot2ClassName;
    private long matchDurationMillis;
    private int turns;

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

    private String retrieveMandatorySysProp(String sysPropKey) {
        String value = System.getProperty(sysPropKey);

        if ((value == null) || value.isEmpty()) {
            throw new MatchExecutionException("Missing required system property: " + sysPropKey);
        }

        return value;
    }

    private void executeMatch() {
        info("Start match execution ...");
        matchResult.clear();

        try {
            loadBotsAndPlayMatch();
        } catch (Exception e) {
            error("Match execution failed: " + e.getMessage(), e);
            populateErrorMatchResult(e.getMessage());
        } finally {
            cleanUp();
        }

        persistMatchResult();
        logMatchResult();
        info("... finished match execution");
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
        players.clear();
        players.add(createPlayer(0, bot1, MatchRuntimeConstants.BOT_1_OUTPUT_FILE_NAME));
        players.add(createPlayer(1, bot2, MatchRuntimeConstants.BOT_2_OUTPUT_FILE_NAME));

        Game game = new Game(players, (player, turn, movementCommands, binaryGameStatus) -> {
            turns = turn;
            // TODO implement storing binary game data
        });

        long matchStartTimeMillis = System.currentTimeMillis();
        turns = 0;
        game.startUp();
        try {
            while (!game.done()) {
                if ((System.currentTimeMillis() - matchStartTimeMillis) > MATCH_TIME_OUT_MILLIS) {
                    throw new TimeoutException();
                }
                game.nextTurn();
            }

            populateSuccessfulMatchResult(game.getWinner(), turns);
        } catch (TimeoutException ignored) {
            String errorMessage = "Match execution took too long and timed out after " + MATCH_TIME_OUT_MILLIS + " ms";
            info(errorMessage); // Should be error log instead??
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

    private void populateSuccessfulMatchResult(Player winner, int turns) {
        if (players.get(0).equals(winner)) {
            matchResult.setOutcome(MatchResult.Outcome.BOT_1_WON);
        } else if (players.get(1).equals(winner)) {
            matchResult.setOutcome(MatchResult.Outcome.BOT_2_WON);
        } else {
            // winner == null means a draw
            matchResult.setOutcome(MatchResult.Outcome.DRAW);
        }

        matchResult.setTurns(turns);
    }

    private void populateErrorMatchResult(String errorMessage) {
        matchResult.setOutcome(MatchResult.Outcome.ERROR);
        matchResult.setErrorMessage(errorMessage);
    }

    private void cleanUp() {
        for (Player player : players) {
            try {
                player.getOutputStream().close();
            } catch (IOException ignored) {
            }
        }
        players.clear();
    }

    private void persistMatchResult() {
        matchResult.storeToFile(MatchRuntimeConstants.MATCH_RESULT_FILE_NAME);
    }

    private void logMatchResult() {
        info("Match finished after " + matchResult.getTurns() + " turns in " +
            matchDurationMillis + " ms with result: " + matchResult.getOutcome());
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
