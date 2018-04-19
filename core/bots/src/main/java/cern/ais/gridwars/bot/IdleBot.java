package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;

public class IdleBot implements PlayerBot {

	@Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
		// Relax and lose
	}
}
