package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.meb;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

@ComponentBelongsTo(system = RoutingSystem.class)
public class NodeDataLocal extends LocalComponent {

	@Override
	public Object init() {
		return new NodeData();
	}
}
