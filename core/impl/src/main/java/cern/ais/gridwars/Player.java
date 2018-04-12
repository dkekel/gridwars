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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Player {
  final PlayerBot        playerBot;
	final File outputFile;
	final FileOutputStream outputStream;
  final Integer colorIndex;
  final long id;

  public Player(long id, PlayerBot playerBot, File outputFile, Integer colorIndex) throws FileNotFoundException {
    this.id = id;
    this.playerBot = playerBot;
    this.outputFile = outputFile;
    this.outputStream = outputFile != null ? new FileOutputStream(outputFile) : null;
    this.colorIndex = colorIndex;
  }

  public long getId()
  {
    return id;
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

    return id == player.id;
  }

  @Override
  public int hashCode()
  {
    return Long.valueOf(id).hashCode();
  }

  @Override
  public String toString()
  {
    return String.valueOf(id);
  }
}
