package de.tum.ei.lkn.eces.routing.algorithms.mcp;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * Proxy removing the cost metric.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NoCostProxy extends PathProxyWrapper {
	public NoCostProxy(PathProxy proxy) {
		super(proxy);
	}

	public NoCostProxy(EdgeProxy proxy) {
		super(proxy);
	}

	public NoCostProxy(PreviousEdgeProxy proxy) {
		super(proxy);
	}

	public void setCostBound(double costBound) {
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward){
		return Double.NaN;
	}

}
