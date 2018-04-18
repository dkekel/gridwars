
package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.command.MovementCommand.Direction;
import cern.ais.gridwars.util.Cell;
import cern.ais.gridwars.util.metric.EuclidianMetric;
import cern.ais.gridwars.util.metric.Metric;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FastExpandBot implements PlayerBot
{
	private Main m_MainBot;
	private final Queue<Cell> cells;
	private final Direction[] directions = new Direction[]
		{Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
	private final Metric metric = new EuclidianMetric();
	private Cell m_Center;
	private AimedBot attackBot;

	public FastExpandBot(Main mainBot)
	{
		cells = new ArrayDeque<Cell>();
		m_MainBot = mainBot;
		attackBot = new AimedBot(mainBot);
	}

	public FastExpandBot()
	{
		this(new Main());
	}

	@Override public void getNextCommands(UniverseView uV, List<MovementCommand> movementCommands)
	{
		if (m_Center == null)
			m_Center = m_MainBot.getCenter((Cell) null);
		int turn = uV.getCurrentTurn() / 2;
		if (m_Center.population <= 0)
			m_Center = m_MainBot.maxCell;
		cells.add(m_Center);
		m_Center.visitTurn = turn;

		List<Cell> myAttackCells = new ArrayList<Cell>();

		double[] costs = new double[4];
		for (Cell from; (from = cells.poll()) != null;)
		{
			from.movedFlag = turn & 1;
			// To how many parts can we split
			int split = 0;
			int maxDistance = 0;
			double distanceSumm = 0;
			Cell[] neighbours = from.neighbours();
			for (int i = 0; i < 4; i++)
			{
				Cell c = neighbours[i];
				if (c.movedFlag == -1 || c.movedFlag != (turn & 1))
				{
					split++; // we will move here, so lets split for mor parts
					double distance = metric.measure(m_Center.coords, c.coords);
					distanceSumm += distance;
					costs[i] = distance;
					if (distance > maxDistance)
						maxDistance = (int)Math.round(distance);
				}
			}

			// Add neighbours to the wave query
			for (int i = 0; i < 4; i++)
			{
				Cell c = neighbours[i];
				if (c.visitTurn != turn && uV.belongsToMe(c.coords))
				{
					cells.add(c);
					c.visitTurn = turn;
				}
			}

			if (split == 0)
				continue;

			int tmp = 5 - (maxDistance / 3);
			if (tmp > 5 && tmp < 0)
				tmp = 0;

			long available = from.population - 5;
			if (maxDistance > 10)
			{
				available = from.population > 5 ? 5 : available;
				if (available > 0)
					myAttackCells.add(from);

			}
			if (available <= 0)
				continue;

			// Do some movements
			for (int i = 0; i < 4; i++)
			{
				// Distribute by metrics
				Cell c = neighbours[i];
				if (c.movedFlag == -1 || c.movedFlag != (turn & 1))
				{
					int amount = (int) (costs[i] / distanceSumm * available);
					if (amount == 0)
						continue;
					from.wasMovedFrom += amount;
					movementCommands.add(new MovementCommand(from.coords, directions[i], amount));
				}
			}
		}

		attackBot.sendArmiesToDefendUs(uV, movementCommands, myAttackCells, m_Center);
	}

	public Cell center()
	{
		return m_Center;
	}

	public void setCenter(Cell value)
	{
		m_Center = value;
	}
}
