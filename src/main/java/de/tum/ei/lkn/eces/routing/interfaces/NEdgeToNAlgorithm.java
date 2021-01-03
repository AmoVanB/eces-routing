package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Algorithm able to compute shortest paths (SP) from all source Edges to all
 * destination Nodes.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface NEdgeToNAlgorithm {
	/**
	 * Runs the algorithm.
	 * @param request Request for which the algorithm has to run.
	 */
	void computePathsFromAnyEdgeToAnyNode(Request request);

	/**
	 * Gets the Path obtained by the algorithm from a given Source EDGE to a
	 * given destination NODE.
	 * @param source Source EDGE of the Path that must be returned.
	 * @param destination Destination NODE of the Path that must be returned.
	 * @return The found shortest path or null if PATH found.
	 * @throws RoutingException if 'computePathsFromAnyEdgeToAnyNode' has never
	 * been called before.
	 */
	Path getPathFromEdgeToNode(Edge source, Node destination);
}
