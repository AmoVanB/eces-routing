package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

public interface SolveUnicastRequest {
	Response solveNoChecks(UnicastRequest request);
}
