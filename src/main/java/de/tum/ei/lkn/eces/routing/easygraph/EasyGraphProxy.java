package de.tum.ei.lkn.eces.routing.easygraph;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.exceptions.ProxyException;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * Proxy for an easy graph implementation: metric values are simply retried from a Metrics
 * object attached to an edge.
 *
 * @author Amaury Van Bemten
 */
public class EasyGraphProxy extends EdgeProxy {
	private double[] constraintsBounds;
	private MetricsMapper mapper;

	public EasyGraphProxy(Controller controller) {
		this(controller, new double[0]);
	}

	public EasyGraphProxy(Controller controller, double[] constraintsBounds) {
		this.constraintsBounds = constraintsBounds;
		this.mapper = new MetricsMapper(controller);
	}

	@Override
	public double[] getNewParameters(Edge edge, double[] oldParameters, Request request, boolean isForward) {
		return new double[0];
	}

	@Override
	public boolean hasAccess(Edge edge, Request request) {
		double[] constraintsValues = mapper.get(edge.getEntity()).getConstraints();

		if (constraintsValues.length != constraintsBounds.length)
			throw new ProxyException("The bounds array does not have the same size as the values");

		for (int i = 0; i < constraintsValues.length; i++)
			if (constraintsValues[i] > constraintsBounds[i])
				return false;

		return true;
	}

	@Override
	public double getCost(Edge edge, Request request) {
		return mapper.get(edge.getEntity()).getCost();
	}

	@Override
	public double[] getConstraintsValues(Edge edge, Request request) {
		return mapper.get(edge.getEntity()).getConstraints();
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
	public boolean register(Response response, Request request) {
		return true;
	}

	@Override
	public boolean deregister(Response response, Request request) {
		return true;
	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return true;
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return this.constraintsBounds.length;
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return 0;
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		return this.constraintsBounds;
	}

	public void setConstraintsBounds(double[] bounds) {
		this.constraintsBounds = bounds;
	}
}
