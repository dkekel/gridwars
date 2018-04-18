/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars.cell;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.GameConstants;
import cern.ais.gridwars.Player;

import java.util.Objects;


public final class Cell {

    private final Coordinates coordinates;
    private int population = 0;
    private Player owner;

    public static Cell of(Coordinates coordinates) {
        return new Cell(coordinates);
    }

    private Cell(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public boolean isEmpty() {
        return population == 0;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isOwner(Player owner) {
        return (this.owner != null) && this.owner.equals(owner);
    }

    public int getPopulation() {
        return population;
    }

    public void moveIn(Player player, int amount) {
        if (isOwner(player)) {
            // Add
            population += amount;
        } else {
            // Battle!
            if (amount > population) {
                population = amount - population;
                this.owner = player;
            } else {
                decreasePopulation(amount);
            }
        }
    }

    public void moveOut(Player player, int amount) {
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

    public void growPopulation() {
        population = Math.min((int) Math.round(population * GameConstants.GROWTH_RATE), GameConstants.MAXIMUM_POPULATION);
    }

    public void truncatePopulation() {
        population = Math.min(population, GameConstants.MAXIMUM_POPULATION);
    }

    @Override
    public String toString() {
        return "[" + coordinates + ": " + owner + " - " + population + ']';
    }
}

