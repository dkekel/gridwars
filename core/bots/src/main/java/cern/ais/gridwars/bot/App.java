package cern.ais.gridwars.bot;

import cern.ais.gridwars.Emulator;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.bot.admin.Level3AdminBot;
//import spring.grid.fusion.HenrymightyBot;

import java.io.FileNotFoundException;

public class App {
	public static void main(String[] args) throws FileNotFoundException {
//	    PlayerBot bot1 = new ExpandBot();

//		Main bot1 = new Main();
//		bot1.setBot(new FastExpandBot(bot1));

//		Main bot1 = new Main();
//        bot1.setBot(new SplitBot(bot1));

        PlayerBot bot1 = new JaegerBot();
//        PlayerBot bot1 = new BrugalColaBot();
//        PlayerBot bot1 = new ExpandBot();
//        PlayerBot bot1 = new HenrymightyBot();

//        PlayerBot bot1 = new RocketBot();
//        PlayerBot bot2 = new JaegerBot();
//        PlayerBot bot2 = new GinTonicBot();
//        PlayerBot bot2 = new BrugalColaBot();
        PlayerBot bot2 = new Level3AdminBot();
//        PlayerBot bot2 = new ThreeFishBot();

//        Main bot2 = new Main();
//		bot2.setBot(new FastExpandBot(bot2));

//        Main bot2 = new Main();
//        bot2.setBot(new SplitBot(bot2));

		Emulator.playMatch(bot1, bot2);
	}
}
