package cern.ais.gridwars;

import java.util.List;


/**
 * Provides an omnipotent view on the universe where the match is played.
 */
public interface UniverseView {

    /**
     * @return all cells that belong to the calling player
     */
    List<Coordinates> getMyCells();

    /**
     * @return population at the given position (not necessarily belonging to the calling player)
     */
    int getPopulation(Coordinates coordinates);

    /**
     * @return population at the given position (not necessarily belonging to the calling player)
     */
    int getPopulation(int x, int y);

    /**
     * @return <code>true</code> if the cell represented by the <code>Coordinates</code> object has no units in
     * it, otherwise <code>false</code>.
     */
    boolean isEmpty(Coordinates coordinates);

    /**
     * @return <code>true</code> if the cell represented by the <code>Coordinates</code> object has no units in
     * it, otherwise <code>false</code>.
     */
    boolean isEmpty(int x, int y);

    /**
     * @return <code>true</code> if the owner of the cell represented by the <code>Coordinates</code> object
     * is occupied by the calling player, otherwise <code>false</code>.
     */
    boolean belongsToMe(Coordinates coordinates);

    /**
     * @return <code>true</code> if the owner of the cell represented by the <code>Coordinates</code> object
     * is occupied by the calling player, otherwise <code>false</code>.
     */
    boolean belongsToMe(int x, int y);

    /**
     * @return the width/height of the universe (square)
     */
    int getUniverseSize();

    /**
     * @return rate at which population is increased at the end of a turn
     */
    double getGrowthRate();

    /**
     * @return population limit for cells. Cells that grow above this will be "truncated" back down to this value.
     */
    long getMaximumPopulation();

    /**
     * @return Time-out value in ms for each PlayerBot.getNextCommands call. Commands added to the parameter list before the time-out are considered as valid.
     */
    int getTurnTimeOutInMilliseconds();

    /**
     * @return Maximum number of turns that the game will run. If this value is reached, the player with the highest population sum over the board wins.
     */
    int getTurnLimit();

    /**
     * @return Current turn number.
     */
    int getCurrentTurn();

    /**
     * @return Coordinates object in the specified position
     */
    Coordinates getCoordinates(int x, int y);
}
