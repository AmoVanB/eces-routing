package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf;

import de.tum.ei.lkn.eces.graph.Edge;

import java.util.Iterator;

/**
 * Recursive implementation of a list of Edges.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class EdgePath implements Iterable<Edge>, Iterator<Edge> {
	/**
	 * Current Edge.
	 */
	private Edge edge;

	/**
	 * Previous Edges.
	 */
	private EdgePath previousEdges;

	public EdgePath(Edge edge, EdgePath previousEdges) {
		this.edge = edge;
		this.previousEdges = previousEdges;
	}

	public Edge getEdge() {
		return edge;
	}

	public EdgePath getPreviousEdges() {
		return previousEdges;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new EdgePath(edge, previousEdges);
	}

	@Override
	public boolean hasNext() {
		return edge != null;
	}

	@Override
	public Edge next() {
		Edge edge = this.edge;
		if(this.previousEdges != null) {
			this.edge = previousEdges.getEdge();
			this.previousEdges = previousEdges.getPreviousEdges();
		}
		else
			this.edge = null;

		return edge;
	}

	@Override
	public String toString() {
		if(previousEdges == null)
			return edge.toString();
		else
			return previousEdges.toString() + " -- " + edge.toString();
	}
}
