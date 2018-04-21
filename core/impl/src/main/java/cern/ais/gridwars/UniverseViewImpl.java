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

import java.util.Collections;
import java.util.List;
import java.util.Objects;


class UniverseViewImpl implements UniverseView {

    private final Universe universe;
    private final Player player;
    private final List<Coordinates> playerOwnedCells;
    private final int turn;

    UniverseViewImpl(Universe universe, Player player, int turn) {
        this.universe = Objects.requireNonNull(universe);
        this.player = Objects.requireNonNull(player);
        this.turn = turn;
        // TODO [optimisation] the list of player cells is eagerly initialised here, could it be lazily initialised
        // in the getter method?
        this.playerOwnedCells = Collections.unmodifiableList(universe.getCellCoordinatesForPlayer(player));
    }

    @Override
    public List<Coordinates> getMyCells() {
        return playerOwnedCells;
    }

    @Override
    public int getPopulation(Coordinates coordinates) {
        return universe.getCell(coordinates).getPopulation();
    }

    @Override
    public int getPopulation(int x, int y) {
        return universe.getCell(x, y).getPopulation();
    }

    @Override
    public boolean isEmpty(Coordinates coordinates) {
        return universe.getCell(coordinates).isEmpty();
    }

    @Override
    public boolean isEmpty(int x, int y) {
        return universe.getCell(x, y).isEmpty();
    }

    @Override
    public boolean belongsToMe(Coordinates coordinates) {
        return belongsToMe(coordinates.getX(), coordinates.getY());
    }

    @Override
    public boolean belongsToMe(int x, int y) {
        Cell cell = universe.getCell(x, y);
        return !cell.isEmpty() && cell.isOwner(player);
    }

    @Override
    public int getUniverseSize() {
        return GameConstants.UNIVERSE_SIZE;
    }

    @Override
    public double getGrowthRate() {
        return GameConstants.GROWTH_RATE;
    }

    @Override
    public int getMaximumPopulation() {
        return GameConstants.MAXIMUM_POPULATION;
    }

    @Override
    public int getTurnTimeOutInMilliseconds() {
        return GameConstants.TURN_TIMEOUT_MS;
    }

    @Override
    public int getTurnLimit() {
        return GameConstants.TURN_LIMIT;
    }

    @Override
    public int getCurrentTurn() {
        return turn;
    }

    @Override
    public Coordinates getCoordinates(int x, int y) {
        return CoordinatesImpl.of(x, y);
    }
}
