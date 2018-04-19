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

import java.util.Objects;


final class Cell {

    private final Coordinates coordinates;
    private int population = 0;
    private Player owner;

    static Cell of(Coordinates coordinates) {
        return new Cell(coordinates);
    }

    private Cell(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    Coordinates getCoordinates() {
        return coordinates;
    }

    boolean isEmpty() {
        return population == 0;
    }

    boolean isNotEmpty() {
        return !isEmpty();
    }

    Player getOwner() {
        return owner;
    }

    boolean isOwner(Player owner) {
        return (this.owner != null) && this.owner.equals(owner);
    }

    int getPopulation() {
        return population;
    }

    void moveIn(Player player, int amount) {
        if (isOwner(player)) {
            // Move in and get comfy
            population += amount;
        } else {
            // Fight and stand your ground!
            if (amount > population) {
                population = amount - population;
                this.owner = player;
            } else {
                decreasePopulation(amount);
            }
        }
    }

    void moveOut(Player player, int amount) {
        if (!isOwner(player)) {
            throw new IllegalArgumentException("moveOut called for a player that doesn't own the cell");
        }

        decreasePopulation(amount);
    }

    private void decreasePopulation(int amount) {
        if (population < amount) {
            throw new IllegalArgumentException("decreasePopulation called on a cell with amount > population");
        }

        population -= amount;
        if (population == 0) {
            owner = null;
        }
    }

    void growPopulation() {
        population = Math.min((int) Math.round(population * GameConstants.GROWTH_RATE), GameConstants.MAXIMUM_POPULATION);
    }

    void truncatePopulation() {
        population = Math.min(population, GameConstants.MAXIMUM_POPULATION);
    }

    @Override
    public String toString() {
        return "[" + coordinates + ": " + owner + " - " + population + ']';
    }
}

