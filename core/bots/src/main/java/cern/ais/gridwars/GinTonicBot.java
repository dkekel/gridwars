package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GinTonicBot implements PlayerBot {

	static {
		System.out.println("Static init block was called on: cern.ais.gridwars.GinTonicBot");
	}

	private static final int MIN_POPULATION = 15;

	private boolean firstRound = true;
	private int universeSize;
	private int minPopulation;
	private UniverseView universeView;
	private List<MovementCommand.Direction> directions;
	private Coordinates myOriginCell;
	private Coordinates enemyOriginCell;
	private List<Coordinates> myCurrentCells;

	public GinTonicBot() {
		System.out.println("Constructor called on: " + getClass().getName());
	}

	@Override
	public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
		this.universeView = universeView;

		if (firstRound) {
			initializeUniverseData();
			firstRound = false;
		}

		initializeCurrentRoundData();

		for (Coordinates currentCell : myCurrentCells) {
			int currentPopulation = universeView.getPopulation(currentCell);

			if (currentPopulation > minPopulation) {
				for (int distance = 1; distance < universeSize; distance++) {
					boolean moved = false;

					// TODO Implement to grow preferably in the direction of the enemy
					for (MovementCommand.Direction direction : directions) {
						if (!universeView.belongsToMe(currentCell.getRelative(distance, direction))) {
							commandList.add(new MovementCommand(currentCell, direction, currentPopulation - minPopulation));
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

	private void initializeUniverseData() {
		universeSize = universeView.getUniverseSize();
//		minPopulation = Math.round(1 / (universeView.getGrowthRate() - 1));
		minPopulation = MIN_POPULATION;

		System.out.println("Universe size: " + universeSize);
		System.out.println("Universe growth rate: " + universeView.getGrowthRate());
		System.out.println("Min population: " + minPopulation);

		findMyOriginCell();
		findEnemyOriginCell();
		determineMainGrowDirection();
	}

	private void findMyOriginCell() {
		boolean foundPosition = false;

		for (int x = 0; x < universeSize; x++) {
			for (int y = 0; y < universeSize; y++) {
				if (!universeView.isEmpty(x, y) && universeView.belongsToMe(x, y)) {
					myOriginCell = universeView.getCoordinates(x, y);
					foundPosition = true;
					break;
				}
			}

			if (foundPosition) {
				break;
			}
		}

		if (!foundPosition) {
			throw new RuntimeException("Could not find my origin");
		}

		System.out.println("Found my origin at: [" + myOriginCell.getX() + ", " + myOriginCell.getY() + "]");
	}

	private void findEnemyOriginCell() {
		boolean foundPosition = false;

		for (int x = 0; x < universeSize; x++) {
			for (int y = 0; y < universeSize; y++) {
				if (!universeView.isEmpty(x, y) && !universeView.belongsToMe(x, y)) {
					enemyOriginCell = universeView.getCoordinates(x, y);
					foundPosition = true;
					break;
				}
			}

			if (foundPosition) {
				break;
			}
		}

		if (!foundPosition) {
			throw new RuntimeException("Could not find my origin");
		}

		System.out.println("Found enemy origin at: [" + enemyOriginCell.getX() + ", " + enemyOriginCell.getY() + "]");
	}

	private void determineMainGrowDirection() {
		directions = Arrays.asList(MovementCommand.Direction.values());
		Collections.shuffle(directions);
	}

//	private void determineMainGrowDirection() {
//		directions = new ArrayList<>();
//
//		// Determine the sector of the enemy origin relative to ours.
//		int xDiff = myOriginCell.getX() - enemyOriginCell.getX();
//		int yDiff = myOriginCell.getY() - enemyOriginCell.getY();
//
//		if (xDiff >= 0) {
//			directions.add(MovementCommand.Direction.DOWN);
//			directions.add(MovementCommand.Direction.UP);
//
//		} else {
//			directions.add(MovementCommand.Direction.UP);
//			directions.add(MovementCommand.Direction.DOWN);
//		}
//
//		if (yDiff >= 0) {
//			directions.add(MovementCommand.Direction.RIGHT);
//			directions.add(MovementCommand.Direction.LEFT);
//		} else {
//			directions.add(MovementCommand.Direction.LEFT);
//			directions.add(MovementCommand.Direction.RIGHT);
//		}
//	}

	private void initializeCurrentRoundData() {
		myCurrentCells = universeView.getMyCells();
	}
}
