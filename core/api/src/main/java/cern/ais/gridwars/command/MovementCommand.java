package cern.ais.gridwars.command;

import cern.ais.gridwars.Coordinates;


/**
 * Encapsulates a movement command.
 */
public final class MovementCommand {

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private final Coordinates coordinatesFrom;
    private final Direction direction;
    private final int amount;

    public MovementCommand(Coordinates coordinatesFrom, Direction direction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Negative amount in movement command: " + amount);
        }

        this.coordinatesFrom = coordinatesFrom;
        this.direction = direction;
        this.amount = amount;
    }

    public final Coordinates getCoordinatesFrom() {
        return coordinatesFrom;
    }

    public final Direction getDirection() {
        return direction;
    }

    public final int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return coordinatesFrom + " -> " + direction + " (" + amount + ")";
    }
}
