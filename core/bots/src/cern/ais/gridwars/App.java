package cern.ais.gridwars;

import java.io.FileNotFoundException;

public class App {
	public static void main(String[] args) throws FileNotFoundException {
		new Visualizer().runGame(new Main(), new ExpandBot());
	}
}
