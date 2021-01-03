package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Head Path as defined by A*Prune paper and which are the elements inside the
 * Priority Queue of the Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
class HeadPath implements Comparable<HeadPath> {
	/**
	 * A head path consists of an EDGE...
	 */
	private Edge lastEdge;

	/**
	 * and a head path before this EDGE.
	 */
	private HeadPath sourcePath;

	/**
	 * Source of the Path.
	 * This is necessary when the Path is empty to be able to identify
	 * the possible next Edges.
	 */
	private Node source;

	/**
	 * Cost of the Path.
	 */
	private double cost;

	/**
	 * Values of the constraints for this Path.
	 */
	private double[] constraintsValues;

	/**
	 * Values of the parameters for this Path.
	 */
	private double[] parametersValues;

	/**
	 * Projected cost of the HeadPath.
	 */
	private double projectedCost;

	/**
	 * ID of the Path.
	 */
	private int id;

	/**
	 * Creates a Path.
	 * @param source Source Node of the Path.
	 */
	public HeadPath(Node source) {
		this.lastEdge = null;
		this.sourcePath = null;
		this.source = source;
		this.cost = 0;
		this.constraintsValues = null;
		this.parametersValues = null;
		this.projectedCost = Double.MAX_VALUE;
		this.id = 0;
	}

	/**
	 * Creates a Path.
	 * @param sourcePath First part of the Path.
	 * @param lastEdge New EDGE added to the Path.
	 */
	public HeadPath(HeadPath sourcePath, Edge lastEdge) {
		this.lastEdge = lastEdge;
		this.sourcePath = sourcePath;
		this.source = sourcePath.getSource();
		this.cost = 0;
		this.constraintsValues = null;
		this.parametersValues = null;
		this.projectedCost = Double.MAX_VALUE;
		this.id = 0;
	}

	/**
	 * Returns the HeadPath as a complete Path.
	 * @return The Path Object.
	 */
	public Path getPath() {
		HeadPathIterator iterator = new HeadPathIterator(this);
		return new Path(iterator, this.getCost(), this.getConstraintsValues(), this.getParametersValues());
	}

	/**
	 * Checks if the Path contains twice its last Node.
	 * @return true/false based on whether or not the Path contains twice its
	 *         last Node or not.
	 */
	public boolean containsLastNode() {
		Node node = this.getLastNode();
		HeadPathIterator iterator = new HeadPathIterator(this);
		for (Edge edge : iterator)
			if(edge.getSource() == node)
				return true;
		return false;
	}

	public Node getSource() {
		return source;
	}

	public Node getLastNode() {
		if(this.lastEdge == null)
			return source;
		else
			return this.lastEdge.getDestination();
	}

	public double getProjectedCost() {
		return projectedCost;
	}

	public void setProjectedCost(double projectedCost) {
		this.projectedCost = projectedCost;
	}

	public double[] getParametersValues() {
		return parametersValues;
	}

	public void setParametersValues(double[] parametersValues) {
		this.parametersValues = parametersValues;
	}

	public double[] getConstraintsValues() {
		return constraintsValues;
	}

	public void setConstraintsValues(double[] constraintsValues) {
		this.constraintsValues = constraintsValues;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public HeadPath getSourcePath() {
		return sourcePath;
	}

	public Edge getLastEdge() {
		return lastEdge;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int compareTo(HeadPath other) {
		/* Paths are ordered by increasing value of projected cost.
		 * In case of tie, higher costs come first. */
		if(this.getProjectedCost() < other.getProjectedCost())
			return -1;
		if(this.getProjectedCost() > other.getProjectedCost())
			return 1;
		if(this.getCost() > other.getCost())
			return -1;
		if(this.getCost() < other.getCost())
			return 1;
		if(this.id > other.id)
			return -1;
		if(this.id < other.id)
			return 1;
		else
			return -1;
	}

	@Override
	public String toString() {
		return "HeadPath (last EDGE: " + lastEdge + ") PC: " + projectedCost + " C: " + cost;
	}
}
