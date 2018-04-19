package cern.ais.gridwars.api.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Must be implemented by the main bot class.
 */
public interface PlayerBot {
    /**
     * Called on every turn to get the next movement commands.
     *
     * @param universeView     Provides information about the current status of the game.
     * @param movementCommands Empty list to which the player is expected to add his movements.
     */
    void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands);
}
