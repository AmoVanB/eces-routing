package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

/**
 * Basic routing request component.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public abstract class Request extends Component implements Cloneable {
	@Override
	public Request clone()
	{
			return (Request) super.clone();
	}

	public abstract Graph getGraph();
}
