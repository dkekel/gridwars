package cern.ais.gridwars.api.command;

import cern.ais.gridwars.api.Coordinates;

import java.util.Objects;


/**
 * <p>Encapsulates a movement command</p>
 *
 * <p>A MovementCommand has an origin (from) cell, a direction where to move to (LEFT, RIGHT, UP, DOWN),
 * and the amount of troops to move. It represents a move into the neighbouring cell of the origin cell
 * in the given direction. So the distance of a movement command is always 1, as the bot can't jump
 * over cells.</p>
 *
 * <p>Instances of this class are immutable and therefore <em>thread-safe</em>.</p>
 */
public final class MovementCommand {

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private final Coordinates coordinatesFrom;
    private final Direction direction;
    private final int amount;

    /**
     * @param coordinatesFrom coordinates of the cell to move from (origin). Must not be <code>null</code>.
     * @param direction direction where to move to. Must not be <code>null</code>.
     * @param amount amount of troops to move out
     */
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
