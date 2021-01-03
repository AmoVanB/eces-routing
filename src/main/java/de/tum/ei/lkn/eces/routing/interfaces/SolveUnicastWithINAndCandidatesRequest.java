package de.tum.ei.lkn.eces.routing.interfaces;

import de.tum.ei.lkn.eces.routing.requests.UnicastWithINAndCandidatesRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

public interface SolveUnicastWithINAndCandidatesRequest {
	Response solveNoChecks(UnicastWithINAndCandidatesRequest request);
}
