package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.lang.reflect.Method;
import java.util.List;

public class CheaterBot implements PlayerBot {

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands) {
        // Try to use reflection
        try {
            Class c = Class.forName("java.lang.String");
            Method methods[] = c.getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
