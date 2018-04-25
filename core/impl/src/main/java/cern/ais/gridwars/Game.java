/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.command.MovementCommand;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;


final class Game {

    interface TurnCallback {
        void onPlayerResponse(Player player, int turn, Supplier<ByteBuffer> binaryGameStatusSupplier);
    }

    private static final int UNIVERSE_STATE_BYTE_SIZE = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4;
    private static final byte[] EMPTY_PIXEL = { 0, 0, 0, 0 };

    private final List<Player> playerList = new LinkedList<>();
    private final Universe universe = new ArrayUniverse();
    private final Random random = new Random();
    private final TurnCallback turnCallback;
    private final boolean debugMode;
    private Iterator<Player> playerIterator;
    private int currentTurn;

    Game(List<Player> playerList, TurnCallback turnCallback) {
        this(playerList, turnCallback, false);
    }

    Game(List<Player> playerList, TurnCallback turnCallback, boolean debugMode) {
        this.playerList.addAll(Objects.requireNonNull(playerList));
        this.playerIterator = this.playerList.iterator();
        this.turnCallback = turnCallback;
        this.debugMode = debugMode;

        if (playerList.size() < 2) {
            throw new IllegalArgumentException("Need at least two players to play a game");
        }
    }

    // TODO can we get rid of this method? We don't want to give out the keys to the universe!!
    Universe getUniverse() {
        return universe;
    }

    void startUp() {
        for (Player player : playerList) {
            Coordinates startCoordinates = getRandomCoordinates();

            // Make sure no two players start in the same position
            while (!universe.getCell(startCoordinates).isEmpty()) {
                startCoordinates = getRandomCoordinates();
            }

            universe.getCell(startCoordinates).moveIn(player, GameConstants.STARTING_POPULATION);
        }

        currentTurn = 1;
    }

    private Coordinates getRandomCoordinates() {
        return CoordinatesImpl.of(
            random.nextInt(GameConstants.UNIVERSE_SIZE),
            random.nextInt(GameConstants.UNIVERSE_SIZE)
        );
    }

    /**
     * Computes one turn for the "next player". If this completes a turn round, also increases populations
     */
    void nextTurn() {
        if (!playerIterator.hasNext()) {
            throw new IllegalStateException("nextTurn() was called but no next player available");
        }

        final Player player = playerIterator.next();
        final boolean currentTurnEndsRound = !playerIterator.hasNext();
        final UniverseView universeView = new UniverseViewImpl(universe, player, currentTurn);
        final PlayerTurnThread playerTurnThread = new PlayerTurnThread(player, universeView, currentTurn,
            debugMode ? 0 : GameConstants.TURN_TIMEOUT_MS);

        try {
            if (player.hasBotOutputPrintWriter()) {
                StdOutputSwitcher.INSTANCE.switchToPrintStream(player.getBotOutputPrintStream());
            }

            final List<MovementCommand> movementCommands = playerTurnThread.getNextMovementCommands();

            if (validateAllMoves(player, movementCommands)) {
                for (MovementCommand movementCommand : movementCommands) {
                    executeMovement(player, movementCommand);
                }
            }
        } finally {
            if (player.hasBotOutputPrintWriter()) {
                player.getBotOutputPrintStream().flush();
                StdOutputSwitcher.INSTANCE.restoreOriginal();
            }
        }

        if (currentTurnEndsRound) {
            growAllCellPopulations();
            playerIterator = playerList.iterator(); // start again with first player in the next call
        } else {
            truncateAllCellPopulations();
        }

        if (turnCallback != null) {
            turnCallback.onPlayerResponse(player, currentTurn, this::getBinaryGameStatus);
        }

        currentTurn++;
    }

    private void growAllCellPopulations() {
        universe.getAllNonEmptyCells().forEach(Cell::growPopulation);
    }

    private void truncateAllCellPopulations() {
        universe.getAllNonEmptyCells().forEach(Cell::truncatePopulation);
    }

    private boolean validateAllMoves(Player player, List<MovementCommand> movementCommands) {
        final HashMap<Coordinates, Integer> totalMoveOutAmounts = new HashMap<>();

        for (MovementCommand movementCommand : movementCommands) {
            if (movementCommand != null) {
                if (!validateMove(player, movementCommand)) {
                    return false;
                }

                int currentValue = 0;

                if (totalMoveOutAmounts.containsKey(movementCommand.getCoordinatesFrom())) {
                    currentValue = totalMoveOutAmounts.get(movementCommand.getCoordinatesFrom());
                }

                currentValue += movementCommand.getAmount();
                totalMoveOutAmounts.put(movementCommand.getCoordinatesFrom(), currentValue);
            }
        }

        for (Map.Entry<Coordinates, Integer> entry : totalMoveOutAmounts.entrySet()) {
            if (entry.getValue() > universe.getCell(entry.getKey()).getPopulation()) {
                logMoveValidationFailed("Total sum of troops moved out of a cell is higher than the cell population: " +
                    entry.getKey());
                return false;
            }
        }

        return true;
    }

    private boolean validateMove(Player player, MovementCommand movementCommand) {
        if (!universe.getCell(movementCommand.getCoordinatesFrom()).isOwner(player)) {
            logMoveValidationFailed("Moving troops from a cell not owned by you: " + movementCommand.getCoordinatesFrom());
            return false;
        }

        if (universe.getCell(movementCommand.getCoordinatesFrom()).getPopulation() < movementCommand.getAmount()) {
            logMoveValidationFailed("Moving more troops from a cell than you have: " + movementCommand.getCoordinatesFrom());
            return false;
        }

        return true;
    }

    private void logMoveValidationFailed(String validationErrorMessage) {
        System.out.println("[WARNING] Move validation failed, all moves of turn " + currentTurn + " are ignored: " +
            validationErrorMessage);
    }

    private void executeMovement(Player player, MovementCommand movementCommand) {
        if (movementCommand == null) {
            return;
        }

        // Move from origin...
        universe.getCell(movementCommand.getCoordinatesFrom()).moveOut(player, movementCommand.getAmount());

        // ... into destination
        universe.getCell(movementCommand.getCoordinatesFrom().getNeighbour(movementCommand.getDirection()))
            .moveIn(player, movementCommand.getAmount());
    }

    private ByteBuffer getBinaryGameStatus() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(UNIVERSE_STATE_BYTE_SIZE);

        for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++) {
            for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++) {
                Cell currentCell = universe.getCell(x, y);

                if (currentCell.isEmpty()) {
                    byteBuffer.put(EMPTY_PIXEL);
                } else {
                    // Red, Green, Blue
                    byteBuffer.put(GameConstants.PLAYER_COLORS[currentCell.getOwner().getId()]);

                    // Alpha channel, opacity (0-255)
                    byteBuffer.put((byte) Math.round(255 * currentCell.getPopulation() / GameConstants.MAXIMUM_POPULATION));
                }
            }
        }

        return byteBuffer;
    }

    Player getWinner() {
        assertThatIsFinished();
        boolean potentialDraw = false;
        Player winner = null;
        int winnerPopulation = -1;

        for (Player player : playerList) {
            int count = 0;

            // TODO [optimisation] would a parallel stream reduce operation here be faster?
            for (Coordinates coordinates : universe.getCellCoordinatesForPlayer(player)) {
                count += universe.getCell(coordinates).getPopulation();
            }

            if (count > winnerPopulation) {
                winnerPopulation = count;
                winner = player;
                potentialDraw = false;
            } else if (count == winnerPopulation) {
                potentialDraw = true;
            }
        }

        return potentialDraw ? null : winner;
    }

    private void assertThatIsFinished() {
        if (!isFinished()) {
            throw new IllegalStateException("Game not finished");
        }
    }

    boolean isFinished() {
        return maxTurnLimitReached() || onlyOneOrLessPlayerLeft();
    }

    private boolean maxTurnLimitReached() {
        return (currentTurn > GameConstants.TURN_LIMIT);
    }

    private boolean onlyOneOrLessPlayerLeft() {
        return (universe.getNumberOfAlivePlayers(playerList.size()) <= 1);
    }

    void cleanUp() {
        playerList.forEach(Player::dispose);
    }

    /**
     * Helper class that encapsulates the thread logic for getting the next moves of a bot
     * while enforcing the timeout limits. The logic must also be able to deal with rouge
     * bot code that causes exception or infinite loops.
     */
    private static final class PlayerTurnThread {

        private final Player player;
        private final UniverseView universeView;
        private final int turn;
        private final int turnTimeoutMillis;
        private final ArrayList<MovementCommand> movementCommands;
        private Thread turnThread;


        private PlayerTurnThread(Player player, UniverseView universeView, int turn, int turnTimeoutMillis) {
            this.player = Objects.requireNonNull(player);
            this.universeView = Objects.requireNonNull(universeView);
            this.turn = turn;
            this.turnTimeoutMillis = turnTimeoutMillis;
            this.movementCommands = new ArrayList<>();
        }

        private List<MovementCommand> getNextMovementCommands() {
            initialiseTurnThread();
            try {
                startTurnThread();
                waitForTurnThreadToFinish();
            } finally {
                if (hasTurnThreadTimedOut()) {
                    forciblyTerminateThread();

                    System.out.println("[WARNING] Getting moves for turn " + turn + " timed out or an error" +
                        " occurred. All further moves of this turn are ignored.");

                    // There is a "critical" moment where terminating the thread can leave the movementCommands
                    // list in an inconsistent state: when the element count has been increased but the
                    // elements themselves have not been set (see ArrayList.add() and variants). We will
                    // get rid of problems by creating a copy of the list further down, that calls .toArray().
                    // In the worst case, we will simply have the last "ghost" elements set as null (see
                    // Arrays.copyOf).
                }
            }

            return Arrays.asList(movementCommands.toArray(new MovementCommand[0]));
        }

        private void initialiseTurnThread() {
            turnThread = new Thread(() -> {
                try {
                    player.getPlayerBot().getNextCommands(universeView, movementCommands);
                } catch (SecurityException se) {
                    System.out.println("You were caught in turn " + turn + " trying to do something that is not " +
                        "allowed. *ding ding ding* Shame! Shame! - " + se.getMessage());
                } catch (Exception e) {
                    System.out.println("[ERROR] Getting moves for turn " + turn + " failed with unhandled " +
                        "exception \"" + e.getClass().getName() + "\": " + e.getMessage());
                } catch (Error e) {
                    System.out.println("[ERROR] Getting moves for turn " + turn + " failed with runtime " +
                        "error \"" + e.getClass().getName() + "\": " + e.getMessage());
                    throw e; // Errors should be passed further up
                }
            });
        }

        private void startTurnThread() {
            turnThread.start();
        }

        private void waitForTurnThreadToFinish() {
            try {
                turnThread.join(turnTimeoutMillis);
            } catch (InterruptedException ignore) {
            }
        }

        private boolean hasTurnThreadTimedOut() {
            // If the thread is still alive when calling this method, it means that it didn't finish in time and
            // is considered to be a timeout.
            return (turnThread != null) && turnThread.isAlive();
        }

        @SuppressWarnings("deprecation")
        private void forciblyTerminateThread() {
            if ((turnThread != null) && turnThread.isAlive()) {
                // The gods of deprecation will smite us, but... well, we have no control over what's going
                // on in the bot code. It could be a while(true){} for all we know, and this is neither
                // interruptable, nor can it be handled in any of the clean non-deprecated ways that involve
                // e.g. the ExecutorService or using an boolean flag in a loop.
                //
                // We have no choice then to deal with bare threads and to issue a forcible stop on it.
                // Otherwise we risk that the threads keep running in the background and eventually cause
                // the resource to deplete. We need to protect ourselves from bot code that goes haywire.

                turnThread.stop();
                turnThread = null;
            }
        }
    }
}
