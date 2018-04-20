package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.List;


public class SnoozeBot implements PlayerBot {

    public SnoozeBot() {
        try {
            System.out.println("I think I'm going to sleep for a while, I'm tired...");
            Thread.sleep(5000);
            System.out.println("... I woke up and will have a beer now!");
        } catch (InterruptedException e) {
            System.out.println("... I got woken up and need to go to work now, meeeh.");
        }
    }

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
        // Do nothing...
    }
}
