package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public class IdleBot implements PlayerBot {
	@Override public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
		// Relax and lose
	}
}
