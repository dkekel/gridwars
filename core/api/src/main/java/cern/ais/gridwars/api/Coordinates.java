package cern.ais.gridwars.api;

import cern.ais.gridwars.api.command.MovementCommand;


/**
 * <p>Represents a x/y position in the two-dimensional game universe</p>
 *
 * <p>The universe is a torus, which means that when moving out on the top, you will enter again
 * from the bottom. The same is true for left and right. When using the methods to create relative
 * <code>Coordinates</code>, this wrapping behaviour will be automatically applied. There is no need
 * to calculate the coordinates wrapping manually.</p>
 *
 * <p>Instances of this class are immutable and therefore <em>thread-safe</em>.</p>
 */
public interface Coordinates {

    /**
     * @return the x portion of the coordinates
     */
    int getX();

    /**
     * @return the y portion of the coordinates
     */
    int getY();

    /**
     * @return the coordinates relative of the current coordinates in the given distance and direction
     */
    Coordinates getRelative(int distance, MovementCommand.Direction direction);

    /**
     * @return the coordinates of the neighbour cell (distance 1) of the current coordinates in the given direction
     */
    Coordinates getNeighbour(MovementCommand.Direction direction);

    /**
     * @return the coordinates of the left neighbour cell (distance 1) of the current coordinates in the given direction
     */
    Coordinates getLeft(int distance);

    /**
     * @return the coordinates of the left neighbour cell (distance 1) of the current coordinates
     */
    Coordinates getLeft();

    /**
     * @return the coordinates of the cell right of the current coordinates in the given distance
     */
    Coordinates getRight(int distance);

    /**
     * @return the coordinates of the right neighbour cell (distance 1) of the current coordinates
     */
    Coordinates getRight();

    /**
     * @return the coordinates of the cell above the current coordinates in the given distance
     */
    Coordinates getUp(int distance);

    /**
     * @return the coordinates of the top neighbour cell (distance 1) of the current coordinates
     */
    Coordinates getUp();

    /**
     * @return the coordinates of the cell below of the current coordinates in the given distance
     */
    Coordinates getDown(int distance);

    /**
     * @return the coordinates of bottom neighbour cell (distance 1) of the current coordinates
     */
    Coordinates getDown();
}
