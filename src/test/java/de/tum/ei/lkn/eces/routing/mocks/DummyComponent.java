package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

@ComponentBelongsTo(system = RoutingSystem.class)
public class DummyComponent extends Component {
	public double cost;
	public double delay;
	public double loss;
	public boolean use;
	public int count;

	public DummyComponent() {
		this(1, 0.1, 0, true, 0);
	}

	public DummyComponent(double cost, double delay, double loss, boolean use, int count) {
		this.cost = cost;
		this.delay = delay;
		this.loss = loss;
		this.use = use;
		this.count = count;
	}

	public void setUnusable() {
		use = false;
	}
}
