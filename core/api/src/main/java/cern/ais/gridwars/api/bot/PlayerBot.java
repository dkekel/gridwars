package cern.ais.gridwars.api.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * Interface that must be implemented by all bot classes.
 */
public interface PlayerBot {

    /**
     * Called on every turn to get the next movement commands of the bot.
     *
     * TODO improve documentation, timeout etc.
     *
     * @param universeView provides information about the current status of the game universe
     * @param movementCommands empty list to which the bot is expected to add the movements
     */
    void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands);
}
