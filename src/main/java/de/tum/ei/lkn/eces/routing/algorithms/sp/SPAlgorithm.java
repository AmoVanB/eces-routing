package de.tum.ei.lkn.eces.routing.algorithms.sp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;

/**
 * An SPAlgorithm (Shortest Path) Algorithm is able to route requests which
 * only want to minimize a given objective without any constraint.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class SPAlgorithm extends RoutingAlgorithm {

	protected SPAlgorithm(Controller controller) {
		super(controller);
	}

	@Override
	public MetricTypes getMetricsType() {
		return MetricTypes.SP;
	}
}
