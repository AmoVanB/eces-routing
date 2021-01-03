package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcur;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

/**
 * LocalComponent storing data at a Node for the DCUR algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class DCURNodeDataLocal extends LocalComponent {

	@Override
	public Object init() {
		return new DCURNodeData();
	}
}
