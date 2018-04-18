/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars;

import cern.ais.gridwars.cell.Cell;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.coordinates.CoordinatesImpl;
import cern.ais.gridwars.universe.ArrayUniverse;
import cern.ais.gridwars.universe.Universe;
import cern.ais.gridwars.universe.UniverseViewImpl;
import cern.ais.gridwars.util.StdOutputSwitcher;

import java.nio.ByteBuffer;
import java.util.*;


public class Game {

    public interface TurnCallback {
        void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands, ByteBuffer binaryGameStatus);
    }

    private static final int UNIVERSE_STATE_BYTE_SIZE = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4;
    private static final byte[] EMPTY_PIXEL = { 0, 0, 0, 0 };

    private final List<Player> playerList = new LinkedList<>();
    private Iterator<Player> playerIterator;
    private final Universe universe = new ArrayUniverse();
    private final Random random = new Random();
    private final TurnCallback turnCallback;
    private final boolean debugMode;
    private int currentTurn;

    public Game(List<Player> playerList, TurnCallback turnCallback) {
        this(playerList, turnCallback, false);
    }

    public Game(List<Player> playerList, TurnCallback turnCallback, boolean debugMode) {
        this.playerList.addAll(Objects.requireNonNull(playerList));
        this.playerIterator = this.playerList.iterator();
        this.turnCallback = Objects.requireNonNull(turnCallback);
        this.debugMode = debugMode;

        if (playerList.size() < 2) {
            throw new IllegalArgumentException("Need at least two players to play a game");
        }
    }

    public Universe getUniverse() {
        return universe;
    }

    public boolean isFinished() {
        return maxTurnLimitReached() || onlyOneOrLessPlayerLeft();
    }

    private boolean maxTurnLimitReached() {
        return (currentTurn > GameConstants.TURN_LIMIT);
    }

    private boolean onlyOneOrLessPlayerLeft() {
        return (universe.getNumberOfAlivePlayers(playerList.size()) <= 1);
    }

    public Player getWinner() {
        assertIsFinished();
        boolean potentialDraw = false;
        Player winner = null;
        int winnerPopulation = -1;

        for (Player player : playerList) {
            int count = 0;

            // TODO [optimisation] use a parallel stream reduce operation here
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

    private void assertIsFinished() {
        if (!isFinished()) {
            throw new IllegalStateException("Game not finished");
        }
    }

    public void startUp() {
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
     * Computes 1 turn for the "next player". If this completes a turn round, also increases populations
     */
    public void nextTurn() {
        if (!playerIterator.hasNext()) {
            throw new RuntimeException("Bad stuff");
        }

        final Player player = playerIterator.next();

        // Check if player has lost already
    /*while (universe.getCellsForPlayer(player).isEmpty())
    {
      if (!playerIterator.hasNext())
      {
        playerIterator = playerList.iterator();
        increasePopulation = true;
      }
      player = playerIterator.next();
    }*/

        final UniverseView universeView = new UniverseViewImpl(universe, player, currentTurn);

        final List<MovementCommand> movementCommands = new ArrayList<MovementCommand>();

        // Set our own security manager to stop bots from doing nasty stuff
    /*if (!(System.getSecurityManager() instanceof GridWarsSecurityManager))
    {
      System.setSecurityManager(new GridWarsSecurityManager());
    }*/

        // TODO Refactor the code below do to it in the proper way using an Executor and completable futures
        // TODO Handle SecurityException in a special way to find out which bot cheated
        Thread playerThread = new Thread() {
            @Override
            public void run() {
                if (player.getPlayerBot() != null) {
                    player.getPlayerBot().getNextCommands(universeView, movementCommands);
                }
            }
        };

        if (player.hasBotOutputPrintWriter()) {
            StdOutputSwitcher.INSTANCE.switchToBotPrintWriter(player.getBotOutputPrintWriter());
        }

        playerThread.start();
        try {
            playerThread.join(debugMode ? 0 : GameConstants.TURN_TIMEOUT_MS);
        } catch (InterruptedException e) {
            // Bad stuff!
        }

        if (playerThread.isAlive()) {
            // The gods of deprecation will smite us in the ass, but... well, we have no control
            // over what's in the player thread. It could be a while(true){} for all we know, and
            // this is not interruptible in any of the clean non-deprecated ways.

            //System.out.println("Calling Thread.stop() on player " + player.getName());
            playerThread.stop();

            // There is a "critical" moment where stopping the thread can leave the movementCommands
            // list in an inconsistent state: when the element count has been increased but the
            // elements themselves have not been set (see ArrayList.add() and variants). We will
            // get rid of problems by creating a copy of the list further down, that calls .toArray().
            // In the worst case, we will simply have the last "ghost" elements set as null (see
            // Arrays.copyOf).
        }

        final ArrayList<MovementCommand> cleanMovementCommands = new ArrayList<>(movementCommands);

        if (validateCommands(player, cleanMovementCommands)) {
            for (MovementCommand movementCommand : cleanMovementCommands) {
                executeMovement(player, movementCommand);
            }
        }

        // TODO do we need to flush or close anything??
        StdOutputSwitcher.INSTANCE.restoreInitial();

        // Battle resolution & clean-up
        for (Cell cell : universe.getAllCells()) {
            if (!cell.isEmpty()) {
                if (!playerIterator.hasNext()) {
                    cell.growPopulation();
                } else {
                    cell.truncatePopulation();
                }
            }
        }

        turnCallback.onPlayerResponse(player, currentTurn, cleanMovementCommands, getBinaryGameStatus());

        // Increase turn counter
        currentTurn++;

        // Circular iteration
        if (!playerIterator.hasNext()) {

            // Restart the iterator
            playerIterator = playerList.iterator();
        }
    }

    private boolean validateCommands(Player player, List<MovementCommand> movementCommands) {
        HashMap<Coordinates, Integer> totalMoveOut = new HashMap<>();
        for (MovementCommand movementCommand : movementCommands) {
            if (!validateCommand(player, movementCommand)) {
                return false;
            }
            int currentValue = 0;
            if (totalMoveOut.containsKey(movementCommand.getCoordinatesFrom())) {
                currentValue = totalMoveOut.get(movementCommand.getCoordinatesFrom());
            }
            currentValue += movementCommand.getAmount();
            totalMoveOut.put(movementCommand.getCoordinatesFrom(), currentValue);
        }

        for (Map.Entry<Coordinates, Integer> entry : totalMoveOut.entrySet()) {
            if (entry.getValue() > universe.getCell(entry.getKey()).getPopulation()) {
                System.out.println("Command validation failed: Total sum of movements out of a position is higher than population ("
                    + entry.getKey().getX() + ", " + entry.getKey().getY() + ")");
                return false;
            }
        }

        return true;
    }

    private boolean validateCommand(Player player, MovementCommand movementCommand) {
        if (movementCommand == null || movementCommand.getCoordinatesFrom() == null || movementCommand.getDirection() == null) {
            System.out.println("Command failed validation: Null pointers in command." + movementCommand);
            return false;
        }
        if (!player.equals(universe.getCell(movementCommand.getCoordinatesFrom()).getOwner())) {
            System.out.println("Command failed validation: Moving cells from a cell not owned by you " + movementCommand.getCoordinatesFrom());
            return false;
        }
        if (universe.getCell(movementCommand.getCoordinatesFrom()).getPopulation() < movementCommand.getAmount()) {
            System.out.println("Command failed validation: Moving more cells than you have " + movementCommand.getCoordinatesFrom());
            return false;
        }
        if (movementCommand.getAmount() <= 0L) {
            System.out.println("Command failed validation: Negative or zero amount in command");
            return false;
        }
        return true;
    }

    private void executeMovement(Player player, MovementCommand movementCommand) {
        if (movementCommand == null)
            return;

        // Move from origin...
        universe.getCell(movementCommand.getCoordinatesFrom())
            .moveOut(player, movementCommand.getAmount());

        // ... into destination
        universe.getCell(movementCommand.getCoordinatesFrom().getRelative(1, movementCommand.getDirection()))
            .moveIn(player, movementCommand.getAmount());
    }

    private ByteBuffer getBinaryGameStatus() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(UNIVERSE_STATE_BYTE_SIZE);

        for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++) {
            for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++) {
                Cell currentCell = getUniverse().getCell(x, y);

                if (currentCell.isEmpty()) {
                    byteBuffer.put(EMPTY_PIXEL);
                } else {
                    // Red, Green, Blue
                    byteBuffer.put(GameConstants.PLAYER_COLORS[currentCell.getOwner().getColorIndex()]);

                    // Alpha channel, opacity (0-255)
                    byteBuffer.put((byte) Math.round(255 * currentCell.getPopulation() / GameConstants.MAXIMUM_POPULATION));
                }
            }
        }

        return byteBuffer;
    }
}
