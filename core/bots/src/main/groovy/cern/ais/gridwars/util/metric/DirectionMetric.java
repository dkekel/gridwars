
package cern.ais.gridwars.util.metric;

import cern.ais.gridwars.Coordinates;

public class DirectionMetric extends Metric
{
	private Metric m_InnerMetric = new EuclidianMetric();

	@Override public double measure(Coordinates from, Coordinates to)
	{
		int dx = from.getX() - to.getX();
		int dy = from.getY() - to.getY();
//		return dy < 0 ? 0 : m_InnerMetric.measure(from, to);
		double distance = m_InnerMetric.measure(from, to);
		return  distance > 10 ? distance : distance;
	}
}
