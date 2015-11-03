package cern.ais.gridwars;

import cern.ais.gridwars.Coordinates;
import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public class ThreeFishBot implements PlayerBot {
	Coordinates start;
	int i=18;
	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
		if  (i == 18) {
			start = universeView.getMyCells().get(0);
			commandList.add(new MovementCommand(start, MovementCommand.Direction.LEFT, 45L));
			commandList.add(new MovementCommand(start, MovementCommand.Direction.RIGHT, 45L));
			i--;
		} else if (i>0) {
			for (Coordinates current : universeView.getMyCells()) {
				int dX=current.getX()-start.getX(),dY=current.getY()-start.getY();
				if (dX==0) {
					if (universeView.getPopulation(current) > 5 ) {
						commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)-5L));
					}
				} else if (dY==0) {
					if ((dX>0)^(Math.abs(dX)<25)) {
						if (universeView.belongsToMe(current.getLeft())) {
							if (universeView.getPopulation(current) > 5 ) {
								commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, universeView.getPopulation(current)-5L));
							}
						} else if (universeView.getPopulation(current) > 44) {
							commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 45L));
						} else {
							commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, universeView.getPopulation(current)));
						}
					} else {
						if (universeView.belongsToMe(current.getRight())) {
							if (universeView.getPopulation(current) > 5 ) {
								commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, universeView.getPopulation(current)-5L));
							}
						} else if (universeView.getPopulation(current) > 44) {
							commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 45L));
						} else {
							commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, universeView.getPopulation(current)));
						}
					}
				}
			}
			i--;
		} else {
			for (Coordinates current : universeView.getMyCells()) {
				if (universeView.getPopulation(current) > 15 ) {
					commandList.add(new MovementCommand(current, MovementCommand.Direction.LEFT, 5L));
					commandList.add(new MovementCommand(current, MovementCommand.Direction.RIGHT, 5L));
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)-15L));
				} else {
					commandList.add(new MovementCommand(current, MovementCommand.Direction.UP, universeView.getPopulation(current)));
				}
			}
		}
	}
}
