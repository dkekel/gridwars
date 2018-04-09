
package cern.ais.gridwars.util;

import cern.ais.gridwars.command.MovementCommand;

public class Cell
{
	public final Cell[][] grid;
	public final int x;
	public final int y;
	public long population = 0;
	public cern.ais.gridwars.Coordinates coords;
	public int visitTurn = -1;
	public long nextPopulation = 0;
	public int movedFlag = -1;
	public long wasMovedFrom = 0;

	public Cell(Cell[][] grid, int x, int y)
	{
		this.grid = grid;
		this.x = x;
		this.y = y;
	}

	public Cell[] neighbours()
	{
		int max = grid.length - 1;
		int top = y - 1 < 0 ? max : y - 1;
		int bottom = y + 1 > max ? 0 : y + 1;
		int left = x - 1 < 0 ? max : x - 1;
		int right = x + 1 > max ? 0 : x + 1;
		return new Cell[]{grid[top][x], grid[y][right], grid[bottom][x], grid[y][left]};
	}

	public Cell getRelative(MovementCommand.Direction direction)
	{
		switch (direction)
		{
		case LEFT:
			return neighbours()[3];
		case RIGHT:
			return neighbours()[1];
		case UP:
			return neighbours()[0];
		case DOWN:
			return neighbours()[2];
		}
		throw new NullPointerException("direction cannot be null");
	}

	@Override public String toString()
	{
		return "{ " +
			population +
			"->" + nextPopulation +
			", " + coords +
			", v: ?" + visitTurn +
			", m: ?" + movedFlag +
			", wm: " + wasMovedFrom +
			"}";
	}
}
