package de.tum.ei.lkn.eces.routing;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;

/**
 * Component defining the routing algorithm to use to solve a given request.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class SelectedRoutingAlgorithm extends Component {
	private final RoutingAlgorithm routingAlgorithm;

	public SelectedRoutingAlgorithm(RoutingAlgorithm routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

	public RoutingAlgorithm getRoutingAlgorithm() {
		return routingAlgorithm;
	}
}
