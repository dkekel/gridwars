package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Simple bot that expands into all directions if there is a cell that does not belong to the bot
 */
public class ExpandBot implements PlayerBot {

    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);

            if (currentPopulation > (4.0 / (universeView.getGrowthRate() - 1))) {
                int split = 1;

                // Check left, right, up, down for cells that don't belong to me
                for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                    if (!universeView.belongsToMe(cell.getNeighbour(direction))) {
                        split++;
                    }
                }

                // Expand
                for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                    if (!universeView.belongsToMe(cell.getNeighbour(direction))) {
                        commandList.add(new MovementCommand(cell, direction, currentPopulation / split));
                    }
                }
            }
        }
    }
}
