package gridwars.starter;

import cern.ais.gridwars.Emulator;

import java.io.FileNotFoundException;


/**
 * Instantiates the example bots and starts the visual game emulator.
 */
public class EmulatorRunner {

    public static void main(String[] args) throws FileNotFoundException {
        MovingBot blueBot = new MovingBot();
        ExpandBot redBot = new ExpandBot();

        new Emulator().runGame(blueBot, redBot);
    }
}
