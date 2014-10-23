/*
 * Copyright (C) CERN 2013 - European Laboratory for Particle Physics
 * All Rights Reserved.
 *
 * Authors:
 *   Dmitry Kekelidze (dmitry.kekelidze@cern.ch)
 *   Gerardo Lastra (gerardo.lastra@cern.ch)
 */

package example.bot;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.lang.Long;import java.util.List;

public class ExpandBot implements PlayerBot
{
  public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList)
  {
    List<Coordinates> myCells = universeView.getMyCells();

    for (Coordinates current : myCells)
    {
      Long currentPopulation = universeView.getPopulation(current);

      if (currentPopulation > (4.0 / (universeView.getGrowthRate() - 1)))
      {

        int split = 1;

        // Check left, right, up, down for own cells
        for (MovementCommand.Direction direction : MovementCommand.Direction.values())
        {
          if (!universeView.belongsToMe(current.getRelative(1, direction)))
          {
            split++;
          }
        }

        // Expand
        for (MovementCommand.Direction direction : MovementCommand.Direction.values())
        {
          if (!universeView.belongsToMe(current.getRelative(1, direction)))
          {
            commandList.add(new MovementCommand(current, direction, currentPopulation / split));
          }
        }
      }
    }
  }
}
