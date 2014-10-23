/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package cern.ais.gridwars.bot;

import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public interface PlayerBot
{
  /**
   * This method is called on every turn for this player in order to get its movement commands.
   * @param universeView Provides information about the current status of the game.
   * @param movementCommands Empty list to which the player is expected to add his movements.
   */
  void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands);
}
