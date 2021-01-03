package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * Algorithm able to compute shortest paths (SP) from one source NODE to all
 * destinations.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface OneToNAlgorithm {
	/**
	 * Runs the algorithm.
	 * @param source Source to start the algorithm.
	 * @param request Request for which the algorithm has to run.
	 */
	void computePathsToAnyNodeFrom(Node source, Request request);

	/**
	 * Gets the Path obtained by the algorithm to a given destination.
	 * @param destination Destination of the Path that must be returned.
	 * @return The found shortest path or null if PATH found.
	 * @throws RoutingException if 'computePathsToAnyNodeFrom' has never been
	 * called before.
	 */
	Path getPathFromNodeTo(Node destination);
}
