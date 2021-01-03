package de.tum.ei.lkn.eces.routing.distancevector;

/**
 * Data stored at each node and for each other node.
 * Just stores the distance.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DistanceVectorData {
	private double hopCount;

	public double getHopCount() {
		return hopCount;
	}

	public void setHopCount(double hopCount) {
		this.hopCount = hopCount;
	}
}
