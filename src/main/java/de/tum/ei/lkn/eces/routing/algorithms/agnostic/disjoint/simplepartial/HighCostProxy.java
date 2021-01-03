package de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint.simplepartial;

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
 * Cost is increased ON link-level (for all queues of the same link).
 */
public class HighCostProxy extends PathProxyWrapper {
	private double MULTIPLIER = 100;
	private Set<Node> highCostNodes = new HashSet<>();
	private Set<Pair<Node, Node>> highCostSourceDestPairs = new HashSet<>();


	public HighCostProxy(PathProxy proxy) {
		super(proxy);
	}
	public HighCostProxy(EdgeProxy proxy) {
		super(proxy);
	}

	public HighCostProxy(PreviousEdgeProxy proxy) {
		super(proxy);
	}

	public void setMultiplier(double multiplier) {
		MULTIPLIER = multiplier;
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] parameters, Request request, boolean isForward) {
		if(highCostNodes.contains(edge.getDestination()) || highCostSourceDestPairs.contains(new Pair<>(edge.getSource(), edge.getDestination())))
			return MULTIPLIER * super.getCost(path, edge, parameters, request, isForward);
		else
			return super.getCost(path, edge, parameters, request, isForward);
	}

	public void increaseCost(Iterable<Edge> path) {
		int i = 0;
		Edge lastEdge = null;
		for (Edge edge : path) {
			// Do no increase cost of source node but increase cost of source edge
			if(i == 0)
				highCostSourceDestPairs.add(new Pair<>(edge.getSource(), edge.getDestination()));
			else
				highCostNodes.add(edge.getSource());

			lastEdge = edge;
			i++;
		}

		// Do not increase cost of destination node but increase cost of destination edge
		highCostSourceDestPairs.add(new Pair<>(lastEdge.getSource(), lastEdge.getDestination()));
	}

	public void resetCosts() {
		highCostNodes.clear();
		highCostSourceDestPairs.clear();
	}
}
