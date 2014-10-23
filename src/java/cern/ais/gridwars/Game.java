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
import cern.ais.gridwars.universe.ArrayUniverse;
import cern.ais.gridwars.universe.Universe;
import cern.ais.gridwars.util.OutputSwitcher;

import java.nio.ByteBuffer;
import java.util.*;

public class Game
{
  private static final byte[] emptyPixel = {0, 0, 0, 0};
  protected final List<Player> playerList;
  protected final Universe universe = new ArrayUniverse();
  protected final TurnCallback     turnCallback;
  protected       Iterator<Player> playerIterator;
  protected       int              currentTurn;

  public Game(List<Player> playerList, TurnCallback turnCallback)
  {
    this.playerList = playerList;
    this.turnCallback = turnCallback;
    playerIterator = this.playerList.iterator();
  }

  public List<Player> getPlayerList()
  {
    return playerList;
  }

  public Universe getUniverse()
  {
    return universe;
  }

  public boolean done()
  {
    return universe.getNumberOfAlivePlayers() <= 1 || currentTurn > GameConstants.TURN_LIMIT;
  }

  public Player getWinner()
  {
    checkDone();

    boolean draw = false;

    // Compute each player's total population, return the one with the highest
    Player winner = null;
    Long winnerPopulation = -1L;

    for (Player player : playerList)
    {
      Long count = 0L;
      for (Coordinates coordinates : universe.getCellsForPlayer(player))
      {
        count += universe.getCell(coordinates).getPopulation();
      }

      if (count > winnerPopulation)
      {
        winnerPopulation = count;
        winner = player;
        draw = false;
      }
      else if (count.equals(winnerPopulation))
      {
        // Potential draw
        draw = true;
      }
    }

    if (draw)
      return null;
    else
      return winner;
  }

  private void checkDone()
  {
    if (!done())
    {
      throw new IllegalStateException("Game not done.");
    }
  }

  private void shufflePlayerList()
  {
    // Random shuffle
    Collections.shuffle(playerList);
  }

  public void startUp()
  {
    shufflePlayerList();
    for (Player player : playerList)
    {
      // Initialize player starting cell
      CoordinatesImpl coordinates = new CoordinatesImpl(Math.round((float) Math.random() * (GameConstants.UNIVERSE_SIZE - 1)),
              Math.round((float) Math.random() * (GameConstants.UNIVERSE_SIZE - 1)));

      // Make sure no two players start in the same position
      // TODO: make this fair in case of 2+ players, etc
      while (!universe.getCell(coordinates).isEmpty())
      {
        coordinates = new CoordinatesImpl(Math.round((float) Math.random() * (GameConstants.UNIVERSE_SIZE - 1)),
                Math.round((float) Math.random() * (GameConstants.UNIVERSE_SIZE - 1)));
      }

      universe.getCell(coordinates).moveIn(player, GameConstants.STARTING_POPULATION);

    }
    currentTurn = 1;
  }

  /**
   * Computes 1 turn for the "next player". If this completes a turn round, also increases populations
   */
  public void nextTurn()
  {
    boolean increasePopulation = false;

    if (!playerIterator.hasNext())
    {
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

    Thread playerThread = new Thread()
    {
      @Override
      public void run()
      {
        if (player.getPlayerBot() != null)
        {
          player.getPlayerBot().getNextCommands(universeView, movementCommands);
        }
      }
    };

    if (player.getOutputStream() != null)
    {
      OutputSwitcher.getInstance().switchToFile(player.getOutputStream());
    }

    playerThread.start();
    try
    {
      playerThread.join(GameConstants.TIMEOUT_DURATION_MS);
    }
    catch (InterruptedException e)
    {
      // Bad stuff!
    }

    if (playerThread.isAlive())
    {
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

    final ArrayList<MovementCommand> cleanMovementCommands = new ArrayList<MovementCommand>(movementCommands);

    if (validateCommands(player, cleanMovementCommands))
    {
      for (MovementCommand movementCommand : cleanMovementCommands)
      {
        executeMovement(player, movementCommand);
      }
    }

    OutputSwitcher.getInstance().restoreInitial();

    // Battle resolution & clean-up
    for (Cell cell : universe.getAllCells())
    {
      if (!cell.isEmpty())
      {
        if (!playerIterator.hasNext() || increasePopulation)
        {
          cell.increasePopulation();
        }
        else
        {
          cell.populationCutOff();
        }
      }
    }

    turnCallback.onPlayerResponse(player, currentTurn, cleanMovementCommands, getBinaryGameStatus());

    // Increase turn counter
    currentTurn++;

    // Circular iteration
    if (!playerIterator.hasNext())
    {

      // Restart the iterator
      playerIterator = playerList.iterator();
    }
  }

  private boolean validateCommands(Player player, List<MovementCommand> movementCommands)
  {
    HashMap<Coordinates, Long> totalMoveOut = new HashMap<Coordinates, Long>();
    for (MovementCommand movementCommand : movementCommands)
    {
      if (!validateCommand(player, movementCommand))
      {
        return false;
      }
      Long currentValue = 0L;
      if (totalMoveOut.containsKey(movementCommand.getCoordinatesFrom()))
      {
        currentValue = totalMoveOut.get(movementCommand.getCoordinatesFrom());
      }
      currentValue += movementCommand.getAmount();
      totalMoveOut.put(movementCommand.getCoordinatesFrom(), currentValue);
    }

    for (Map.Entry<Coordinates, Long> entry : totalMoveOut.entrySet())
    {
      if (entry.getValue() > universe.getCell(entry.getKey()).getPopulation())
      {
        System.out.println("Command validation failed: Total sum of movements out of a position is higher than population ("
                + entry.getKey().getX() + ", " + entry.getKey().getY() + ")");
        return false;
      }
    }

    return true;
  }

  private boolean validateCommand(Player player, MovementCommand movementCommand)
  {
    if (movementCommand == null || movementCommand.getCoordinatesFrom() == null || movementCommand.getAmount() == null || movementCommand.getDirection() == null)
    {
      System.out.println("Command failed validation: Null pointers in command");
      return false;
    }
    if (!player.equals(universe.getCell(movementCommand.getCoordinatesFrom()).getOwner()))
    {
      System.out.println("Command failed validation: Moving cells from a cell not owned by you ("
              + movementCommand.getCoordinatesFrom().getX() + ", " + movementCommand.getCoordinatesFrom().getY());
      return false;
    }
    if (universe.getCell(movementCommand.getCoordinatesFrom()).getPopulation() < movementCommand.getAmount())
    {
      System.out.println("Command failed validation: Moving more cells than you have ("
              + movementCommand.getCoordinatesFrom().getX() + ", " + movementCommand.getCoordinatesFrom().getY());
      return false;
    }
    if (movementCommand.getAmount() <= 0L)
    {
      System.out.println("Command failed validation: Negative or zero amount in command");
      return false;
    }
    return true;
  }

  public void executeMovement(Player player, MovementCommand movementCommand)
  {
    if (movementCommand == null)
      return;

    // Move from origin...
    universe.getCell(movementCommand.getCoordinatesFrom())
            .moveOut(player, movementCommand.getAmount());

    // ... into destination
    universe.getCell(movementCommand.getCoordinatesFrom().getRelative(1, movementCommand.getDirection()))
            .moveIn(player, movementCommand.getAmount());
  }

  public ByteBuffer getBinaryGameStatus()
  {
    ByteBuffer byteBuffer = ByteBuffer.allocate(GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE * 4);

    for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++)
    {
      for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++)
      {
        Cell currentCell = getUniverse().getCell(x, y);

        if (currentCell.isEmpty())
        {
          byteBuffer.put(emptyPixel);
        }
        else
        {
          // Red, Green, Blue
          byteBuffer.put(GameConstants.PLAYER_COLORS[currentCell.getOwner().getColorIndex()]);

          // Alpha channel, opacity (0-255)
          byteBuffer.put((byte) Math.round(255 * currentCell.getPopulation().doubleValue() / GameConstants.MAXIMUM_POPULATION));
        }
      }
    }

    return byteBuffer;
  }

  @Override
  public String toString()
  {
    String result = "Game status:\n";
    char[] symbols = {'x', 'o', '+', '/', '*'};

    for (int y = GameConstants.UNIVERSE_SIZE - 1; y >= 0; y--)
    {
      for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++)
      {
        final Cell cell = universe.getCell(new CoordinatesImpl(x, y));
        result += (cell.isEmpty()) ? "  " : symbols[cell.getOwner().getColorIndex()] + " ";
      }
      result += '\n';
    }

    return result;
  }

  public interface TurnCallback
  {
    void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands, ByteBuffer binaryGameStatus);
  }
}
