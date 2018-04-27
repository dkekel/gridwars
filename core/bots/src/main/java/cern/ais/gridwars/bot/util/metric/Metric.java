
package cern.ais.gridwars.bot.util.metric;

import cern.ais.gridwars.api.Coordinates;

public abstract class Metric
{
	public abstract double measure(Coordinates from, Coordinates to);
}
