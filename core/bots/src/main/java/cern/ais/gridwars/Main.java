package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.util.Cell;

import java.util.ArrayList;
import java.util.List;

public class Main implements PlayerBot
{
	public PlayerBot bot;
	public Cell[][] grid;
	public int m_Size = 0;
	public long myPopulation;
	public long theirPopulation;
	public int myCells;
	public int theirCells;
	public long bestValue = 5;
	public long maxPopulation = 100;
	private Cell startCell; //FIXME temp
	public Cell maxCell;
	private Cell[] enemyCores;
	private int m_EnemyCoreTimeout = 0;

	public Main()
	{
		bot = new ExpandBot();
//		bot = new RocketBot();
//		bot = new ThreeFishBot();
//		bot = new FastExpandBot(this);
//		bot = new SplitBot(this);
	}

	public void setBot(PlayerBot bot) {
		this.bot = bot;
	}

	private void createGrid()
	{
		grid = new Cell[m_Size][m_Size];
		for (int y = 0; y < m_Size; y++)
		{
			for (int x = 0; x < m_Size; x++)
				grid[y][x] = new Cell(grid, x, y);
		}
	}

	private Coordinates getTopLeftCoords(Coordinates coordinates)
	{
		return coordinates.getLeft(coordinates.getX()).getUp(coordinates.getY());
	}

	@Override public void getNextCommands(UniverseView universeView, List<MovementCommand> movementCommands)
	{
		m_Size = universeView.getUniverseSize();

		if (grid == null)
		{
			createGrid();
			Coordinates coords = universeView.getMyCells().get(0);
			startCell = grid[coords.getY()][coords.getX()];
		}

		myPopulation = 0;
		theirPopulation = 0;
		myCells = 0;
		theirCells = 0;
		maxCell = grid[0][0];
		Coordinates coords = getTopLeftCoords(universeView.getMyCells().get(0));
		for (int y = 0; y < m_Size; y++, coords = coords.getDown())
		{
			Coordinates currCoords = coords;
			for (int x = 0; x < m_Size; x++, currCoords = currCoords.getRight())
			{
				// FIXME test wasn't coordinate creation faster;
				boolean isMy = universeView.belongsToMe(x, y);
				int population = universeView.getPopulation(x, y);
				Cell cell = grid[y][x];
				cell.population = isMy ? population : -population;
				cell.coords = currCoords;
				cell.wasMovedFrom = 0;
				if (population > maxCell.population)
					maxCell = cell;

				if (isMy)
				{
					myPopulation += population;
					myCells++;
				}
				else if (population != 0)
				{
					theirPopulation += population;
					theirCells++;
				}
			}
		}

		int turn = universeView.getCurrentTurn();

		System.out.println("Turn " + turn +
			" their: " + theirPopulation + "(" + theirCells + ")" +
			" my: " + myPopulation + "(" + myCells + ")");

		// FIXME REMOVE IT
		// if (Math.random() > 0.8)
		//	((FastExpandBot)bot).center().population = 0;

//		if (turn > 2 && (turn / 2) % 10 == 0 || turn > 2 && (turn / 2) % 10 == 1)
//		{
//			expandBot.getNextCommands(universeView, movementCommands);
//			return;
//		}
		bot.getNextCommands(universeView, movementCommands);
	}


	public Cell[] getEnemyCores()
	{
		if (m_EnemyCoreTimeout-- != 0)
			return enemyCores;
		ArrayList <Cell> enemyPoints = new ArrayList<Cell>(10);
		for (Cell rows[] : grid)
		{
			for (Cell cell : rows)
			{
				if (cell.population < 0)
					enemyPoints.add(cell);
			}
		}

		int size = enemyPoints.size();
		int index = size / 5;
		int offset = (int)(Math.random() * index / 2);
		enemyCores = new Cell[4];

		for (int i = 0; i < 4; i++)
			enemyCores[i] = enemyPoints.get((offset + size / index * i) % size);
		m_EnemyCoreTimeout = 20;

		for (int i = 0; i < 4; i++)
			System.out.println(enemyCores[i]);
		return enemyCores;
	}

	public Cell getCenter(Cell from)
	{
		return startCell;
	}

	public Cell getCenter(Coordinates from)
	{
		return startCell;
	}

	public Cell getCell(Coordinates coordinates)
	{
		return grid[coordinates.getY()][coordinates.getX()];
	}

	public Cell getCell(int x, int y)
	{
		return grid[y][x];
	}
}
