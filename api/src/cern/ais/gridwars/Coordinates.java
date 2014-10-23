/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars;

import cern.ais.gridwars.command.MovementCommand;

/**
 * Represents a position in the grid where the game is played.
 */
public interface Coordinates
{
  public int getX();

  public int getY();

  /**
   * Used to get a coordinate object placed in a relative distance to this one.
   * @param distance Relative distance.
   * @param direction Relative direction.
   * @return New Coordinates object representing the new position.
   */
  public Coordinates getRelative(int distance, MovementCommand.Direction direction);

  public Coordinates getLeft(int distance);

  public Coordinates getLeft();

  public Coordinates getRight(int distance);

  public Coordinates getRight();

  public Coordinates getUp(int distance);

  public Coordinates getUp();

  public Coordinates getDown(int distance);

  public Coordinates getDown();
}
