package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf;

import de.tum.ei.lkn.eces.graph.Edge;

/**
 * Data stored at each Edge during CBF algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EdgeData implements Comparable<EdgeData> {

	/**
	 * Edge to which the data is attached.
	 */
	private Edge edge;

	/**
	 * Cost to reach and use the Edge.
	 */
	private double costSoFar;

	/**
	 * Delay to reach and use the Edge.
	 */
	private double delaySoFar;

	/**
	 * Parameters value to reach and use the Edge.
	 */
	private double[] parametersSoFar;

	/**
	 * Path used to reach the Edge (not including it).
	 */
	private EdgePath pathSoFar;


	private int sqnum;

	public EdgeData() {
		init();
	}

	public void init() {
		edge = null;
		costSoFar = Double.MAX_VALUE;
		delaySoFar = Double.MAX_VALUE;
		parametersSoFar = null;
		pathSoFar = null;
		sqnum = 0;
	}

	public double getCostSoFar() {
		return costSoFar;
	}

	public void setCostSoFar(double costSoFar) {
		this.costSoFar = costSoFar;
	}

	public double getDelaySoFar() {
		return delaySoFar;
	}

	public void setDelaySoFar(double delaySoFar) {
		this.delaySoFar = delaySoFar;
	}

	public void setParametersSoFar(double[] parametersSoFar) {
		this.parametersSoFar = parametersSoFar;
	}

	public double[] getParametersSoFar() {
		return parametersSoFar;
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	public EdgePath getPathSoFar() {
		return pathSoFar;
	}

	public void setPathSoFar(EdgePath pathSoFar) {
		this.pathSoFar = pathSoFar;
	}

	public void setSqnum(int sqnum) {
		this.sqnum = sqnum;
	}

	@Override
	public int compareTo(EdgeData other) {
		// Comparison based on the delaySoFar.
		if(this.getDelaySoFar() > other.getDelaySoFar())
			return 1;
		if(this.getDelaySoFar() < other.getDelaySoFar())
			return -1;
		if(this.sqnum > other.sqnum) // For determinism.
			return 1;
		if(this.sqnum < other.sqnum)
			return -1;
		return 0;
	}

	@Override
	public String toString() {
		return edge.toString();
	}
}
