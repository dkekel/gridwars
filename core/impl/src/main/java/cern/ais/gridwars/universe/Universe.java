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
import cern.ais.gridwars.Player;
import cern.ais.gridwars.cell.Cell;

import java.util.Collection;
import java.util.List;


public interface Universe {

    Cell getCell(Coordinates coordinates);

    Cell getCell(int x, int y);

    List<Coordinates> getCellsForPlayer(Player player);

    void putCell(Cell cell);

    void removeCell(Coordinates coordinates);

    void removeCell(Cell cell);

    Collection<Cell> getAllCells();

    int getNumberOfAlivePlayers();
}
