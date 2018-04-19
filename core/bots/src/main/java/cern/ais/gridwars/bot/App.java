package cern.ais.gridwars.bot;

import cern.ais.gridwars.Emulator;

import java.io.FileNotFoundException;

public class App {
	public static void main(String[] args) throws FileNotFoundException {
		Main bot1 = new Main();
		bot1.setBot(new FastExpandBot(bot1));
		Main bot2 = new Main();
		bot2.setBot(new SplitBot(bot2));

		new Emulator().runGame(bot1, bot2);
	}
}
