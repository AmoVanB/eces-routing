package de.tum.ei.lkn.eces.routing.algorithms.mcp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * An MCPAlgorithm (i.e. a Multi-Constrained Path Algorithm) is a
 * RoutingAlgorithm able to compute a path subject to several constraints.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class MCPAlgorithm extends RoutingAlgorithm {
	protected MCPAlgorithm(Controller controller) {
		super(controller);
	}

	@Override
	public boolean handle(Request request) {
		return proxy.handle(request, isForward());
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public MetricTypes getMetricsType() {
		return MetricTypes.MCP;
	}
}
