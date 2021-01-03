package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.ResilientRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

public interface SolveResilientRequest {
	Response solveNoChecks(ResilientRequest request);
}
