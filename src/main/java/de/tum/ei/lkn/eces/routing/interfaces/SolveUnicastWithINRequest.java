package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

public interface SolveUnicastWithINRequest {
	Response solveNoChecks(UnicastWithINRequest request);
}
