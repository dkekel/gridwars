package cern.ais.gridwars.bot;

import cern.ais.gridwars.UniverseView;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;


/**
 * Must be implements by the main bot class.
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
