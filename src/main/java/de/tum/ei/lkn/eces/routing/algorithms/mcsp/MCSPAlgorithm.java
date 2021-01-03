package de.tum.ei.lkn.eces.routing.algorithms.mcsp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * A MCSPAlgorithm (i.e. a Multi-Constrained Shortest Path Algorithm) is a
 * RoutingAlgorithm able to compute a shortest path (in terms of a single
 * objective to minimize) subject to any number of constraints.
 *
 * A MCSPAlgorithm is hence also a (C)SPAlgorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class MCSPAlgorithm extends CSPAlgorithm {
	protected MCSPAlgorithm(Controller controller) {
		super(controller);
	}

	@Override
	public boolean handle(Request request) {
		return super.handle(request) &&
				(this.getProxy().handle(request, isForward()) &&
						this.getProxy().getNumberOfConstraints(request) >= 0);
	}

	@Override
	public MetricTypes getMetricsType() {
		return MetricTypes.MCSP;
	}
}
