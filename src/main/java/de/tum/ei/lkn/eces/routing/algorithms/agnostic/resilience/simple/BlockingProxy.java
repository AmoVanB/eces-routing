package de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.simple;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.Request;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * Proxy that allows to prevent the usage from a given path.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class BlockingProxy extends PathProxyWrapper {
	private Set<Node> blockedNodes = new HashSet<>();
	private Set<Pair<Node, Node>> blockedSDPairs = new HashSet<>();

	public BlockingProxy(PathProxy proxy) {
		super(proxy);
	}
	public BlockingProxy(EdgeProxy proxy) {
		super(proxy);
	}

	public BlockingProxy(PreviousEdgeProxy proxy) {
		super(proxy);
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		return !blockedNodes.contains(edge.getDestination())
				&& !blockedSDPairs.contains(new Pair<>(edge.getSource(), edge.getDestination()))
				&& super.hasAccess(path, edge, parameters, request, isForward);

	}

	public void block(Iterable<Edge> path) {
		int i = 0;
		Edge lastEdge = null;
		for (Edge edge : path) {
			// Do no block source node but block source edge
			if(i == 0)
				blockedSDPairs.add(new Pair<>(edge.getSource(), edge.getDestination()));
			else
				blockedNodes.add(edge.getSource());

			lastEdge = edge;
			i++;
		}

		// Do not block destination node but block destination edge
		blockedSDPairs.add(new Pair<>(lastEdge.getSource(), lastEdge.getDestination()));
	}

	public void unblockAll() {
		blockedNodes.clear();
		blockedSDPairs.clear();
	}
}
