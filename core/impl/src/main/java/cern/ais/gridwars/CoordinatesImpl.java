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

public class CoordinatesImpl implements Coordinates
{
  final int x;
  final int y;

  public CoordinatesImpl(int x, int y)
  {
    if (x >= GameConstants.UNIVERSE_SIZE)
    {
      x = x % GameConstants.UNIVERSE_SIZE;
    }

    while (x < 0)
    {
      x += GameConstants.UNIVERSE_SIZE;
    }

    if (y >= GameConstants.UNIVERSE_SIZE)
    {
      y = y % GameConstants.UNIVERSE_SIZE;
    }

    while (y < 0)
    {
      y += GameConstants.UNIVERSE_SIZE;
    }

    this.x = x;
    this.y = y;
  }

  public int getX()
  {
    return x;
  }

  public int getY()
  {
    return y;
  }

  public CoordinatesImpl getRelative(int distance, MovementCommand.Direction direction)
  {
    switch (direction)
    {
      case LEFT:
        return getLeft(distance);
      case RIGHT:
        return getRight(distance);
      case UP:
        return getUp(distance);
      case DOWN:
        return getDown(distance);
      default:
        return this;
    }
  }

  public CoordinatesImpl getLeft(int distance)
  {
    return new CoordinatesImpl(x - distance, y);
  }

  public CoordinatesImpl getLeft()
  {
    return getLeft(1);
  }

  public CoordinatesImpl getRight(int distance)
  {
    return new CoordinatesImpl(x + distance, y);
  }

  public CoordinatesImpl getRight()
  {
    return getRight(1);
  }

  public CoordinatesImpl getUp(int distance)
  {
    return new CoordinatesImpl(x, y - distance);
  }

  public CoordinatesImpl getUp()
  {
    return getUp(1);
  }

  public CoordinatesImpl getDown(int distance)
  {
    return new CoordinatesImpl(x, y + distance);
  }

  public CoordinatesImpl getDown()
  {
    return getDown(1);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CoordinatesImpl that = (CoordinatesImpl) o;

    return x == that.x && y == that.y;
  }

  @Override
  public int hashCode()
  {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ')';
  }
}
