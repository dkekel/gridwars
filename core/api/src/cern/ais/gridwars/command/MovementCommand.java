/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars.command;

import cern.ais.gridwars.Coordinates;

/**
 * Bean that encapsulates a movement command.
 */
public final class MovementCommand
{

  public static enum Direction
  {
    LEFT, RIGHT, UP, DOWN
  }

  final Coordinates coordinatesFrom;
  final Direction   direction;
  final Long        amount;

  public MovementCommand(Coordinates coordinatesFrom, Direction direction, Long amount)
  {
    if (amount <= 0)
    {
      throw new RuntimeException("Negative amount " + amount.toString() + " in movement command.");
    }

    this.coordinatesFrom = coordinatesFrom;
    this.direction = direction;
    this.amount = amount;
  }

  public final Coordinates getCoordinatesFrom()
  {
    return coordinatesFrom;
  }

  public final Direction getDirection()
  {
    return direction;
  }

  public final Long getAmount()
  {
    return amount;
  }

  @Override
  public String toString()
  {
    return coordinatesFrom + "->" + direction + "(" + amount + ")";
  }
}
