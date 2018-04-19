package cern.ais.gridwars;

import cern.ais.gridwars.api.bot.PlayerBot;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;


public final class Emulator {

    private static final int[] SPEEDS = new int[] { 0, 5, 10, 20, 40, 80, 160, 320 };

	private JFrame frame;
	private int timerSpeedIndex = 3;
	private Game game;
	private Timer timer;
    private volatile boolean running = true;

    public void runGame(PlayerBot bot1, PlayerBot bot2) throws FileNotFoundException {
        createGame(bot1, bot2);
        createAndShowGUI();
    }

	private void createGame(PlayerBot bot1, PlayerBot bot2) throws FileNotFoundException {
        List<Player> players = Arrays.asList(
            new Player(0, bot1, new File("bot1.log"), 0),
            new Player(1, bot2, new File("bot2.log"), 1)
        );

		game = new Game(players, (player, turn, binaryGameStatus) -> {
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
				for (int y = 0; y < GameConstants.UNIVERSE_SIZE; y++) {
                    for (int x = 0; x < GameConstants.UNIVERSE_SIZE; x++) {
                        Cell cell = universe.getCell(x, y);
                        long population = cell.getPopulation();
                        if (population == 0) {
                            continue;
                        }

                        int alpha = (int) (Math.abs(population) / (double) GameConstants.MAXIMUM_POPULATION * 255);
                        Color color = cell.getOwner().getColorIndex() == 0 ? Color.blue : Color.red;
                        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

                        graphics.setColor(color);
                        graphics.fillRect(x * 10, 50 + y * 10, 10, 10);
                    }
                }
			}
		};

		mainPanel.add(drawPanel, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new BorderLayout());
		mainPanel.add(controlPanel, BorderLayout.SOUTH);

		final JToggleButton pauseButton = new JToggleButton("Pause");
		pauseButton.setSelected(false);
		pauseButton.addItemListener(itemEvent -> {
		    boolean shouldBeRunning = itemEvent.getStateChange() != ItemEvent.SELECTED;
		    running = shouldBeRunning;
		    pauseButton.setText(shouldBeRunning ? "Pause" : "Play");
        });

		controlPanel.add(pauseButton);
		controlPanel.add(createSpeedControlButton("+"), BorderLayout.EAST);
		controlPanel.add(createSpeedControlButton("-"), BorderLayout.WEST);

		frame.setPreferredSize(new Dimension(520, 620));
		frame.getContentPane().add(mainPanel);

		frame.pack();
		frame.setVisible(true);

		// TODO Center on the screen

		final int[] delta = { 0 };
		timer = new Timer(5, e -> {
            if (!running) {
                return;
            }

            if (delta[0] < SPEEDS[timerSpeedIndex]) {
                delta[0] += 5;
                return;
            }
            delta[0] = 0;

            if (!game.isFinished()) {
                game.nextTurn();
            } else {
                timer.stop();
                showWinnerInTitle();
                game.cleanUp();
            }
        });

		timer.start();
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

	private void showWinnerInTitle() {
        if (game.isFinished()) {
            Player winner = game.getWinner();

            String titleSuffix = "DRAW";
            if (winner != null) {
                if (winner.getId() == 0) {
                    titleSuffix = "Bot 1 (blue) wins";
                } else {
                    titleSuffix = "Bot 2 (red) wins";
                }
            }
            frame.setTitle(frame.getTitle() + " - " + titleSuffix);
        }
    }
}
