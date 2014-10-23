/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;

import java.io.FileOutputStream;

public class Player
{
  final String           name;
  final PlayerBot        playerBot;
  final FileOutputStream outputStream;
  final Integer colorIndex;

  public Player(String name, PlayerBot playerBot, FileOutputStream outputStream, Integer colorIndex)
  {
    this.name = name;
    this.playerBot = playerBot;
    this.outputStream = outputStream;
    this.colorIndex = colorIndex;
  }

  public String getName()
  {
    return name;
  }

  public PlayerBot getPlayerBot()
  {
    return playerBot;
  }

  public FileOutputStream getOutputStream()
  {
    return outputStream;
  }

  public Integer getColorIndex()
  {
    return colorIndex;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Player player = (Player) o;

    return name.equals(player.name);
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }

  @Override
  public String toString()
  {
    return name;
  }
}
