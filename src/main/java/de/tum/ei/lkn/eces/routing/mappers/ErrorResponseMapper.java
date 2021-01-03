package de.tum.ei.lkn.eces.routing.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.routing.responses.ErrorResponse;

public class ErrorResponseMapper extends Mapper<ErrorResponse> {
	public ErrorResponseMapper(Controller controller) {
		super(controller);
	}
}
