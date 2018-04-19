package cern.ais.gridwars.api.command;

import cern.ais.gridwars.api.Coordinates;

import java.util.Objects;


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
        this.coordinatesFrom = Objects.requireNonNull(coordinatesFrom);
        this.direction = Objects.requireNonNull(direction);
        this.amount = amount;

        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount, you must at least move 1 troop: " + amount);
        }
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
