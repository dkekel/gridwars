package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


public class MovingBot implements PlayerBot {
	private MovementCommand.Direction direction;

	public MovingBot() {
		this(MovementCommand.Direction.DOWN);
	}

	public MovingBot(MovementCommand.Direction direction) {
		this.direction = direction;
	}

	@Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
		for (int my = 0; my < universeView.getUniverseSize(); my++)
			for (int mx = 0; mx < universeView.getUniverseSize(); mx++) {
				if (universeView.belongsToMe(mx, my)) {
					int population = universeView.getPopulation(mx, my);
					if (population > 1) {
						movementCommands.add(new MovementCommand(universeView.getCoordinates(mx, my), direction, population / 2));
					}
				}
			}
	}
}
