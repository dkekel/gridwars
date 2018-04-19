package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


public class MovingBot implements PlayerBot {

	private final MovementCommand.Direction direction;

	public MovingBot() {
		this(MovementCommand.Direction.RIGHT);
	}

	public MovingBot(MovementCommand.Direction direction) {
		this.direction = direction;
	}

	@Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
		for (int y = 0; y < universeView.getUniverseSize(); y++) {
            for (int x = 0; x < universeView.getUniverseSize(); x++) {

                if (universeView.belongsToMe(x, y)) {
                    int population = universeView.getPopulation(x, y);

                    if (population > 1) {
                        MovementCommand movementCommand = new MovementCommand(universeView.getCoordinates(x, y),
                            direction, population / 2);
                        movementCommands.add(movementCommand);
                    }
                }
            }
        }
	}
}
