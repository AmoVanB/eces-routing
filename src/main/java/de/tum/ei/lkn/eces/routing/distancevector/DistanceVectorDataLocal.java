package de.tum.ei.lkn.eces.routing.distancevector;

import de.tum.ei.lkn.eces.core.LocalComponent;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;

/**
 * Local component storing data at each node for each different node.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = DistanceVectorSystem.class)
public class DistanceVectorDataLocal extends LocalComponent {
	@Override
	public Object init() {
		return new DistanceVectorData();
	}
}
