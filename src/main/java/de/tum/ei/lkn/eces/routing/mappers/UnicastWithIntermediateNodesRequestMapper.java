package de.tum.ei.lkn.eces.routing.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;

public class UnicastWithIntermediateNodesRequestMapper extends Mapper<UnicastWithINRequest> {
	public UnicastWithIntermediateNodesRequestMapper(Controller controller) {
		super(controller);
	}
}
