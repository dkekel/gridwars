package cern.ais.gridwars;

import cern.ais.gridwars.command.MovementCommand;


/**
 * <p>Represents a position in the universe. All function return new <code>Coordinates</code> objects
 * relative to the current <code>Coordinates</code> object.</p>
 *
 * <p>Instances of this class are immutable and therefore thread-safe.</p>
 */
public interface Coordinates {

    int getX();

    int getY();

    Coordinates getRelative(int distance, MovementCommand.Direction direction);

    Coordinates getNeighbour(MovementCommand.Direction direction);

    Coordinates getLeft(int distance);

    Coordinates getLeft();

    Coordinates getRight(int distance);

    Coordinates getRight();

    Coordinates getUp(int distance);

    Coordinates getUp();

    Coordinates getDown(int distance);

    Coordinates getDown();
}
