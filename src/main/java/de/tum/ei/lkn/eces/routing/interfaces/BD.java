package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * Bounded Dijkstra (BD) algorithm.
 *
 * See: "Bounded Dijkstra (BD): Search Space Reduction for Expediting Shortest Path Subroutines" A Van Bemten, JW Guck,
 * CM Machuca, W Kellerer. 2019.
 */
public interface BD {
	Response solve(Request request, double costBorder);
	void setCostBorder(double costBorder);
	void removeCostBorder();
}
