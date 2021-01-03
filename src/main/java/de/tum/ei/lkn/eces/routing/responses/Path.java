package de.tum.ei.lkn.eces.routing.responses;

import com.google.common.collect.Iterators;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Component representing a Path in a Graph.
 *
 * Besides the list of Edges forming it, a Path is characterized by
 * - a cost (the goal of a RoutingAlgorithm is to find a Path whose cost is
 *   minimum),
 * - constraints values (depending on the Request, a RoutingAlgorithm has to
 *   find a Path whose constraints values do not exceed the limits imposed
 *   by the Request),
 * - parameters (any other values evolving along the Path and which are not
 *   part of the objective or of the constraints).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class Path extends Response implements Iterable<Edge> {
	/**
	 * Array of Edges forming the Path.
	 */
	private Edge[] edges;

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
	 * Hash code of the Path based on the hashes of the Edges constituting the
	 * Path.
	 */
	private int edgeHash;

	/**
	 * Creates a Path.
	 * @param edges array of Edges forming the Path. It is the responsibility
	 *              of the user to give an array of Edges for which the
	 *              source of EDGE n is the destination of EDGE n-1. This is
	 *              not checked for performance reasons, but the object might
	 *              misbehave if it is not the case.
	 * @param cost Cost of the Path.
	 * @param constraintsValues Values of the constraints for the Path.
	 * @param parametersValues Values of the parameters for the Path.
	 */
	public Path(Edge[] edges, double cost, double[] constraintsValues, double[] parametersValues) {
		this.edges = edges;
		this.cost = cost;
		this.constraintsValues = constraintsValues;
		this.parametersValues = parametersValues;
		this.edgeHash = computeHash();
	}

	/**
	 * Creates a Path.
	 * @param path Iterator over the Path. Both source -> destination and
	 *             destination -> source Iterators are supported. The path
	 *             will however always be created source -> destination.
	 * @param cost Cost of the Path.
	 * @param constraintsValues Values of the constraints for the Path.
	 * @param parametersValues Values of the parameters for the Path.
	 * @throws RoutingException if the Iterable object yields a Path whose
	 *                          Edges are not connected.
	 */
	@Deprecated
	public Path(Iterable<Edge> path, double cost, double[] constraintsValues, double[] parametersValues) {
		this.cost = cost;
		this.constraintsValues = constraintsValues;
		this.parametersValues = parametersValues;
		Iterator<Edge> pathIterator = path.iterator();

		// No Edges?
		if(!pathIterator.hasNext()) {
			this.edges = new Edge[0];
			this.edgeHash = computeHash();
			return;
		}

		// Getting the first two Edges to check the direction of the iterator.
		Edge first = pathIterator.next();
		if(!pathIterator.hasNext()) {
			this.edges = new Edge[]{first};
			this.edgeHash = computeHash();
			return;
		}

		Edge second = pathIterator.next();

		// Now getting the other Edges.
		Vector<Edge> headPath = new Vector<>();
		if(first.getDestination() == second.getSource()) {
			// Iterator is forward.
			headPath.add(first);
			headPath.add(second);
			while(pathIterator.hasNext()) {
				Edge newEdge = pathIterator.next();
				if(newEdge.getSource() != headPath.get(headPath.size() - 1).getDestination())
					throw new RoutingException("The Iterable object yields a Path whose Edges are not connected.");
				headPath.add(newEdge);
			}
		}
		else if(first.getSource() == second.getDestination()) {
			// Iterator is backward.
			headPath.add(second);
			headPath.add(first);
			while(pathIterator.hasNext()) {
				Edge newEdge = pathIterator.next();
				if(newEdge.getDestination() != headPath.get(0).getSource())
					throw new RoutingException("The Iterable object yields a Path whose Edges are not connected.");
				headPath.insertElementAt(newEdge, 0);
			}
		}
		else
			throw new RuntimeException("The Iterable object yields a Path whose Edges are not connected.");

		// Creation of the edge array.
		this.edges = headPath.toArray(new Edge[headPath.size()]);
		this.edgeHash = computeHash();
	}
	/**
	 * Creates a Path.
	 * @param path Iterator over the Path. Both source -> destination and
	 *             destination -> source Iterators are supported. The path
	 *             will however always be created source -> destination.
	 * @param isForward specifies the direction of the iterator.
	 * @param cost Cost of the Path.
	 * @param constraintsValues Values of the constraints for the Path.
	 * @param parametersValues Values of the parameters for the Path.
	 * @throws RoutingException if the Iterable object yields a Path whose
	 *                          Edges are not connected.
	 */
	public Path(Iterable<Edge> path, boolean isForward, double cost, double[] constraintsValues, double[] parametersValues) {
		this.cost = cost;
		this.constraintsValues = constraintsValues;
		this.parametersValues = parametersValues;
		Iterator<Edge> pathIterator = path.iterator();

		// No Edges?
		if(!pathIterator.hasNext()) {
			this.edges = new Edge[0];
			this.edgeHash = computeHash();
			return;
		}
		LinkedList<Edge> headPath = new LinkedList<>();
		headPath.addFirst(pathIterator.next());
		if(!isForward){
			while(pathIterator.hasNext()) {
				Edge newEdge = pathIterator.next();
				if(newEdge.getSource() != headPath.getLast().getDestination())
					throw new RoutingException("The Iterable object yields a Path whose Edges are not connected.");
				headPath.addLast(newEdge);
			}
		} else{
			while(pathIterator.hasNext()) {
				Edge newEdge = pathIterator.next();
				if(newEdge.getDestination() != headPath.getFirst().getSource())
					throw new RoutingException("The Iterable object yields a Path whose Edges are not connected.");
				headPath.addFirst(newEdge);
			}
		}

		// Creation of the EDGE array.
		this.edges = headPath.toArray(new Edge[headPath.size()]);
		this.edgeHash = computeHash();
	}
	/**
	 * Gets the array of Edges corresponding to the Path.
	 * @return the array of Edges.
	 */
	public Edge[] getPath() {
		return edges;
	}

	/**
	 * Gets the cost of the Path.
	 * @return Cost of the Path.
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Gets the values of the constraints for the Path.
	 * @return Values of the constraints for the Path.
	 */
	public double[] getConstraintsValues() {
		return constraintsValues;
	}

	/**
	 * Gets the values of the parameters for the Path.
	 * @return Values of the parameters for the Path.
	 */
	public double[] getParametersValues() {
		return parametersValues;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setConstraintsValues(double[] constraintsValues) {
		this.constraintsValues = constraintsValues;
	}

	public void setParametersValues(double[] parametersValues) {
		this.parametersValues = parametersValues;
	}

	/**
	 * Computes the hash of the Path from the hashes of the constituting
	 * Edges.
	 * @return The hash computed from the hashes of the Edges constituting
	 *         the Path.
	 */

	private int computeHash() {
		final int prime = 31;
		int hash = 1;
		for(Edge edge : edges)
			hash = hash * prime + edge.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for(Edge edge : edges)
			out.append("(").append(edge.getSource()).append(")-[").append(edge).append("]->");
		if(edges.length > 0)
			out.append("(").append(edges[edges.length - 1].getDestination()).append(")");
		else
			return "()";
		return out.toString();
	}

	@Override
	public boolean equals(Object path) {
		if(path instanceof Path)
			if(edgeHash == ((Path) path).edgeHash)
				return true;
		return false;
	}

	@Override
	public Iterator<Edge> iterator() {
		return Iterators.forArray(edges);
	}

	@Override
	public int hashCode() {
		return edgeHash;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = super.toJSONObject();
		result.put("cost", cost);
		result.put("constraints", constraintsValues);
		result.put("parameters", parametersValues);
		JSONArray array = new JSONArray();
		for(Edge edge: edges)
			array.put(edge.getId());

		result.put("edges", array);
		return result;
	}
}
