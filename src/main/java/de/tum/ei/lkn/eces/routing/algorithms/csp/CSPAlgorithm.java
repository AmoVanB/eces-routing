package de.tum.ei.lkn.eces.routing.algorithms.csp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;

/**
 * A CSPAlgorithm (i.e. a Constrained Shortest Path Algorithm) is a
 * RoutingAlgorithm able to compute a shortest path (in terms of a single
 * objective to minimize) subject to a single constraint.
 *
 * A CSPAlgorithm is hence also a SPAlgorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class CSPAlgorithm extends SPAlgorithm {
	protected CSPAlgorithm(Controller controller) {
		super(controller);
	}

	@Override
	public MetricTypes getMetricsType() {
		return MetricTypes.CSP;
	}
}
