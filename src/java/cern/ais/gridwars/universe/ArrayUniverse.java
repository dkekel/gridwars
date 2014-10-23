/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars.universe;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.CoordinatesImpl;
import cern.ais.gridwars.GameConstants;
import cern.ais.gridwars.Player;
import cern.ais.gridwars.cell.Cell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayUniverse implements Universe
{

  List<Cell> cells = new ArrayList<Cell>(GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE);

  public ArrayUniverse()
  {
    for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++)
    {
      for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++)
      {
        cells.add(new Cell(new CoordinatesImpl(x, y)));
      }
    }
  }

  public Cell getCell(Coordinates coordinates)
  {
    return getCell(coordinates.getX(), coordinates.getY());
  }

  public Cell getCell(int x, int y)
  {
    Cell cell = cells.get(y * GameConstants.UNIVERSE_SIZE + x);
    if (cell == null)
    {
      throw new IllegalStateException("Access to non-initialized cell at [" + x + ", " + y + "]");
    }
    return cell;
  }

  public List<Coordinates> getCellsForPlayer(Player player)
  {
    List<Coordinates> result = new ArrayList<Coordinates>();

    for (Cell cell : cells)
    {
      if (player.equals(cell.getOwner()))
      {
        result.add(cell.getCoordinates());
      }
    }

    return result;
  }

  public void putCell(Cell cell)
  {
    cells.set(cell.getCoordinates().getY() * GameConstants.UNIVERSE_SIZE + cell.getCoordinates().getX(), cell);
  }

  public void removeCell(Coordinates coordinates)
  {
    if (!cells.get(coordinates.getY() * GameConstants.UNIVERSE_SIZE + coordinates.getX()).isEmpty())
    {
      cells.set(coordinates.getY() * GameConstants.UNIVERSE_SIZE + coordinates.getX(), new Cell(coordinates));
    }
  }

  public void removeCell(Cell cell)
  {
    removeCell(cell.getCoordinates());
  }

  public Collection<Cell> getAllCells()
  {
    return cells;
  }

  public int getNumberOfAlivePlayers()
  {
    int count = 0;
    List<Player> alreadyCounted = new ArrayList<Player>();

    for (Cell cell : getAllCells())
    {
      Player owner = cell.getOwner();
      if (owner != null && !alreadyCounted.contains(owner))
      {
        count++;
        alreadyCounted.add(owner);
      }
    }

    return count;
  }
}
