package cern.ais.gridwars;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.cell.Cell;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.universe.Universe;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;


public class Visualizer {
    private static final int[] SPEEDS = new int[] { 10, 20, 40, 80, 160, 320, 500, 1000 };

	private JFrame frame;
	private boolean running = true;
	private int timerSpeedIndex = 3;
	private Game game;

    public static void main(String[] args) throws FileNotFoundException {
        new Visualizer().runGame(new IdleBot(), new MovingBot(MovementCommand.Direction.RIGHT));
    }

    public void runGame(PlayerBot bot1, PlayerBot bot2) throws FileNotFoundException {
        createGame(bot1, bot2);
        createAndShowGUI();
    }

	private void createGame(PlayerBot bot1, PlayerBot bot2) throws FileNotFoundException {
        List<Player> players = Arrays.asList(
            new Player(0, bot1, new File("bot1.log"), 0),
            new Player(1, bot2, new File("bot2.log"), 1)
        );

		game = new Game(players, (player, turn, movementCommands, binaryGameStatus) -> {
            frame.setTitle("Turn " + turn);
            frame.repaint();
        }, true);

		game.startUp();
	}

	private void createAndShowGUI() {
		frame = new JFrame("GridWars Emulator");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel drawPanel = new JPanel(new BorderLayout()) {
			@Override
            public void paint(Graphics graphics) {
				Universe universe = game.getUniverse();
				for (int my = 0; my < GameConstants.UNIVERSE_SIZE; my++) {
                    for (int mx = 0; mx < GameConstants.UNIVERSE_SIZE; mx++) {
                        Cell cell = universe.getCell(mx, my);
                        long population = cell.getPopulation();
                        if (population == 0) {
                            continue;
                        }

                        int alpha = (int) (Math.abs(population) / (double) GameConstants.MAXIMUM_POPULATION * 255);
                        Color color = cell.getOwner().getColorIndex() == 0 ? Color.blue : Color.red;
                        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

                        graphics.setColor(color);
                        graphics.fillRect(mx * 10, 50 + my * 10, 10, 10);
                    }
                }
			}
		};

		mainPanel.add(drawPanel, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new BorderLayout());
		mainPanel.add(controlPanel, BorderLayout.SOUTH);

		final JToggleButton pauseButton = new JToggleButton("Pause");
		pauseButton.setSelected(false);
		pauseButton.addItemListener(itemEvent -> running = itemEvent.getStateChange() != ItemEvent.SELECTED);

		controlPanel.add(pauseButton);
		controlPanel.add(createSpeedControlButton("+"), BorderLayout.EAST);
		controlPanel.add(createSpeedControlButton("-"), BorderLayout.WEST);

		frame.setPreferredSize(new Dimension(500, 600));
		frame.getContentPane().add(mainPanel);

		frame.pack();
		frame.setVisible(true);

		final int[] delta = { 0 };
		new Timer(10, e -> {
            if (!running) {
                return;
            }

            if (delta[0] < SPEEDS[timerSpeedIndex]) {
                delta[0] += 10;
                return;
            }
            delta[0] = 0;

            if (!game.isFinished()) {
                game.nextTurn();
            }
        }).start();
	}

	private Component createSpeedControlButton(final String caption) {
		JButton control = new JButton(caption);
		control.addActionListener(e -> {
            if (caption.equals("-") && timerSpeedIndex < SPEEDS.length - 2) {
                timerSpeedIndex += 1;
            }

            if (caption.equals("+") && timerSpeedIndex > 0) {
                timerSpeedIndex -= 1;
            }
        });
		return control;
	}
}
