package de.tum.ei.lkn.eces.routing.easygraph;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

/**
 * Metrics of a given edge.
 *
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class Metrics extends Component {
	private double cost;
	private double[] constraints;

	public Metrics(double cost) {
		this.cost = cost;
		this.constraints = new double[0];
	}

	public Metrics(double cost, double[] constraints) {
		this.cost = cost;
		this.constraints = constraints;
	}

	public double[] getConstraints() {
		return constraints;
	}

	public double getCost() {
		return cost;
	}
}
