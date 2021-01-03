package de.tum.ei.lkn.eces.routing.proxies.wrappers;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * Wrapper around an EdgeProxy. To the user (i.e., to the RoutingAlgorithm) it
 * looks like a PreviousEdgeProxy. The wrapper transforms the calls containing
 * the previous EDGE to calls containing only the current EDGE (for an
 * EdgeProxy).
 *
 * This allows RoutingAlgorithms able to deal with both EdgeProxies and
 * PreviousEdgeProxies to be implemented only once using a PreviousEdgeProxy
 * which will either be a real PreviousEdgeProxy or a PreviousEdgeProxyWrapper.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public final class PreviousEdgeProxyWrapper extends PreviousEdgeProxy {
	/**
	 * Wrapped proxy.
	 */
	private EdgeProxy proxy;

	/**
	 * Creates the Wrapper around an EdgeProxy.
	 * @param proxy the EdgeProxy.
	 */
	public PreviousEdgeProxyWrapper(EdgeProxy proxy) {
		this.proxy = proxy;
	}

	@Override
	public double[] getNewParameters(Edge previousEdge, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		return proxy.getNewParameters(edge, oldParameters, request, isForward);
	}

	@Override
	public boolean hasAccess(Edge previousEdge, Edge edge, Request request, boolean isForward) {
		return proxy.hasAccess(edge, request);
	}

	@Override
	public double getCost(Edge previousEdge, Edge edge, Request request, boolean isForward) {
		return proxy.getCost(edge, request);
	}

	@Override
	public double[] getConstraintsValues(Edge previousEdge, Edge edge, Request request, boolean isForward) {
		return proxy.getConstraintsValues(edge, request);
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return proxy.getNumberOfParameters(request);
	}

	@Override
	public boolean register(Edge previousEdge, Edge edge, Request request) {
		return ((EdgeProxy) proxy).register(edge, request);
	}

	@Override
	public boolean deregister(Edge previousEdge, Edge edge, Request request) {
		return ((EdgeProxy) proxy).deregister(edge, request);
	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return proxy.handle(request, isForward);
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return proxy.getNumberOfConstraints(request);
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		return proxy.getConstraintsBounds(request);
	}

	@Override
	public double getGuessForCost(Node source, Node destination) {
		return proxy.getGuessForCost(source, destination);
	}

	@Override
	public double getGuessForConstraint(int index, Node source, Node destination) {
		return proxy.getGuessForConstraint(index, source, destination);
	}

	@Override
	public ProxyTypes getType() {
		return proxy.getType();
	}

	@Override
	public Proxy getProxy() {
		return this.proxy.getProxy();
	}

	@Override
	public PreviousEdgeProxyWrapper clone() {
		PreviousEdgeProxyWrapper clone = (PreviousEdgeProxyWrapper) super.clone();
		clone.proxy = proxy.clone();
		return clone;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.proxy.toString() + ")";
	}
}
