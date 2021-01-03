package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcur;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Data stored at each Node during the DCUR algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCURNodeData {
	/**
	 * Node to  which the data is attached.
	 */
	private Node node;

	/**
	 * Whether the Node as chosen the least-delay (LD) or least-cost (LC)
	 * Path for the next hop.
	 */
	private boolean chosenLD;

	/**
	 * Delay of the Path from the source until the current Node.
	 */
	private double delaySoFar;

	/**
	 * Cost of the Path from the source until the current Node.
	 */
	private double costSoFar;

	/**
	 * Parameters of the Path from the source until the current Node.
	 */
	private double[] parametersSoFar;

	/**
	 * Whether or not the current Node has already been traversed by DCUR.
	 */
	private boolean traversed;

	/**
	 * Set of Edges that are not to be used by the Node (to avoid loops).
	 */
	private Set<Edge> invalidEdges;

	public DCURNodeData() {
		init();
	}

	public void init() {
		node = null;
		chosenLD = false;
		delaySoFar = 0;
		costSoFar = 0;
		parametersSoFar = null;
		traversed = false;
		invalidEdges = new HashSet<>();
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean hasChosenLD() {
		return chosenLD;
	}

	public boolean hasChosenLC() {
		return !chosenLD;
	}

	public void setChosenLD() {
		chosenLD = true;
	}

	public void setChosenLC() {
		chosenLD = false;
	}

	public double getDelaySoFar() {
		return delaySoFar;
	}

	public void setDelaySoFar(double delay) {
		delaySoFar = delay;
	}

	public double getCostSoFar() {
		return costSoFar;
	}

	public void setCostSoFar(double cost) {
		costSoFar = cost;
	}

	public void setParametersSoFar(double[] parameters) {
		this.parametersSoFar = parameters;
	}

	public double[] getParametersSoFar() {
		return parametersSoFar;
	}

	public void addInvalidEdge(Edge edge) {
		invalidEdges.add(edge);
	}

	public boolean isInvalid(Edge edge) {
		return invalidEdges.contains(edge);
	}

	public boolean isTraversed() {
		return traversed;
	}

	public void setTraversed(boolean traversed) {
		this.traversed = traversed;
	}
}
