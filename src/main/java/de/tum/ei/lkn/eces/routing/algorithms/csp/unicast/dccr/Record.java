package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dccr;

import de.tum.ei.lkn.eces.graph.Edge;

/**
 * Record as defined in Appendix A of reference paper. We simply add the
 * cumulative parameters as part of a record.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class Record {
	/**
	 * Delay to reach the Node with this Record.
	 */
	private double delay;

	/**
	 * Cost to reach the Node with this Record.
	 */
	private double cost;

	/**
	 * Weight used for this Record.
	 */
	private double weight;

	/**
	 * Whether or not Node is visited.
	 */
	private boolean visited;

	/**
	 * Parameters values to reach the Node with this Record.
	 */
	private double[] parameters;

	/**
	 * Previous EDGE used to reach the Node with this Record.
	 */
	private Edge previousEdge;

	/**
	 * Previous Record used to reach the Node with this Record.
	 */
	private Record previousRecord;

	public Record() {
		init();
	}

	public void init() {
		cost = Double.MAX_VALUE;
		delay = Double.MAX_VALUE;
		weight = Double.MAX_VALUE;
		previousEdge = null;
		previousRecord = null;
		parameters = null;
		visited = false;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited() {
		this.visited = true;
	}

	public void setUnvisited() {
		this.visited = false;
	}
	public double[] getParameters() {
		return parameters;
	}

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public Edge getPreviousEdge() {
		return previousEdge;
	}

	public void setPreviousEdge(Edge previousEdge) {
		this.previousEdge = previousEdge;
	}

	public Record getPreviousRecord() {
		return previousRecord;
	}

	public void setPreviousRecord(Record previousRecord) {
		this.previousRecord = previousRecord;
	}
}
