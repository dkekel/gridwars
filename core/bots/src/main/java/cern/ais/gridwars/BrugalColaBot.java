package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.command.MovementCommand.Direction;

import java.util.*;


public class BrugalColaBot implements PlayerBot {

    static {
        System.out.println("Static init block was called on: cern.ais.gridwars.BrugalColaBot");
    }

    private static final Long ROUND_THRESHOLD = 5L;
    private static final Long ROUND_MODULUS = 10L;

    private UniverseView universeView;
    private long enemySize;
    private long alliesSize;
    private final List<Strategy> strategies = Arrays.asList(new FastExpansion(), new AttackStrategy());

    public BrugalColaBot() {
        System.out.println("Constructor called on: " + getClass().getName());
    }

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> list) {
        long tstart = System.nanoTime();

        this.enemySize = 0;
        this.alliesSize = 0;
        this.universeView = universeView;

        // Separate cells in danger from the rest
        List<Cell> allCells = getMyCells();
        List<Cell> underAttackCells = new LinkedList<Cell>();
        List<Cell> expansionCells = new LinkedList<Cell>();
        for (Cell cell : allCells) {
            if (cell.underAttack) {
                underAttackCells.add(cell);
            } else {
                expansionCells.add(cell);
            }
        }

        // Apply the strategies first to the cells under stack so we can redistribute troops to
        // prevent losing terrain
        applyStrategies(underAttackCells, list);

        // Apply the expansion strategies to the rest
        applyStrategies(expansionCells, list);

//        for (Cell cell : allCells) {
//            System.out.println(String.format("Cell%s[%s/%s] : %s", cell.coordinates, cell.originalPopulation,
//                    cell.population, cell.commands));
//        }

//        System.out.println(String.format("Turn %s: [score : %s / %s] [spent : %s ms] [commands : %s]", enemySize,
//                alliesSize, universeView.getCurrentTurn(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tstart),
//                list.size())
//
//        );
    }

    private void applyStrategies(List<Cell> cells, List<MovementCommand> list) {
        for (Cell cell : cells) {
            if (!cell.done) {
                if (cell.commands == null) {
                    for (Strategy strategy : strategies) {
                        if (strategy.shouldApply(cell)) {
                            strategy.apply(cell);
                            break;
                        }
                    }
                }
                if (cell.commands != null) {
                    for (Command command : cell.commands) {
                        list.add(command.build());
                    }
                    cell.done = true;
                }
            }
        }
    }

    private List<Cell> getMyCells() {
        Map<Coordinates, Cell> temp = new LinkedHashMap<Coordinates, Cell>(universeView.getUniverseSize());
        List<Coordinates> myCoordinates = universeView.getMyCells();
        List<Cell> myCells = new ArrayList<Cell>(myCoordinates.size());
        for (int x = 0; x < universeView.getUniverseSize(); x++) {
            for (int y = 0; y < universeView.getUniverseSize(); y++) {
                Coordinates coordinates = new CoordinatesImpl(x, y);
                if (universeView.belongsToMe(coordinates)) {
                    myCells.add(getCell(coordinates, universeView, temp));
                    alliesSize += universeView.getPopulation(coordinates);
                } else {
                    enemySize += universeView.getPopulation(coordinates);
                }
            }
        }
        return myCells;
    }

    private Cell getCell(Coordinates location, UniverseView universeView, Map<Coordinates, Cell> temp) {
        Cell cell = temp.get(location);
        if (cell == null) {
            cell = new Cell(universeView.getPopulation(location), location, universeView.belongsToMe(location));
            temp.put(location, cell);
            if (cell.mine) {
                cell.neighbours.addAll(getNeighbors(location, universeView, temp));
                cell.underAttack = isUnderAttack(cell);
            }
        }
        return cell;
    }

    private List<NeighborCell> getNeighbors(Coordinates location, UniverseView universeView,
                                            Map<Coordinates, Cell> temp) {
        List<NeighborCell> result = new LinkedList<NeighborCell>();
        for (Direction direction : Direction.values()) {
            result.add(new NeighborCell(direction, getCell(location.getRelative(1, direction), universeView, temp)));
        }
        return result;
    }

    private boolean isUnderAttack(Cell cell) {
        for (NeighborCell neighbor : cell.neighbours) {
            if (!neighbor.cell.mine && neighbor.cell.population > 0) {
                return true;
            }
        }
        return false;
    }

    private Direction invert(Direction direction) {
        switch (direction) {
            case UP:
                return Direction.DOWN;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            case DOWN:
                return Direction.UP;
            default:
                throw new EnumConstantNotPresentException(Direction.class, direction.name());
        }
    }

    private class Cell implements Comparable<Cell> {
        long population;
        boolean underAttack;
        boolean mine;
        boolean done = false;

        final int originalPopulation;
        final Coordinates coordinates;
        final List<NeighborCell> neighbours = new LinkedList<NeighborCell>();

        Long weight = null;
        List<Command> commands = null;

        Cell(long population, Coordinates coordinates, boolean mine) {
            this.originalPopulation = population;
            this.population = population;
            this.coordinates = coordinates;
            this.mine = mine;
        }

        Long getWeight() {
            if (weight == null) {
                weight = 0L;
                for (NeighborCell neighbor : neighbours) {
                    weight += neighbor.cell.population;
                }
            }
            return weight;
        }

        @Override
        public int compareTo(Cell o) {
            return getWeight().compareTo(o.getWeight());
        }
    }

    private class NeighborCell implements Comparable<NeighborCell> {
        final Direction direction;
        final Cell cell;

        NeighborCell(Direction direction, Cell cell) {
            this.direction = direction;
            this.cell = cell;
        }

        @Override
        public int compareTo(NeighborCell o) {
            return Long.valueOf(cell.population).compareTo(o.cell.population);
        }
    }

    private interface Strategy {
        boolean shouldApply(Cell cell);

        void apply(Cell cell);
    }

    private class FastExpansion implements Strategy {

        @Override
        public boolean shouldApply(Cell cell) {
            return !cell.underAttack && cell.population >= 2 * ROUND_THRESHOLD;
        }

        @Override
        public void apply(Cell cell) {
            Map<NeighborCell, Long> commands = new LinkedHashMap<NeighborCell, Long>();
            long populationToMove = cell.originalPopulation - ROUND_THRESHOLD;
            populationToMove = distributeRoundingGarbage(cell, commands, populationToMove);
            expandPopulation(cell, commands, populationToMove);
            removeOriginalCellRoundingGarbage(cell, commands);

            List<Command> list = new LinkedList<Command>();
            for (Map.Entry<NeighborCell, Long> entry : commands.entrySet()) {
                NeighborCell neighbor = entry.getKey();
                Long amount = entry.getValue();
                if (amount > 0) {
                    list.add(new ExpandCommand(cell, neighbor.direction, amount));
                }
            }
            cell.commands = list.isEmpty() ? null : list;
        }

        private long distributeRoundingGarbage(Cell cell, Map<NeighborCell, Long> commands, Long populationToMove) {
            Collections.sort(cell.neighbours);
            for (NeighborCell neighbor : cell.neighbours) {
                commands.put(neighbor, 0L);
                if (neighbor.cell.population < universeView.getMaximumPopulation()) {
                    long remainder = neighbor.cell.population % ROUND_MODULUS;
                    if (remainder < ROUND_THRESHOLD) {
                        Long amount = ROUND_THRESHOLD - remainder;
                        if (amount <= populationToMove) {
                            commands.put(neighbor, commands.get(neighbor) + amount);
                            neighbor.cell.population += amount;
                            cell.population -= amount;
                            populationToMove -= amount;
                        }
                    }
                }
            }
            return populationToMove;
        }

        private void expandPopulation(Cell cell, Map<NeighborCell, Long> commands, Long populationToMove) {
            Collections.sort(cell.neighbours);
            List<NeighborCell> cells = new LinkedList<NeighborCell>(cell.neighbours);
            int buckets = (int) Math.floor(populationToMove / (double) ROUND_MODULUS);
            while (buckets > 0 && !cells.isEmpty()) {
                NeighborCell neighbor = cells.get(0);
                if (neighbor.cell.population + ROUND_MODULUS >= universeView.getMaximumPopulation()) {
                    cells.remove(0);
                } else {
                    commands.put(neighbor, commands.get(neighbor) + ROUND_MODULUS);
                    neighbor.cell.population += ROUND_MODULUS;
                    cell.population -= ROUND_MODULUS;
                    buckets--;
                }
                Collections.sort(cells);
            }
        }

        private void removeOriginalCellRoundingGarbage(Cell cell, Map<NeighborCell, Long> commands) {
            if (cell.population >= ROUND_MODULUS) {
                long remainder = cell.population % ROUND_MODULUS;
                if (remainder < ROUND_THRESHOLD) {
                    long toRemove = remainder + 1;
                    List<NeighborCell> neighbors = new LinkedList<NeighborCell>(commands.keySet());
                    Collections.sort(neighbors);
                    for (NeighborCell neighbor : neighbors) {
                        Long amount = commands.get(neighbor);
                        long population = neighbor.cell.population;
                        long neighborRemainder = population % ROUND_MODULUS;
                        if (neighborRemainder >= ROUND_THRESHOLD && neighborRemainder < ROUND_MODULUS) {
                            long toAdd = Math.min(toRemove, ROUND_MODULUS - neighborRemainder);
                            commands.put(neighbor, amount + toAdd);
                            toRemove -= toAdd;
                            cell.population -= toAdd;
                            neighbor.cell.population += toAdd;
                        }
                        if (toRemove == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private class AttackStrategy implements Strategy {

        @Override
        public boolean shouldApply(Cell cell) {
            return cell.underAttack;
        }

        @Override
        public void apply(Cell cell) {
            List<NeighborCell> allies = new LinkedList<NeighborCell>();
            List<NeighborCell> enemies = new LinkedList<NeighborCell>();
            Map<NeighborCell, Long> defenseCommands = new LinkedHashMap<NeighborCell, Long>();
            for (NeighborCell neighbor : cell.neighbours) {
                if (!neighbor.cell.mine) {
                    enemies.add(neighbor);
                } else if (!neighbor.cell.underAttack && neighbor.cell.commands == null) {
                    allies.add(neighbor);
                    defenseCommands.put(neighbor, 0L);
                }
            }

            attack(cell, enemies);
            helpAttackedCell(cell, allies, defenseCommands);

            for (Map.Entry<NeighborCell, Long> entry : defenseCommands.entrySet()) {
                NeighborCell neighbor = entry.getKey();
                Long amount = entry.getValue();
                if (amount > 0) {
                    List<Command> defenses = new LinkedList<Command>();
                    defenses.add(new DefenseCommand(neighbor.cell, invert(neighbor.direction), amount));
                    neighbor.cell.commands = defenses;
                }
            }
        }

        private void attack(Cell cell, List<NeighborCell> enemies) {
            // Strike !!!
            Collections.sort(enemies);
            Collections.reverse(enemies);
            NeighborCell enemy = enemies.get(0);
            List<Command> attack = new LinkedList<Command>();
            attack.add(new AttackCommand(cell, enemy.direction, cell.originalPopulation));
            cell.population -= cell.originalPopulation;
            cell.commands = attack;
        }

        private void helpAttackedCell(Cell cell, List<NeighborCell> neightborAllies,
                Map<NeighborCell, Long> defenseCommands) {
            LinkedList<NeighborCell> allies = new LinkedList<NeighborCell>(neightborAllies);
            while (cell.population < universeView.getMaximumPopulation() && !allies.isEmpty()) {
                Collections.sort(allies);
                Collections.reverse(allies);
                NeighborCell neighbor = allies.getFirst();
                if (neighbor.cell.population <= ROUND_MODULUS) {
                    allies.removeFirst();
                } else {
                    long remainder = neighbor.cell.population % ROUND_MODULUS;
                    long toRemove;
                    if (remainder < ROUND_THRESHOLD) {
                        toRemove = remainder + 1;
                    } else {
                        toRemove = ROUND_MODULUS;
                    }
                    Long newHelp = defenseCommands.get(neighbor) + toRemove;
                    defenseCommands.put(neighbor, newHelp);
                    neighbor.cell.population -= toRemove;
                    cell.population += toRemove;
                }
            }
        }
    }

    private abstract class Command {
        protected final Cell from;
        protected final Direction direction;
        protected final int amount;

        public Command(Cell from, Direction direction, int amount) {
            this.from = from;
            this.direction = direction;
            this.amount = amount;
        }

        public MovementCommand build() {
            return new MovementCommand(from.coordinates, direction, amount);
        }

        @Override
        public String toString() {
            return String.format("%s. %s -> %s[%s]", getClass().getSimpleName(), from.coordinates, direction, amount);
        }
    }

    private class ExpandCommand extends Command {
        public ExpandCommand(Cell from, Direction direction, int amount) {
            super(from, direction, amount);
        }
    }

    private class AttackCommand extends Command {
        public AttackCommand(Cell from, Direction direction, int amount) {
            super(from, direction, amount);
        }
    }

    private class DefenseCommand extends Command {
        public DefenseCommand(Cell from, Direction direction, int amount) {
            super(from, direction, amount);
        }
    }
}
