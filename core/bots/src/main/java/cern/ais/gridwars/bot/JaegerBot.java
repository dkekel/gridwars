package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


public class JaegerBot implements PlayerBot {

	@Override
	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
		List<Coordinates> myCells = universeView.getMyCells();

		int minPrePop = (int) Math.round(1 / (universeView.getGrowthRate() - 1));

		for (Coordinates current : myCells) {
			int currentPopulation = universeView.getPopulation(current);

			if (currentPopulation > minPrePop) {
				for (int distance = 1; distance < universeView.getUniverseSize(); distance++) {
					boolean moved = false;
					// Force move
					for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
						if (!universeView.belongsToMe(current.getRelative(distance, direction))) {
							commandList.add(new MovementCommand(current, direction, currentPopulation - minPrePop));
							moved = true;
							break;
						}
					}
					if (moved) {
						break;
					}
				}
			}
		}
	}
}
