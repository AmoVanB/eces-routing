package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

/**
 * LocalComponent storing data at a Node for the CBF algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class NodeDataLocal extends LocalComponent {

	@Override
	public Object init() {
		return new NodeData();
	}
}
