package de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;

import java.util.HashSet;

/**
 * Proxy wrapping an underlying Proxy by adding the functionality that
 * some Edges and Nodes of the Graph can be declared blocked, in which
 * case the Proxy will refuse any access to them.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class BlockingProxy extends PathProxyWrapper {

	/**
	 * List of Edges that cannot be accessed.
	 */
	private HashSet<Edge> blockedEdges = new HashSet<>();

	/**
	 * List of Nodes that cannot be accessed.
	 */
	private HashSet<Node> blockedNodes = new HashSet<>();

	public BlockingProxy(EdgeProxy proxy) {
		super(proxy);
	}

	public BlockingProxy(PreviousEdgeProxy proxy) {
		super(proxy);
	}

	public BlockingProxy(PathProxy proxy) {
		super(proxy);
	}

	/**
	 * Adds an Edge to the list of Edges that cannot be used.
	 * @param edge Edge to add.
	 */
	public void setBlocked(Edge edge) {
		blockedEdges.add(edge);
	}

	/**
	 * Adds a Node to the list of Nodes that cannot be used.
	 * @param node Node to add.
	 */
	public void setBlocked(Node node) {
		blockedNodes.add(node);
	}

	/**
	 * Unblock all the Nodes and Edges.
	 */
	public void unblockAll() {
		blockedEdges.clear();
		blockedNodes.clear();
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		// Deny access to blocked Edges or Nodes.
		if(blockedEdges.contains(edge) || blockedNodes.contains(edge.getDestination()))
			return false;
		return super.hasAccess(path, edge, parameters, request, isForward);
	}

	@Override
	public BlockingProxy clone() {
		BlockingProxy clone = (BlockingProxy) super.clone();
		return clone;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.proxy.toString() + ")";
	}
}
