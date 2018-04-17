package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.util.Cell;

import java.util.ArrayList;
import java.util.List;

import static cern.ais.gridwars.command.MovementCommand.Direction;

public class AimedBot implements PlayerBot
{
	private Coordinates headL;
	private Main main;


	public AimedBot(Main main)
	{
		this.main = main;
	}

	public Coordinates getCoord(int x, int y, UniverseView universeView) {
		return main.getCell(x, y).coords;
	}



	public Coordinates findMyInitialBoundaryCell(UniverseView universeView, Coordinates center) {
		if (isMyBoundaryCell(universeView, center)) {
			return center;
		} else {
			Coordinates c = center.getDown();
			while (c.getY() <= 49) {
				if (isMyBoundaryCell(universeView, c)) {
					return c;
				}
				center.getDown();
			}
			return c;
		}
	}

	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList)
	{
		List<Coordinates> myCells = universeView.getMyCells();

		List<Coordinates> battleCells = getBattleCells(universeView);
	}

	public boolean isMyBoundaryCell(UniverseView universeView, Coordinates coord) {
		if (!universeView.belongsToMe(coord)) {
			return false;
		}
		if ((coord.getX() == 0) || (coord.getY() == 0) ||(coord.getX() == 49) || (coord.getY() == 49)) {
			return true;
		}
		Coordinates left = coord.getLeft();
		Coordinates right = coord.getRight();
		Coordinates up = coord.getUp();
		Coordinates down = coord.getDown();
		if (!universeView.belongsToMe(left) || !universeView.belongsToMe(right) || !universeView.belongsToMe(up) || !universeView.belongsToMe(down)) {
			return true;
		}
		return false;
	}

	public void sendArmiesToDefendUs(UniverseView universeView, List<MovementCommand> movementCommands,
		List<Cell> myAttackCells, Cell center)
	{

		List<Coordinates> battleCells = getPadding(universeView, center.coords);


		for (Cell myCoord : myAttackCells)
		{
			long minMetrics = 1000000L;
			Coordinates aim = myCoord.getRelative(Direction.LEFT).coords;

			if (myCoord == null)
				continue;
			for (Coordinates battleCoord : battleCells)
			{
				if (battleCoord == null)
					continue;
				int deltaY = battleCoord.getY() - myCoord.y;
				int deltaX = battleCoord.getX() - myCoord.x;

				long metrics = Math.abs(deltaX) + Math.abs(deltaY);
				if (metrics < minMetrics) {
					minMetrics = metrics;
					aim = battleCoord;
				}
			}
			//System.out.println("aim: " +aim.getX() + " , " + aim.getY());
			moveTowardsCell(universeView, movementCommands, myCoord, aim);
		}
	}

	public void moveTowardsCell(UniverseView universeView, List<MovementCommand> movementCommands, Cell start, Coordinates aim)
	{
		int deltaY = aim.getY() - start.y;
		int deltaX = aim.getX() - start.x;
		long troopsInCell = start.population - start.wasMovedFrom;
		if (troopsInCell == 0)
			return;

		double ratioX = Math.abs((double)Math.abs(deltaX) / ((double)Math.abs(deltaX) + (double)Math.abs(deltaY)));
		double ratioY = Math.abs((double)Math.abs(deltaY) / ((double)Math.abs(deltaX) + (double)Math.abs(deltaY)));
		int five = 8;
		Coordinates startCoords = start.coords;
		if (deltaX < 0)
		{
			long tr = 0;
			if (troopsInCell > five + 1) {
				tr = troopsInCell - five;
			} else {
				tr = 0;
			}
			if ((ratioX * (troopsInCell-five)) > 1)
			{
				//System.out.println("HAHA1: " +troopsInCell);
				movementCommands.add(new MovementCommand(startCoords, Direction.LEFT, (int) (ratioX * (troopsInCell-five))));
			}
		} else {
			//System.out.println("HAHA2: " +troopsInCell);
			if ((ratioX * (troopsInCell - five)) > 1) {
				//System.out.println("HAHA2: " +troopsInCell);
				movementCommands.add(new MovementCommand(startCoords, Direction.RIGHT, (int)(ratioX * (troopsInCell-five))));
			}
		}
		if (deltaY < 0) {
			if ((ratioY * (troopsInCell - five)) > 1) {
				//System.out.println("HAHA3: " +(troopsInCell - five));
				movementCommands.add(new MovementCommand(startCoords, Direction.UP, (int)(ratioY * (troopsInCell-five))));
			}
		} else {
			if ((ratioY * (troopsInCell - five)) > 1) {
				//System.out.println("HAHA4: " +troopsInCell);
				movementCommands.add(new MovementCommand(startCoords, Direction.DOWN, (int)(ratioY * (troopsInCell-five))));
			}
		}
	}

	public List<Coordinates> getBattleCells(UniverseView universeView) {
		List<Coordinates> myCells = universeView.getMyCells();
		List<Coordinates> battleCells = new ArrayList<Coordinates>();
		for (Coordinates coord : myCells){
			Coordinates left = coord.getLeft();
			Coordinates right = coord.getRight();
			Coordinates up = coord.getUp();
			Coordinates down = coord.getDown();
			if (!universeView.belongsToMe(left) && !universeView.isEmpty(left)) {
				battleCells.add(coord);
				continue;
			}
			if (!universeView.belongsToMe(right) && !universeView.isEmpty(right)) {
				battleCells.add(coord);
				continue;
			}
			if (!universeView.belongsToMe(up) && !universeView.isEmpty(up)) {
				battleCells.add(coord);
				continue;
			}
			if (!universeView.belongsToMe(down) && !universeView.isEmpty(down)) {
				battleCells.add(coord);
				continue;
			}

		}

		return battleCells;
	}



	public List<Coordinates> getPadding(UniverseView universeView, Coordinates center) {
		List<Coordinates> myCells = universeView.getMyCells();
		List<Coordinates> battleCells = new ArrayList<Coordinates>();
		for (Coordinates coord : myCells){
			Coordinates left = coord.getLeft();
			Coordinates right = coord.getRight();
			Coordinates up = coord.getUp();
			Coordinates down = coord.getDown();
			if (!universeView.belongsToMe(left)) {
				battleCells.add(left);
			}
			if (!universeView.belongsToMe(right)) {
				battleCells.add(right);
			}
			if (!universeView.belongsToMe(up)) {
				battleCells.add(up);
			}
			if (!universeView.belongsToMe(down)) {
				battleCells.add(down);
			}
			if (battleCells.size() > 20) {
				int i = battleCells.size() / (battleCells.size() - 20);
				for (int j = 1; j <= battleCells.size(); j++) {
					if (j % i == 0) {
						battleCells.remove(j-1);
					}
				}
			}
			/*if (battleCells.size() > 20) {
				for (int j = 0; j < battleCells.size() - 20; j++) {
					battleCells.remove(j);
				}
			}*/

		}

		return battleCells;
	}

	public AimedBot()
	{
		//bot = new AimedBot();
	}

}
