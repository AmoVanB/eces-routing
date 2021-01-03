package de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint.simple;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.AgnosticAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.simple.BlockingProxy;
import de.tum.ei.lkn.eces.routing.interfaces.SolveDisjointRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.DisjointRequest;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.DisjointPaths;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple disjoint algorithm.
 * Finds a path, blocks it, and then finds the second one.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SDAlgorithm extends AgnosticAlgorithm implements SolveDisjointRequest {
	private RoutingAlgorithm routingAlgorithm;
	private BlockingProxy blockingProxy;

	public SDAlgorithm(Controller controller, RoutingAlgorithm routingAlgorithm) {
		super(controller);
		this.routingAlgorithm = routingAlgorithm;
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return solveNoChecks((DisjointRequest) request);
	}

	@Override
	public Response solveNoChecks(DisjointRequest request) {
		blockingProxy.unblockAll();

		List<Path> result = new LinkedList<>();
		for(Node destination : request.getDestinations()) {
			Path path = (Path) routingAlgorithm.solve(new UnicastRequest(request.getSource(), destination));
			if(path == null)
				return null;
			else {
				result.add(path);
				blockingProxy.block(path);
			}
		}

		return new DisjointPaths(result);
	}

	@Override
	public boolean isForward() {
		return this.routingAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void setProxy(EdgeProxy edgeProxy) {
		super.setProxy(edgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		setProxy();
	}

	@Override
	public boolean handle(Request request) {
		return request instanceof DisjointRequest && routingAlgorithm.handle(new UnicastRequest(((DisjointRequest) request).getSource(), ((DisjointRequest) request).getDestinations().get(0)));
	}

	@Override
	public MetricTypes getMetricsType() {
		return routingAlgorithm.getMetricsType();
	}

	private void setProxy() {
		blockingProxy = new BlockingProxy(this.proxy);
		routingAlgorithm.setProxy(blockingProxy);
	}
}
