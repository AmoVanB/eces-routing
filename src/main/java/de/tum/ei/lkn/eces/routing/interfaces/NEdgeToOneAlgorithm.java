package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Algorithm able to compute shortest paths (SP) from all source Edges to a
 * given destination Node.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface NEdgeToOneAlgorithm {
	/**
	 * Runs the algorithm.
	 * @param destination Destination Node.
	 * @param request Request for which the algorithm has to run.
	 */
	void computePathsFromAnyEdgeTo(Node destination, Request request);

	/**
	 * Gets the Path obtained by the algorithm from a given Source Edge.
	 * @param source Source Edge of the Path that must be returned.
	 * @return The found shortest path or null if PATH found.
	 * @throws RoutingException if 'computePathsFromAnyEdgeTo' has never been
	 * called before.
	 */
	Path getPathToNodeFrom(Edge source);
}
