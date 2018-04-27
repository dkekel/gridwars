package cern.ais.gridwars.api;

import java.util.List;


/**
 * <p>Provides an omnipotent view of the two-dimensional game universe during a turn</p>
 *
 * <p>Instances of this view will be used by the bot code to inspect the current universe situation
 * during a turn in order to decide on the next movements. The view is <em>omnipotent</em> in
 * the sense that the populations and owners of all cells can be queried.</p>
 *
 * <p><b>IMPORTANT:</b> The <code>UniverseView</code> will be created for each turn, it is therefore important
 * to not store and reuse references to these objects between turns in the bot code.</p>
 */
public interface UniverseView {

    /**
     * @return all coordinates of the cells that belong to the current bot
     */
    List<Coordinates> getMyCells();

    /**
     * @return population at the given position (not necessarily belonging to the current bot)
     */
    int getPopulation(Coordinates coordinates);

    /**
     * @return population at the given position (not necessarily belonging to the current bot)
     */
    int getPopulation(int x, int y);

    /**
     * @return <code>true</code> if the cell at the given position has no troops in it, otherwise <code>false</code>
     */
    boolean isEmpty(Coordinates coordinates);

    /**
     * @return <code>true</code> if the cell at the given position has no troops in it, otherwise <code>false</code>
     */
    boolean isEmpty(int x, int y);

    /**
     * @return <code>true</code> if the owner of the cell at the given position belongs to the current bot,
     * otherwise <code>false</code>.
     */
    boolean belongsToMe(Coordinates coordinates);

    /**
     * @return <code>true</code> if the owner of the cell at the given position belongs to the current bot,
     * otherwise <code>false</code>.
     */
    boolean belongsToMe(int x, int y);

    /**
     * @return width/height of the universe, which is a square (width == height)
     */
    int getUniverseSize();

    /**
     * @return rate at which the population of each cell is increased at the end of a round (every 2 turns)
     */
    double getGrowthRate();

    /**
     * @return population limit for cells. Cells that grow above this will be "truncated" back down to this value.
     */
    int getMaximumPopulation();

    /**
     * @return timeout value in milliseconds for each PlayerBot.getNextCommands call. Commands added to the parameter list before the timeout are evaluated, moves added afterwards are ignored.
     */
    int getTurnTimeOutInMilliseconds();

    /**
     * @return maximum number of turns that the game will run. If this value is reached, the player with the highest total population over all cells wins.
     */
    int getTurnLimit();

    /**
     * @return current turn number.
     */
    int getCurrentTurn();

    /**
     * @return coordinates object at the specified x/y position
     */
    Coordinates getCoordinates(int x, int y);
}
