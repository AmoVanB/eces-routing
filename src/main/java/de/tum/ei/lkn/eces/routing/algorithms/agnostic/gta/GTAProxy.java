package de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Proxy for a transformed graph.
 *
 * Does not support guess right now. Proven by jg
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class GTAProxy extends PathProxy {
	private final static Iterable<Edge> EMPTY_PATH = new LinkedList<>();
	/**
	 * Wrapped proxy.
	 */
	protected PathProxy proxy;

	/**
	 * Original request for the non-transformed graph.
	 */
	private Request originalRequest;

	public GTAProxy(PathProxy proxy) {
		this.proxy = proxy;
	}

	public GTAProxy(EdgeProxy proxy) {
		this.proxy = new PathProxyWrapper(proxy);
	}

	public GTAProxy(PreviousEdgeProxy proxy) {
		this.proxy = new PathProxyWrapper(proxy);
	}

	/**
	 * Sets the original request from the original graph.
	 * @param originalRequest original request from the original
	 *                        graph.
	 */
	public void setOriginalRequest(Request originalRequest) {
		this.originalRequest = originalRequest;
	}

	@Override
	public double[] getNewParameters(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		Edge originalEdge = this.getEdgeInOriginalGraph(edge);

		if(originalEdge == null) {
			return oldParameters;
		}

		switch(proxy.getType()) {
			case EDGE_PROXY:
				return proxy.getNewParameters(
						EMPTY_PATH,
						originalEdge,
						oldParameters,
						originalRequest,
						isForward
				);

			case PREVIOUS_EDGE_PROXY:
				Iterator<Edge> iterator = path.iterator();
				if(iterator.hasNext())
					return proxy.getNewParameters(
							Collections.singletonList(getEdgeInOriginalGraph(iterator.next())),
							originalEdge,
							oldParameters,
							originalRequest,
							isForward
					);
				else
					return proxy.getNewParameters(
							EMPTY_PATH,
							originalEdge,
							oldParameters,
							originalRequest,
							isForward
					);

			default:
				// Path Proxy
				return proxy.getNewParameters(
						getPathInOriginalGraph(path),
						originalEdge,
						oldParameters,
						originalRequest,
						isForward
				);
		}
	}


	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		Edge originalEdge = this.getEdgeInOriginalGraph(edge);

		if(originalEdge == null)
			return true;

		switch(proxy.getType()) {
			case EDGE_PROXY:
				return proxy.hasAccess(
						EMPTY_PATH,
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);

			case PREVIOUS_EDGE_PROXY:
				Iterator<Edge> iterator = path.iterator();
				if(iterator.hasNext())
					return proxy.hasAccess(
							Collections.singletonList(getEdgeInOriginalGraph(iterator.next())),
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);
				else
					return proxy.hasAccess(
							EMPTY_PATH,
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);

			default:
				// Path Proxy
				return proxy.hasAccess(
						getPathInOriginalGraph(path),
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);
		}
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		Edge originalEdge = this.getEdgeInOriginalGraph(edge);

		if(originalEdge == null)
			return 0;

		switch(proxy.getType()) {
			case EDGE_PROXY:
				return proxy.getCost(
						EMPTY_PATH,
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);

			case PREVIOUS_EDGE_PROXY:
				Iterator<Edge> iterator = path.iterator();
				if(iterator.hasNext())
					return proxy.getCost(
							Collections.singletonList(getEdgeInOriginalGraph(iterator.next())),
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);
				else
					return proxy.getCost(
							EMPTY_PATH,
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);

			default:
				// Path Proxy
				return proxy.getCost(
						getPathInOriginalGraph(path),
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);
		}
	}

	@Override
	public double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		Edge originalEdge = this.getEdgeInOriginalGraph(edge);

		if(originalEdge == null)
			return new double[this.getNumberOfConstraints(request)];

		switch(proxy.getType()) {
			case EDGE_PROXY:
				return proxy.getConstraintsValues(
						EMPTY_PATH,
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);

			case PREVIOUS_EDGE_PROXY:
				Iterator<Edge> iterator = path.iterator();
				if(iterator.hasNext())
					return proxy.getConstraintsValues(
							Collections.singletonList(getEdgeInOriginalGraph(iterator.next())),
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);
				else
					return proxy.getConstraintsValues(
							EMPTY_PATH,
							originalEdge,
							parameters,
							originalRequest,
							isForward
					);

			default:
				// Path Proxy
				return proxy.getConstraintsValues(
						getPathInOriginalGraph(path),
						originalEdge,
						parameters,
						originalRequest,
						isForward
				);
		}
	}

	@Override
	public boolean register(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		throw new RuntimeException("Use the original proxy for registration");
	}

	@Override
	public boolean deregister(Iterable<Edge> path, Edge edge, double[] parameters, Request request) {
		throw new RuntimeException("Use the original proxy for deregistration");
	}

	@Override
	public ProxyTypes getType() {
		return this.proxy.getType();
	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return proxy.handle(originalRequest, isForward);
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return proxy.getNumberOfConstraints(originalRequest);
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return proxy.getNumberOfParameters(originalRequest);
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		return proxy.getConstraintsBounds(originalRequest);
	}

	/**
	 * Gets the path in the original graph from a path in the
	 * transformed graph.
	 * @param path Path in the transformed graph.
	 * @return Path in the original graph.
	 */
	protected Iterable<Edge> getPathInOriginalGraph(Iterable<Edge> path) {
		return StreamSupport.stream(path.spliterator(), false)
				.map(this::getEdgeInOriginalGraph).filter(Objects::nonNull)
				.collect(Collectors.toList());
		/* filter(Objects::nonNull):
		 *    Skip edges which are null.
		 *    Edges with no corresponding original edge would lead to this.
		 *    This would corresponding to edges between layers, which indeed
		 *    have to be skipped for creating the path in the original graph.
		 */
	}

	/**
	 * Gets the edge in the original graph corresponding to an
	 * edge from the transformed graph.
	 * @param edge Edge from the transformed graph.
	 * @return Corresponding edge from the original graph.
	 */
	protected Edge getEdgeInOriginalGraph(Edge edge) {
		return ((TransformedEdge) edge).getOriginalEdge();
	}

	@Override
	public String toString() {
		return "GTAProxy(" + this.proxy.toString() + ")";
	}
}
