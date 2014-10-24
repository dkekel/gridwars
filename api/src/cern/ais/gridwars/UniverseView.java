/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars;

import java.util.List;

public interface UniverseView
{
  /**
   *
   * @return List of coordinates where the caller has cells.
   */
  public List<Coordinates> getMyCells();

  /**
   *
   * @param coordinates
   * @return Current population at the given position (not necessarily belonging to the calling player).
   */
  public Long getPopulation(Coordinates coordinates);

  /**
   *
   * @param x
   * @param y
   * @return Current population at the given position (not necessarily belonging to the calling player).
   */
  public Long getPopulation(int x, int y);

  /**
   *
   * @param coordinates
   * @return True if the cell represented by the Coordinates object has no units in it.
   */
  public boolean isEmpty(Coordinates coordinates);

  /**
   *
   * @param x
   * @param y
   * @return True if the cell represented by the Coordinates object has no units in it.
   */
  public boolean isEmpty(int x, int y);

  /**
   *
   * @param coordinates
   * @return True if the owner of the cell represented by the Coordinates object is occupied by the calling player.
   */
  public boolean belongsToMe(Coordinates coordinates);

  /**
   *
   * @param x
   * @param y
   * @return True if the owner of the cell represented by the Coordinates object is occupied by the calling player.
   */
  public boolean belongsToMe(int x, int y);

  /**
   *
   * @return Width/height of the universe
   */
  public int getUniverseSize();

  /**
   *
   * @return Rate at which population is increased at the end of a round of turns.
   */
  public Double getGrowthRate();

  /**
   *
   * @return Population limit for cells. Cells that grow above this will be "truncated" back to this value.
   */
  public Long getMaximumPopulation();

  /**
   *
   * @return Time-out value in ms for each PlayerBot.getNextCommands call. Commands added to the parameter list before the time-out are considered as valid.
   */
  public int getTimeOutInMilliseconds();

  /**
   *
   * @return Maximum number of turns that the game will run. If this value is reached, the player with the highest population sum over the board wins.
   */
  public int getTurnLimit();

  /**
   *
   * @return Current turn number.
   */
  public int getCurrentTurn();

  /**
   *
   * @param x
   * @param y
   * @return Coordinates object in the specified position
   */
  public Coordinates getCoordinates(int x, int y);
}
