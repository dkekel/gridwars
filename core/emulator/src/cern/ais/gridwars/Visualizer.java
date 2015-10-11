package cern.ais.gridwars;

import cern.ais.gridwars.cell.Cell;
import cern.ais.gridwars.command.MovementCommand;
import cern.ais.gridwars.universe.Universe;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class Visualizer {
	private static JFrame frame;
	private static boolean m_Running = !false;
	private static Label m_StatusLabel;
	private static int[] speeds = new int[] { 10, 20, 40, 80, 160, 320, 500 };
	private static int m_TimerSpeedId = 4;
	private static Game game;

	private static void createGame() throws FileNotFoundException {
		game = new Game(Arrays.asList(new Player(0, new MovingBot(MovementCommand.Direction.DOWN), new File("player1.log"), 0), new Player(1, new MovingBot(MovementCommand.Direction.RIGHT), new File("player2.log"), 1)), new Game.TurnCallback() {
			@Override public void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands, ByteBuffer binaryGameStatus) {
				frame.setTitle("Turn " + turn);
				frame.repaint();
			}
		});

		game.startUp();
	}

	private static void createAndShowGUI() throws FileNotFoundException
	{
		createGame();
		frame = new JFrame("GridWars Emulator");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel drawPanel = new JPanel(new BorderLayout()) {
			@Override public void paint(Graphics graphics) {
				Universe universe = game.getUniverse();
				for (int my = 0; my < GameConstants.UNIVERSE_SIZE; my++)
					for (int mx = 0; mx < GameConstants.UNIVERSE_SIZE; mx++)
					{
						Cell cell = universe.getCell(my, mx);
						long population = cell.getPopulation();
						if (population == 0)
							continue;

						Color color = cell.getOwner().colorIndex == 0 ? Color.red : Color.blue;
						int alpha = (int)(Math.abs(population) / (double)GameConstants.MAXIMUM_POPULATION * 255);
						color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

						graphics.setColor(color);
						graphics.fillRect(mx * 10, 50 + my * 10, 10, 10);
					}
			}
		};

		m_StatusLabel = new Label("IDLE");
		mainPanel.add(m_StatusLabel, BorderLayout.NORTH);
		mainPanel.add(drawPanel, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new BorderLayout());
		mainPanel.add(controlPanel, BorderLayout.SOUTH);

		final JToggleButton pauseButton = new JToggleButton("Pause");
		pauseButton.setSelected(false);
		pauseButton.addItemListener(new ItemListener() {
			@Override public void itemStateChanged(ItemEvent itemEvent) {
				m_Running = itemEvent.getStateChange() != ItemEvent.SELECTED;
			}
		});

		controlPanel.add(pauseButton);
		controlPanel.add(createSpeedControlButton("+"), BorderLayout.EAST);
		controlPanel.add(createSpeedControlButton("-"), BorderLayout.WEST);
		
		frame.setPreferredSize(new Dimension(500, 600));
		frame.getContentPane().add(mainPanel);

		frame.pack();
		frame.setVisible(true);

		final int[] delta = { 0 };
		new Timer(10, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if (!m_Running) return;

				if (delta[0] < speeds[m_TimerSpeedId]) {
					delta[0] += 10;
					return;
				}
				delta[0] = 0;

				if (!game.done()) game.nextTurn();
			}
		}).start();
	}

	private static Component createSpeedControlButton(final String caption) {
		JButton control = new JButton(caption);
		control.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if (caption.equals("+") && m_TimerSpeedId < speeds.length - 2)
					m_TimerSpeedId += 1;
				if (caption.equals("-") && m_TimerSpeedId > 0)
					m_TimerSpeedId -= 1;
			}
		});
		return control;
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		createAndShowGUI();
	}
}
