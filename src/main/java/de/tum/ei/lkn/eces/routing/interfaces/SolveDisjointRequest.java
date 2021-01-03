package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.DisjointRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

public interface SolveDisjointRequest {
	Response solveNoChecks(DisjointRequest request);
}
