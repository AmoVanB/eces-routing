package de.tum.ei.lkn.eces.routing.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;

public class UnicastRequestMapper extends Mapper<UnicastRequest> {
	public UnicastRequestMapper(Controller controller) {
		super(controller);
	}
}
