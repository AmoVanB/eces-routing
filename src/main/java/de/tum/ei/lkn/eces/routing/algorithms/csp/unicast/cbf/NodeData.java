package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf;

import de.tum.ei.lkn.eces.graph.Node;

/**
 * Data stored at each Node during the CBF algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NodeData {
	/**
	 * Node to  which the data is attached.
	 */
	private Node node;

	/**
	 * Cost to reach the Node.
	 */
	private double cost;

	/**
	 * Delay to reach the Node.
	 */
	private double delay;

	/**
	 * Parameters values to reach the Node.
	 */
	private double[] parameters;

	/**
	 * Path used to reach the Node.
	 */
	private EdgePath pathSoFar;

	public NodeData() {
		init();
	}

	public void init() {
		node = null;
		cost = Double.MAX_VALUE;
		delay = Double.MAX_VALUE;
		parameters = null;
		pathSoFar = null;
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

	public void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	public double[] getParameters() {
		return parameters;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public EdgePath getPathSoFar() {
		return pathSoFar;
	}

	public void setPathSoFar(EdgePath pathSoFar) {
		this.pathSoFar = pathSoFar;
	}
}
