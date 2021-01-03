package de.tum.ei.lkn.eces.routing.proxies;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * Implementation of a Proxy handling 0 constraints, 0 additive parameters and
 * setting the cost of an EDGE to a constant value. This Proxy can thus be used
 * by an SP algorithm to find shortest paths (in terms of hop count).
 *
 * No resources are reserved by the Proxy.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ShortestPathProxy extends EdgeProxy {
	/**
	 * Cost assigned to each EDGE.
	 */
	private final double cost;

	public ShortestPathProxy() {
		this.cost = 1;
	}

	public ShortestPathProxy(double cost) {
		this.cost = cost;
	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return true;
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return 0;
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return 0;
	}

	@Override
	public double[] getNewParameters(Edge edge, double[] oldParameters, Request request, boolean isForward) {
		return new double[0]; // No parameters.
	}

	@Override
	public boolean hasAccess(Edge edge, Request request) {
		return true;
	}

	@Override
	public double getCost(Edge edge, Request request) {
		return cost;
	}

	@Override
	public double[] getConstraintsValues(Edge edge, Request request) {
		return new double[0]; // No constraints.
	}

	@Override
	public boolean register(Edge edge, Request request) {
		return true;
	}

	@Override
	public boolean deregister(Edge edge, Request request) {
		return true;
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		return new double[0]; // No constraints.
	}
}
