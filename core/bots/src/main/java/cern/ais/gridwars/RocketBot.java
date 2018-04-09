package cern.ais.gridwars;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public class RocketBot implements PlayerBot
{
	Coordinates genPoint;
	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
		if  (genPoint == null) genPoint = universeView.getMyCells().get(0);
		for (Coordinates current : universeView.getMyCells()) {
			int dX = current.getX() - genPoint.getX(), n = universeView.getUniverseSize();
			if (dX%(n/2)==0) {
				if (universeView.getPopulation(current) > 15 ) {
					commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 5L));
					commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 5L));
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)-15L));
				} else {
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)));
				}
			} else {
				if (universeView.getPopulation(current) > 10 ) {
					if ((dX>0)^(Math.abs(dX)>n/2)) {
						commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 5L));
					} else {
						commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 5L));
					}
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)-10));
				} else {
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)));
				}
			}
		}
	}
}
