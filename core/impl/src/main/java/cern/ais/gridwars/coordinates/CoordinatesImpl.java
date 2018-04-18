/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */
package cern.ais.gridwars.coordinates;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.GameConstants;
import cern.ais.gridwars.command.MovementCommand;


public class CoordinatesImpl implements Coordinates {

    private final int x;
    private final int y;

    public static CoordinatesImpl of(int x, int y) {
        return new CoordinatesImpl(x, y);
    }

    private CoordinatesImpl(int x, int y) {
        this.x = truncateToUniverseSize(x);
        this.y = truncateToUniverseSize(y);
    }

    private int truncateToUniverseSize(int coordinateValue) {
        if (coordinateValue >= GameConstants.UNIVERSE_SIZE) {
            coordinateValue = coordinateValue % GameConstants.UNIVERSE_SIZE;
        }

        while (coordinateValue < 0) {
            coordinateValue += GameConstants.UNIVERSE_SIZE;
        }

        return coordinateValue;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CoordinatesImpl getRelative(int distance, MovementCommand.Direction direction) {
        switch (direction) {
            case LEFT:
                return getLeft(distance);
            case RIGHT:
                return getRight(distance);
            case UP:
                return getUp(distance);
            case DOWN:
                return getDown(distance);
            default:
                return this;
        }
    }

    public CoordinatesImpl getLeft(int distance) {
        return new CoordinatesImpl(x - distance, y);
    }

    public CoordinatesImpl getLeft() {
        return getLeft(1);
    }

    public CoordinatesImpl getRight(int distance) {
        return new CoordinatesImpl(x + distance, y);
    }

    public CoordinatesImpl getRight() {
        return getRight(1);
    }

    public CoordinatesImpl getUp(int distance) {
        return new CoordinatesImpl(x, y - distance);
    }

    public CoordinatesImpl getUp() {
        return getUp(1);
    }

    public CoordinatesImpl getDown(int distance) {
        return new CoordinatesImpl(x, y + distance);
    }

    public CoordinatesImpl getDown() {
        return getDown(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinatesImpl that = (CoordinatesImpl) o;

        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ']';
    }
}
