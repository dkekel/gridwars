package cern.ais.gridwars.util.metric;

import cern.ais.gridwars.Coordinates;

public class EuclidianMetric extends Metric
{
	// FIXME may be should handle world wrapping?
	int size = 50;
	@Override public double measure(Coordinates from, Coordinates to)
	{
		int x1 = from.getX();
		int y1 = from.getY();
		int x2 = to.getX();
		int y2 = to.getY();

		int dx = x2 - x1;
		int dy = y2 - y1;

		dx = dx <= size / 2 ? dx : size - dx;
		dy = dy <= size / 2 ? dy : size - dy;

		return Math.sqrt(dx * dx + dy * dy);
	}
}
