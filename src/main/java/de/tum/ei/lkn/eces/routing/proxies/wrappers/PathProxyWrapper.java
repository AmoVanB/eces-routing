package de.tum.ei.lkn.eces.routing.proxies.wrappers;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.requests.Request;

import java.util.Iterator;

/**
 * Wrapper around either an EdgeProxy or a PreviousEdgeProxy.
 * To the user (i.e., to the RoutingAlgorithm) it looks like a PathProxy.
 * The wrapper transforms the calls containing the whole Path to calls
 * containing either only the current Edge (for an EdgeProxy) or the previous
 * Edge (for a PreviousEdgeProxy).
 *
 * This allows RoutingAlgorithms able to deal with the three different types of
 * ProxyTypes to be implemented only once using a PathProxy which will either be
 * a real PathProxy or a PathProxyWrapper.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class PathProxyWrapper extends PathProxy {
	/**
	 * Wrapped proxy.
	 */
	protected Proxy proxy;

	protected PathProxyWrapper() {
	}

	/**
	 * Creates the Wrapper around an EdgeProxy.
	 * @param proxy the EdgeProxy.
	 */
	public PathProxyWrapper(PathProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Creates the Wrapper around an EdgeProxy.
	 * @param proxy the EdgeProxy.
	 */
	public PathProxyWrapper(EdgeProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Creates the Wrapper around a PreviousEdgeProxy.
	 * @param proxy the PreviousEdgeProxy.
	 */
	public PathProxyWrapper(PreviousEdgeProxy proxy) {
		this.proxy = proxy;
	}

	@Override
	public double[] getNewParameters(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).getNewParameters(path, edge, oldParameters, request, isForward);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).getNewParameters(edge, oldParameters, request, isForward);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).getNewParameters(null, edge, oldParameters, request, isForward);
			return ((PreviousEdgeProxy) proxy).getNewParameters(pathIterator.next(), edge, oldParameters, request, isForward);
		}
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).hasAccess(path, edge, parameters, request, isForward);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).hasAccess(edge, request);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).hasAccess(null, edge, request, isForward);
			return ((PreviousEdgeProxy) proxy).hasAccess(pathIterator.next(), edge, request, isForward);
		}
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).getCost(path, edge, parameters, request, isForward);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).getCost(edge, request);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).getCost(null, edge, request, isForward);
			return ((PreviousEdgeProxy) proxy).getCost(pathIterator.next(), edge, request, isForward);
		}
	}

	@Override
	public double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).getConstraintsValues(path, edge, parameters, request, isForward);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).getConstraintsValues(edge, request);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).getConstraintsValues(null, edge, request, isForward);
			return ((PreviousEdgeProxy) proxy).getConstraintsValues(pathIterator.next(), edge, request, isForward);
		}
	}

	@Override
	public boolean register(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).register(path, edge, parameters, request);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).register(edge, request);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).register(null, edge, request);
			return ((PreviousEdgeProxy) proxy).register(pathIterator.next(), edge, request);
		}
	}

	@Override
	public boolean deregister(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		if(proxy instanceof PathProxy)
			return ((PathProxy) proxy).deregister(path, edge, parameters, request);
		else if(proxy instanceof EdgeProxy)
			return ((EdgeProxy) proxy).deregister(edge, request);
		else { // if(proxy instanceof PreviousEdgeProxy) {
			Iterator<Edge> pathIterator = path.iterator();
			if(!pathIterator.hasNext())
				return ((PreviousEdgeProxy) proxy).deregister(null, edge, request);
			return ((PreviousEdgeProxy) proxy).deregister(pathIterator.next(), edge, request);
		}
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
	public int getNumberOfParameters(Request request) {
		return proxy.getNumberOfParameters(request);
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
	public PathProxyWrapper clone() {
		PathProxyWrapper clone = (PathProxyWrapper) super.clone();
		clone.proxy = proxy.clone();
		return clone;

	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.proxy.toString() + ")";
	}
}
