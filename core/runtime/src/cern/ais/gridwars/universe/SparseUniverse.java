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
import cern.ais.gridwars.Player;
import cern.ais.gridwars.cell.Cell;

import java.util.*;

public class SparseUniverse implements Universe
{
  final Map<Coordinates, Cell> cellMap = new HashMap<Coordinates, Cell>();

  public Cell getCell(Coordinates coordinates)
  {
    Cell result = cellMap.get(coordinates);
    if (result != null)
    {
      return result;
    }
    else
    {
      return new Cell(coordinates);
    }
  }

  public Cell getCell(int x, int y)
  {
    return getCell(new CoordinatesImpl(x, y));
  }

  public List<Coordinates> getCellsForPlayer(Player player)
  {
    List<Coordinates> result = new ArrayList<Coordinates>();

    for (Map.Entry<Coordinates, Cell> entry : cellMap.entrySet())
    {
      if (player.equals(entry.getValue().getOwner()))
      {
        result.add(entry.getValue().getCoordinates());
      }
    }

    return result;
  }

  public void putCell(Cell cell)
  {
    cellMap.put(cell.getCoordinates(), cell);
  }

  public void removeCell(Coordinates coordinates)
  {
    cellMap.remove(coordinates);
  }

  public void removeCell(Cell cell)
  {
    removeCell(cell.getCoordinates());
  }

  public Collection<Cell> getAllCells()
  {
    return cellMap.values();
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
