package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Algorithm able to compute shortest paths (SP) from one source Edge to all
 * destination Nodes. This allows to force the algorithm to use a given first
 * Edge.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface OneEdgeToNAlgorithm {
	/**
	 * Runs the algorithm.
	 * @param source Source Edge to start the algorithm.
	 * @param request Request for which the algorithm has to run.
	 */
	void computePathsToAnyNodeFrom(Edge source, Request request);

	/**
	 * Gets the Path obtained by the algorithm to a given destination.
	 * @param destination Destination of the Path that must be returned.
	 * @return The found shortest path or null if PATH found.
	 * @throws RoutingException if 'computePathsToAnyNodeFrom' has never been
	 * called before.
	 */
	Path getPathFromEdgeTo(Node destination);
}
