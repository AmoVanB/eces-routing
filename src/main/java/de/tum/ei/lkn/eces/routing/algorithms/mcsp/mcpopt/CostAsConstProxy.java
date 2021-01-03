package de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * Proxy that transfers the cost as a constant.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class CostAsConstProxy extends PathProxyWrapper {
	private double costBound;

	/**
	 * Creates the Wrapper around an EdgeProxy.
	 * @param proxy the EdgeProxy.
	 */
	public CostAsConstProxy(PathProxy proxy) {
		super(proxy);
	}

	/**
	 * Creates the Wrapper around an EdgeProxy.
	 * @param proxy the EdgeProxy.
	 */
	public CostAsConstProxy(EdgeProxy proxy) {
		super(proxy);
	}

	/**
	 * Creates the Wrapper around a PreviousEdgeProxy.
	 * @param proxy the PreviousEdgeProxy.
	 */
	public CostAsConstProxy(PreviousEdgeProxy proxy) {
		super(proxy);
	}

	public void setCostBound(double costBound) {
		this.costBound = costBound;
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward){
		return Double.NaN;
	}

	@Override
	public double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		double[] result = new double[getNumberOfConstraints(request)];
		System.arraycopy( super.getConstraintsValues(path,edge,parameters,request,isForward), 0, result, 0, super.getNumberOfConstraints(request) );
		result[super.getNumberOfConstraints(request)] = super.getCost(path,edge,parameters,request,isForward);
		return result;
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		double[] result = new double[getNumberOfConstraints(request)];
		System.arraycopy( super.getConstraintsBounds(request), 0, result, 0, super.getNumberOfConstraints(request) );
		result[super.getNumberOfConstraints(request)] = costBound;
		return result;
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return super.getNumberOfConstraints(request) + 1;
	}

}
