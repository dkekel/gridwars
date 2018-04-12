package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.List;

public class JaegerBot implements PlayerBot {

	static {
		System.out.println("Static init block was called on: cern.ais.gridwars.JaegerBot version 1");
        //System.out.println("Static init block was called on: cern.ais.gridwars.JaegerBot version 2");
	}

	public JaegerBot() {
		System.out.println("Constructor called on: " + getClass().getName() + " version 1");
        //System.out.println("Constructor called on: " + getClass().getName() + " version 2");

        System.out.println("I'm initialising, but I think I'm going to sleep for a while first ...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
            System.out.println("... zzzZZZZZzzzZZZZ, oh, I got woken up ...");
        }

        System.out.println("... ok, ok, I'll initialise already!!");
	}

	@Override
	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
		List<Coordinates> myCells = universeView.getMyCells();

		Long minPrePop = Math.round(1 / (universeView.getGrowthRate() - 1));

		for (Coordinates current : myCells) {
			Long currentPopulation = universeView.getPopulation(current);

			if (currentPopulation > minPrePop) {
				for (int distance = 1; distance < universeView.getUniverseSize(); distance++) {
					boolean moved = false;
					// Force move
					for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
						if (!universeView.belongsToMe(current.getRelative(distance, direction))) {
							commandList.add(new MovementCommand(current, direction, currentPopulation - minPrePop));
							moved = true;
							break;
						}
					}
					if (moved) {
						break;
					}
				}
			}
		}
	}
}
