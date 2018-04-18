package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.util.Cell;

import java.util.ArrayList;
import java.util.List;

import static cern.ais.gridwars.command.MovementCommand.*;

public class SplitBot implements PlayerBot {
	private abstract class Phase {
		abstract boolean isOver();
		abstract void run(UniverseView universeView, List<MovementCommand> movementCommands);
		void start() {}
		void end() {}
	}

	private abstract class TimeLimitedPhase extends Phase {
		int duration = 1;
		TimeLimitedPhase() {}
		TimeLimitedPhase(int duration) { this.duration = duration; }

		boolean isOver() {
			return duration-- == 0;
		}
	}

	private class MovePhase extends TimeLimitedPhase {
		{ duration = 10; }
		MoveBot[] moveBots;

		@Override void run(UniverseView universeView, List<MovementCommand> movementCommands) {
			Cell center = m_Main.getCenter((Cell) null);
			if (moveBots == null) {
				moveBots = new MoveBot[] {
					startMove(center, Direction.DOWN, movementCommands),
					startMove(center, Direction.UP, movementCommands),
					startMove(center, Direction.LEFT, movementCommands),
					startMove(center, Direction.RIGHT, movementCommands),
				};
			}
			else {
				for (PlayerBot bot : moveBots)
					bot.getNextCommands(universeView, movementCommands);
			}
		}

		private MoveBot startMove(Cell center, Direction direction, List<MovementCommand> movementCommands) {
			movementCommands.add(new MovementCommand(center.coords, direction, 25));
			return new MoveBot(center.getRelative(direction), direction);
		}
	}

	private final Main m_Main;
	private PlayerBot[] bots;
	private PlayerBot bot;
	private int m_Step = 10;
	private List<Phase> phases = new ArrayList<Phase>();

	public SplitBot(Main main)
	{
		m_Main = main;
		bot = null;
		MovePhase movePhase = new MovePhase();
		phases.add(movePhase);
		phases.add(fastExpandPhase(movePhase));
		phases.add(expandPhase(movePhase));
	}

	private Phase expandPhase(MovePhase movePhase)
	{
		return new Phase() {
			FastExpandBot bot;
			@Override boolean isOver() {
				return false;
			}

			@Override void run(UniverseView universeView, List<MovementCommand> movementCommands) {
				if (bot == null) {
					Cell center = m_Main.getCenter((Cell) null);
					bot = new FastExpandBot(m_Main);
					bot.setCenter(center);
				}
				bot.getNextCommands(universeView, movementCommands);
			}
		};
	}

	Phase fastExpandPhase(final MovePhase phase) {
		return new TimeLimitedPhase(35) {
			List<FastExpandBot> bots = null;
			@Override void run(UniverseView universeView, List<MovementCommand> movementCommands) {
				if (bots == null) {
					bots = new ArrayList<FastExpandBot>();
					for (MoveBot bot : phase.moveBots) {
						if (universeView.belongsToMe(bot.head().coords)) {
							FastExpandBot fastExpandBot = new FastExpandBot(m_Main);
							fastExpandBot.setCenter(bot.head());
							bots.add(fastExpandBot);
						}
					}
				}

				for (FastExpandBot bot : bots) {
					bot.getNextCommands(universeView, movementCommands);
				}
			}
		};
	}

	@Override public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands)
	{
		Phase phase = phases.get(0);
		if (phase.isOver()) {
			phases.remove(0);
			phase = phases.size() > 0 ? phases.get(0) : null;
		}

		if (phase != null)
			phase.run(universeView, movementCommands);
	}
}
