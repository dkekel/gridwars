package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Simple bot that only moves into one direction
 */
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
	    List<Coordinates> myCells = universeView.getMyCells();

	    for (Coordinates cell : myCells) {
	        int population = universeView.getPopulation(cell);

	        if (population > 1) {
	            MovementCommand movementCommand = new MovementCommand(cell, direction, population / 2);
	            movementCommands.add(movementCommand);
            }
        }
	}
}
