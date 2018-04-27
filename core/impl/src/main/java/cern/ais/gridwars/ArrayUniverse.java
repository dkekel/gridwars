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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


final class ArrayUniverse implements Universe {

    private static final int CELL_LIST_LENGTH = GameConstants.UNIVERSE_SIZE * GameConstants.UNIVERSE_SIZE;

    private final List<Cell> cells = new ArrayList<>(CELL_LIST_LENGTH);

    ArrayUniverse() {
        for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++) {
            for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++) {
                cells.add(Cell.of(CoordinatesImpl.of(x, y)));
            }
        }
    }

    @Override
    public Cell getCell(Coordinates coordinates) {
        return getCell(coordinates.getX(), coordinates.getY());
    }

    @Override
    public Cell getCell(int x, int y) {
        try {
            return cells.get(calculateCellIndex(x, y));
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Access to non-existing cell at [" + x + ", " + y + "]");
        }
    }

    @Override
    public List<Coordinates> getCellCoordinatesForPlayer(Player player) {
        // TODO [optimisation] would a parallel stream be faster here??
        return cells.stream()
            .filter(cell -> cell.isOwner(player))
            .map(Cell::getCoordinates)
            .collect(Collectors.toList());
    }

    private int calculateCellIndex(int x, int y) {
        return y * GameConstants.UNIVERSE_SIZE + x;
    }

    @Override
    public List<Cell> getAllCells() {
        // TODO better to return a copy?
        return cells;
    }

    @Override
    public List<Cell> getAllNonEmptyCells() {
        return cells.stream().filter(Cell::isNotEmpty).collect(Collectors.toList());
    }

    @Override
    public int getNumberOfAlivePlayers(int initialPlayerNumber) {
        final List<Player> alreadyCountedPlayers = new ArrayList<>(initialPlayerNumber);

        for (Cell cell : cells) {
            if (!cell.isEmpty()) {
                Player owner = cell.getOwner();

                if ((owner != null) && !alreadyCountedPlayers.contains(owner)) {
                    alreadyCountedPlayers.add(owner);
                }

                // Optimisation to return early when we already found the expected number of alive player. We don't
                // need to continue hitchhiking through the whole galaxy.
                if (alreadyCountedPlayers.size() == initialPlayerNumber) {
                    return alreadyCountedPlayers.size();
                }
            }
        }

        return alreadyCountedPlayers.size();
    }
}
