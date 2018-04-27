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

import java.util.List;


interface Universe {

    Cell getCell(Coordinates coordinates);

    Cell getCell(int x, int y);

    List<Coordinates> getCellCoordinatesForPlayer(Player player);

    List<Cell> getAllCells();

    List<Cell> getAllNonEmptyCells();

    int getNumberOfAlivePlayers(int initialPlayerNumber);
}
