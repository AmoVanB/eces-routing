package de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.simple;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.AgnosticAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveResilientRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.ResilientRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.ResilientPath;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * Simple resilient algorithm.
 * Finds a path, blocks it, and then finds a second one.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SRAlgorithm extends AgnosticAlgorithm implements SolveResilientRequest {
	private RoutingAlgorithm routingAlgorithm;
	private BlockingProxy blockingProxy;

	public SRAlgorithm(Controller controller, RoutingAlgorithm routingAlgorithm) {
		super(controller);
		this.routingAlgorithm = routingAlgorithm;
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
	public MetricTypes getMetricsType() {
		return routingAlgorithm.getMetricsType();
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
	protected Response solveNoChecks(Request request) {
		return solveNoChecks((ResilientRequest) request);
	}

	private void setProxy() {
		blockingProxy = new BlockingProxy(this.proxy);
		routingAlgorithm.setProxy(blockingProxy);
	}

	@Override
	public Response solveNoChecks(ResilientRequest request) {
		blockingProxy.unblockAll();
		Path path1 = (Path) routingAlgorithm.solve(request);
		if(path1 == null)
			return null;
		blockingProxy.block(path1);
		Path path2 = (Path) routingAlgorithm.solve(request);
		if(path2 == null)
			return null;
		return new ResilientPath(path1, path2);
	}
}
