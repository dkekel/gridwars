package cern.ais.gridwars.bot;

import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;
import cern.ais.gridwars.bot.util.Cell;

import java.util.List;


public class MoveBot implements PlayerBot
{
	private Cell m_Head;
	private final MovementCommand.Direction m_Direction;

	public MoveBot(Cell head, MovementCommand.Direction direction)
	{
		m_Head = head;
		m_Direction = direction;
	}

	@Override public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands)
	{
		int population = universeView.getPopulation(m_Head.coords);
		if (population > 0)
		{
			movementCommands.add(new MovementCommand(m_Head.coords, m_Direction, population));
			m_Head = m_Head.getRelative(m_Direction);
		}
	}

	public Cell head()
	{
		return m_Head;
	}
}
