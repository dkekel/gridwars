package cern.ais.gridwars.api.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


/**
 * <p>Interface that must be implemented a bot class</p>
 */
public interface PlayerBot {

    /**
     * <p>Called by the game engine on every turn of the bot to get the next movement commands</p>
     *
     * <p>The <code>UniverseView</code> and an empty list of <code>MovementCommand</code> objects
     * passed. The bot code uses the <code>UniverseView</code> to get the information about the
     * current game situation of the turn to decide on the next moves. The bot is expected to
     * add the next move commands to the provided <code>movementCommands</code> list.</p>
     *
     * <p>The game engine will enforce a timeout when calling this method. The bot code has
     * 50 milliseconds to provide the movement commands. All movement commands that are added
     * to the list within this time limit are evaluated. Movements that are added after the
     * timeout will be ignored. So the bot code should make sure to add the movement commands
     * in time. The code may also track the current execution time to make sure it finishes
     * in time. But be aware that the measurement of the execution time might not correspond
     * exactly to that of the game engine. At the end, the game engine will decide on the
     * timeout.</p>
     *
     * <p><b>IMPORTANT:</b> All movement commands will be evaluated after they have been
     * submitted. If any of the movement commands is invalid (e.g. moving from a cell that doesn't
     * belong to the bot, moving more troops than the available population in a cell, etc.),
     * then all movement commands of the current turn are discarded and ignored. The bot will
     * basically just sit idle in during this turn.</p>
     *
     * @param universeView provides information about the current status of the game universe. This
     *                     object is recreated for every turn. It is therefore important <b>to not store
     *                     references to this object</b> and to not reuse is between turns.
     * @param movementCommands empty list of movement commands to which the bot is expected to add the movements
     */
    void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands);
}
