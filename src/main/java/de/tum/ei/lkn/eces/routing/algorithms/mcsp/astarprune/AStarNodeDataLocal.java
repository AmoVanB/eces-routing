package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

/**
 * LocalComponent storing data at a Node for the A*Prune algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class AStarNodeDataLocal extends LocalComponent {

	@Override
	public Object init() {
		return new AStarNodeData();
	}
}
