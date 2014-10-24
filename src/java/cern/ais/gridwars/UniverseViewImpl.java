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
import cern.ais.gridwars.universe.Universe;

import java.util.List;

public class UniverseViewImpl implements UniverseView
{
  private final Universe          universe;
  private final Player            player;
  private final List<Coordinates> playerOwnedCells;
  private final int               turn;

  public UniverseViewImpl(Universe universe, Player player, int turn)
  {
    this.universe = universe;
    this.player = player;
    this.turn = turn;
    playerOwnedCells = this.universe.getCellsForPlayer(this.player);
  }

  public List<Coordinates> getMyCells()
  {
    return playerOwnedCells;
  }

  public Long getPopulation(Coordinates coordinates)
  {
    return universe.getCell(coordinates).getPopulation();
  }

  public Long getPopulation(int x, int y)
  {
    return getPopulation(new CoordinatesImpl(x, y));
  }

  public boolean isEmpty(Coordinates coordinates)
  {
    return universe.getCell(coordinates).isEmpty();
  }

  public boolean isEmpty(int x, int y)
  {
    return universe.getCell(x, y).isEmpty();
  }

  public boolean belongsToMe(Coordinates coordinates)
  {
    Cell cell = universe.getCell(coordinates);
    return !cell.isEmpty() && player.equals(cell.getOwner());
  }

  public boolean belongsToMe(int x, int y)
  {
    return belongsToMe(new CoordinatesImpl(x, y));
  }


  public int getUniverseSize()
  {
    return GameConstants.UNIVERSE_SIZE;
  }

  public Double getGrowthRate()
  {
    return GameConstants.GROWTH_RATE;
  }

  public Long getMaximumPopulation()
  {
    return GameConstants.MAXIMUM_POPULATION;
  }

  public int getTimeOutInMilliseconds()
  {
    return GameConstants.TIMEOUT_DURATION_MS;
  }

  public int getTurnLimit()
  {
    return GameConstants.TURN_LIMIT;
  }

  public int getCurrentTurn()
  {
    return turn;
  }

  public Coordinates getCoordinates(int x, int y)
  {
  	return new CoordinatesImpl(x, y);
  }
}
