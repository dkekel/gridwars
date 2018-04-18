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
import cern.ais.gridwars.coordinates.CoordinatesImpl;
import cern.ais.gridwars.Player;
import cern.ais.gridwars.cell.Cell;

import java.util.*;


// TODO Can be deleted? Hm, maybe keep it for the time being. When there are only very few populated cells, this
// universe data structure will perform much better, but where is the sweet spot when to use the sparse and when
// to use the array universe??
public class SparseUniverse implements Universe {
    private final Map<Coordinates, Cell> cellMap = new HashMap<>();

    public Cell getCell(Coordinates coordinates) {
        Cell result = cellMap.get(coordinates);
        if (result != null) {
            return result;
        } else {
            return Cell.of(coordinates);
        }
    }

    public Cell getCell(int x, int y) {
        return getCell(CoordinatesImpl.of(x, y));
    }

    public List<Coordinates> getCellCoordinatesForPlayer(Player player) {
        List<Coordinates> result = new ArrayList<Coordinates>();

        for (Map.Entry<Coordinates, Cell> entry : cellMap.entrySet()) {
            if (player.equals(entry.getValue().getOwner())) {
                result.add(entry.getValue().getCoordinates());
            }
        }

        return result;
    }

    public void putCell(Cell cell) {
        cellMap.put(cell.getCoordinates(), cell);
    }

    public void removeCell(Coordinates coordinates) {
        cellMap.remove(coordinates);
    }

    public void removeCell(Cell cell) {
        removeCell(cell.getCoordinates());
    }

    public Collection<Cell> getAllCells() {
        return cellMap.values();
    }

    public int getNumberOfAlivePlayers(int initialPlayerNumber) {
        int count = 0;
        List<Player> alreadyCounted = new ArrayList<Player>();

        for (Cell cell : getAllCells()) {
            Player owner = cell.getOwner();
            if (owner != null && !alreadyCounted.contains(owner)) {
                count++;
                alreadyCounted.add(owner);
            }
        }

        return count;
    }
}
