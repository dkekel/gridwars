
package cern.ais.gridwars.util.metric;

import cern.ais.gridwars.Coordinates;

public abstract class Metric
{
	public abstract double measure(Coordinates from, Coordinates to);
}
