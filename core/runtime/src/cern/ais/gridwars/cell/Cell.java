/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars.cell;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.GameConstants;
import cern.ais.gridwars.Player;

public class Cell
{
  protected final Coordinates coordinates;
  protected       Long        population;
  protected       Player      player;

  public Cell(Coordinates coordinates)
  {
    this(null, 0L, coordinates);
  }

  public Cell(Player player, Long population, Coordinates coordinates)
  {
    this.coordinates = coordinates;
    this.population = population;
    this.player = player;
  }

  public Coordinates getCoordinates()
  {
    return coordinates;
  }

  public boolean isEmpty()
  {
    return population == 0;
  }

  public Player getOwner()
  {
    return player;
  }

  public Long getPopulation()
  {
    return population;
  }

  public void moveIn(Player player, Long amount)
  {
    if (player.equals(getOwner()))
    {
      // Add
      population += amount;
    }
    else
    {
      // Battle!
      if (amount > population)
      {
        population = amount - population;
        this.player = player;
      }
      else
      {
        decreasePopulation(amount);
      }
    }
  }

  public void moveOut(Player player, Long amount)
  {
    if (!player.equals(getOwner()))
    {
      throw new IllegalStateException("moveOut called for a player that doesn't own the cell");
    }

    decreasePopulation(amount);
  }

  protected void decreasePopulation(Long amount)
  {
    if (population < amount)
    {
      throw new IllegalStateException("decreasePopulation called on a cell with amount > population");
    }

    population -= amount;
    if (population == 0)
    {
      player = null;
    }
  }

  public void increasePopulation()
  {
    population = Math.min(Math.round(population * GameConstants.GROWTH_RATE), GameConstants.MAXIMUM_POPULATION);
  }

  @Override
  public String toString()
  {
    return "[" + coordinates + ": " + player + " - " + population + ']';
  }

  public void populationCutOff()
  {
    population = Math.min(population, GameConstants.MAXIMUM_POPULATION);
  }
}

