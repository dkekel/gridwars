package cern.ais.gridwars;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;

class InitTimeoutIdleBot implements PlayerBot {

    private final String originalBotClassName;
    private final int allowedTimeMillis;
    private boolean printedWarning = false;

    InitTimeoutIdleBot(String originalBotClassName, int allowedTimeMillis) {
        this.originalBotClassName = originalBotClassName;
        this.allowedTimeMillis = allowedTimeMillis;
    }

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
        if (!printedWarning) {
            System.out.println("Bot \"" + originalBotClassName + "\" failed to initialise within the allowed time" +
                " of " + allowedTimeMillis + " ms and will idle for the rest of the match. You snooze, you lose!");
            printedWarning = true;
        }
    }
}
