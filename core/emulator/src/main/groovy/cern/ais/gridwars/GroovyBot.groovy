package cern.ais.gridwars

import cern.ais.gridwars.bot.PlayerBot
import cern.ais.gridwars.command.MovementCommand
import cern.ais.gridwars.command.MovementCommand.Direction
import groovy.transform.CompileStatic
import groovy.util.logging.Log

/**
 * Idea of this bot is to spread from horizontal line(baseline), corresponding to initial position, mostly - up and down.
 * 5 troops should stay, and 5 troops move left & right along baseline.
 * 
 * Nothing fancy, just illustration of Groovy usage.
 * Points of interest: 
 *   1. Closure "move" with logging of actual move data.
 *   2. CompileStatic as alternative compilation mode, more strict, but giving higher performance.
 *   3. @Log annotation. (injects 'log' field of java.util.Logging type).
 *   4. implicit return in migration(Up|Down)Amount methods.
 */
@Log
@CompileStatic
class GroovyBot implements PlayerBot {
	int baseline = -1;

	@Override void getNextCommands(UniverseView universeView, List<MovementCommand> list) {
		if (baseline == -1)
			baseline = universeView.myCells.first().y

		log.warning("Turn $universeView.currentTurn")
		def move = { Coordinates from, long amount, Direction dir ->
			def pop = universeView.getPopulation(from)
			amount = Math.min(pop, amount) // Correct amount, to avoid error.
			if (amount == 0L)
				return // Moving 0 troops is an invalid move.

			def movementCommand = new MovementCommand(from, dir, amount)
			list.add(movementCommand)
			log.warning("Moving $movementCommand")
			pop - amount // Return population left
		}

		int sidemove = 10
		int stay = 5
		universeView.myCells.each { Coordinates it ->
			def population = universeView.getPopulation(it)

			if (it.y == baseline) {
				long spreadPopulation = population - sidemove - stay
				if (spreadPopulation > 10) {
					move(it, spreadPopulation / 2 as long, Direction.DOWN)
				}

				if (spreadPopulation > 5) {
					move(it, spreadPopulation / 2 as long, Direction.UP)
				}

				move(it, (sidemove / 2) as long , Direction.LEFT)
				move(it, (sidemove / 2) as long, Direction.RIGHT)
			}
			else {
				move(it, migrationUpAmount(universeView, it), Direction.UP)
				move(it, migrationDownAmount(universeView, it), Direction.DOWN)
			}
		}
	}

	long migrationUpAmount(UniverseView universeView, Coordinates from) {
		from.y > baseline ? 0L : universeView.getPopulation(from) - 5
	}

	long migrationDownAmount(UniverseView universeView, Coordinates from) {
		from.y < baseline ? 0L : universeView.getPopulation(from) - 5
	}
}
