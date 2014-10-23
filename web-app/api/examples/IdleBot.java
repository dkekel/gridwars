/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package example.bot;

import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public class IdleBot implements PlayerBot
{
  public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands)
  {
    System.out.println("I'm idling and I like it.");
  }
}
