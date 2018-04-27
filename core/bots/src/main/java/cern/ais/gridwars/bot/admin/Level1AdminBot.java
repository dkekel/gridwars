package cern.ais.gridwars.bot.admin;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Level 1 Admin Bot
 *
 * Was originally: cern.ais.gridwars.bot.RocketBot
 */
public class Level1AdminBot implements PlayerBot {
    private Coordinates genPoint;

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        if (genPoint == null) genPoint = universeView.getMyCells().get(0);
        for (Coordinates current : universeView.getMyCells()) {
            int dX = current.getX() - genPoint.getX(), n = universeView.getUniverseSize();
            if (dX % (n / 2) == 0) {
                if (universeView.getPopulation(current) > 15) {
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 5));
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 5));
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current) - 15));
                } else {
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)));
                }
            } else {
                if (universeView.getPopulation(current) > 10) {
                    if ((dX > 0) ^ (Math.abs(dX) > n / 2)) {
                        commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 5));
                    } else {
                        commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 5));
                    }
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current) - 10));
                } else {
                    commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)));
                }
            }
        }
    }
}
