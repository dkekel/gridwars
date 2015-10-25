package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.util.Cell;

import java.util.List;

import static cern.ais.gridwars.command.MovementCommand.*;

public class SplitBot implements PlayerBot
{

	private final Main m_Main;
	private PlayerBot[] bots;
	private PlayerBot bot;
	private int m_Step = 10;

	public SplitBot(Main main)
	{
		m_Main = main;
		bot = null;
	}

	@Override public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands)
	{
		Cell center = m_Main.getCenter((Cell) null);
		if (bots == null) // first turn
		{
			bots = new PlayerBot[]{
				new MoveBot(center.getRelative(Direction.DOWN),Direction.DOWN),
				new MoveBot(center.getRelative(Direction.UP),Direction.UP),
				new MoveBot(center.getRelative(Direction.LEFT),Direction.LEFT),
				new MoveBot(center.getRelative(Direction.RIGHT),Direction.RIGHT),
			};
			movementCommands.add(new MovementCommand(center.coords, Direction.DOWN, 25L));
			movementCommands.add(new MovementCommand(center.coords, Direction.UP, 25L));
			movementCommands.add(new MovementCommand(center.coords, Direction.LEFT, 25L));
			movementCommands.add(new MovementCommand(center.coords, Direction.RIGHT, 25L));
			return;
		}

		if (m_Step == 0)
		{
			if (bot == null)
			{
				for (int i = 0; i < bots.length; i++)
				{
					FastExpandBot expandBot = new FastExpandBot(m_Main);
					expandBot.setCenter(((MoveBot)bots[i]).head());
					bots[i] = expandBot;
				}
				bot = new ExpandBot();
				m_Step = 55;
			}
			else
			{

				m_Step = 75;
			}
		}
//		if (m_Step < 6)
//		{
//			bot.getNextCommands(universeView, movementCommands);
//		}
		if (bot != null)
			bot.getNextCommands(universeView, movementCommands);
		else
		{
			for (PlayerBot bot : bots)
				bot.getNextCommands(universeView, movementCommands);
		}
		m_Step--;
	}
}
