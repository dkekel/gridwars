package cern.ais.gridwars.bot.winner;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Winner bot of the Spring Campus 2018 in Riga.
 *
 * Team: SpringGridFusion
 * Author: Arnis Stasko (arnis.stasko@gmail.com)
 */
public class HenrymightyBot implements PlayerBot {
    boolean init = false;
    private List<Item> order = new ArrayList<Item>();
    private int marked[][];
    private int moves = 0;

    public void HernymightyBot() {
        // Empty constructor
    }

    private class Item {
        public int x;
        public int y;

        public Item(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void getNextCommands(UniverseView universe, List<MovementCommand> list) {
        List <Coordinates> team = universe.getMyCells();

        if (!init) {
            init = true;
            marked = new int[universe.getUniverseSize()][universe.getUniverseSize()];
        }

        // Count moves
        moves++;
        //System.out.println("MOVE #" + moves);

        // Initialize order
        for (Coordinates cell : team) {
            if (marked[cell.getX()][cell.getY()] != 1) {
                marked[cell.getX()][cell.getY()] = 1;
                order.add(new Item(cell.getX(), cell.getY()));
            } else {
                //System.out.println("Good ahead [" + cell.getX() + "][" + cell.getY() + "]");
            }
        }
        //System.out.println("ordered: " + order.size());

        // Select policy
        int policy[][][] = new int[universe.getUniverseSize()][universe.getUniverseSize()][4];
        int fc[][] = new int[universe.getUniverseSize()][universe.getUniverseSize()];

        for (Coordinates cell : team) {
            // Priority: enemy
            int targetPolicy[] = new int[4];
            boolean hasPolicy = false;

            for (int lev = 1; !hasPolicy && lev <= 10; lev++) {
                hasPolicy = detectPolicy(universe, cell, targetPolicy, lev);
            }

            if (hasPolicy) {
                policy[cell.getX()][cell.getY()] = targetPolicy;
            } else {
                policy[cell.getX()][cell.getY()] = new int[] {1, 1, 1, 1};
                //System.out.println("NO POLICY");
            }

            //System.out.print("[" + cell.getX() + "][" + cell.getY() + "] = ");
            //System.out.print(policy[cell.getX()][cell.getY()][0]+",");
            //System.out.print(policy[cell.getX()][cell.getY()][1]+",");
            //System.out.print(policy[cell.getX()][cell.getY()][2]+",");
            //System.out.println(policy[cell.getX()][cell.getY()][3]);
        }

        // Move
        for (Item item: order) {
            if (universe.belongsToMe(item.x, item.y)) {
                int value = universe.getPopulation(item.x, item.y);
                int fcAmount = value + fc[item.x][item.y];
                int send = Math.max(0, Math.min(value, fcAmount - 5));

                //System.out.println("send: " + send + "(value: " + value + ", fc: " + fc[item.x][item.y] + ")");
                if (send > 0) {
                    Coordinates cell = universe.getCoordinates(item.x, item.y);

                    int p[] = policy[item.x][item.y];
                    int max = 0;
                    for (int i = 0, c = p.length; i < c; i++) {
                        max += p[i];
                    }
                    int part = send / max;
                    if (part < 6) {
                        part = 5;
                    } else if (part == 25) {
                        part = 25;
                    } else if (part > 15) {
                        part = 15;
                    }

                    //System.out.println("Part: " + part);
                    int done = 0;
                    int sent = 0;
                    int rest = 0;

                    int stats[] = new int[4];

                    for (int b = 0; b < 4; b++) {
                        if (p[b] == 1 && (send - sent) > 0) {
                            done++;

                            int sendAmount = (done == max ? Math.max(0, send - sent) : Math.min(part, Math.max(0, send - sent)));
                            sent += sendAmount;
                            rest = Math.max(0, send - sent);
                            if (rest < 5) {
                                sendAmount += rest;
                                sent += rest;
                            }
                            if (sendAmount > 0) {
                                Coordinates targetCell = getTargetCell(cell, b);
                                fc[targetCell.getX()][targetCell.getY()] = sendAmount;
                                list.add(new MovementCommand(cell, getTargetDirection(b), sendAmount));
                            }

                            stats[b] = sendAmount;
                        }
                    }
                    //System.out.println("Send [" + stats[0] + ", " + stats[1] + ", " + stats[2] + ", " + stats[3] + "], total: " + sent);
                }
            }
        }
    }

    private boolean detectPolicy(UniverseView universe, Coordinates cell, int policy[], int level) {
        boolean hasPolicy = false;

        int value = universe.getPopulation(cell);

        Coordinates up = cell.getUp(level);
        Coordinates right = cell.getRight(level);
        Coordinates down = cell.getDown(level);
        Coordinates left = cell.getLeft(level);

        int upPopulation = universe.getPopulation(up);
        int rightPopulation = universe.getPopulation(right);
        int downPopulation = universe.getPopulation(down);
        int leftPopulation = universe.getPopulation(left);

        if (level < 10 || value < 100) {
            if (upPopulation == 0 || (upPopulation > 0 && !universe.belongsToMe(up))) {
                policy[0] = 1;
                hasPolicy = true;
            }
            if (rightPopulation == 0 || (rightPopulation > 0 && !universe.belongsToMe(right))) {
                policy[1] = 1;
                hasPolicy = true;
            }
            if (downPopulation == 0 || (downPopulation > 0 && !universe.belongsToMe(down))) {
                policy[2] = 1;
                hasPolicy = true;
            }
            if (leftPopulation == 0 || (leftPopulation > 0 && !universe.belongsToMe(left))) {
                policy[3] = 1;
                hasPolicy = true;
            }
        } else {
            if (upPopulation < 100) {
                policy[0] = 1;
                hasPolicy = true;
            }
            if (rightPopulation < 100) {
                policy[1] = 1;
                hasPolicy = true;
            }
            if (downPopulation < 100) {
                policy[2] = 1;
                hasPolicy = true;
            }
            if (leftPopulation < 100) {
                policy[3] = 1;
                hasPolicy = true;
            }
        }

        //if (hasPolicy) {
        //    System.out.println("Policy LEVEL" + level);
        //}

        return hasPolicy;
    }

    private Coordinates getTargetCell(Coordinates cell, int direction) {
        Coordinates target = null;

        switch (direction) {
            case 0:
                target = cell.getUp();
                break;
            case 1:
                target = cell.getRight();
                break;
            case 2:
                target = cell.getDown();
                break;
            case 3:
                target = cell.getLeft();
                break;
        }

        return target;
    }

    private MovementCommand.Direction getTargetDirection(int dir) {
        MovementCommand.Direction direction = null;

        switch (dir) {
            case 0:
                direction = MovementCommand.Direction.UP;
                break;
            case 1:
                direction = MovementCommand.Direction.RIGHT;
                break;
            case 2:
                direction = MovementCommand.Direction.DOWN;
            break;
            case 3:
                direction = MovementCommand.Direction.LEFT;
            break;
        }

        return direction;
    }
}
