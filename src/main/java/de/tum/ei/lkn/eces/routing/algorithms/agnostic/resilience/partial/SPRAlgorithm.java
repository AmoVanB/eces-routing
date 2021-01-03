package de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.partial;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.AgnosticAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint.simplepartial.HighCostProxy;
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
 * Simple partial resilient algorithm.
 * Finds a path, allocates it a high cost, and then finds a second one.
 * Disjointness is not guaranteed but maximized through the increased cost.
 *
 * @author Amaury Van Bemten
 */
public class SPRAlgorithm extends AgnosticAlgorithm implements SolveResilientRequest {
	private RoutingAlgorithm routingAlgorithm;
	private HighCostProxy highCostProxy;

	public SPRAlgorithm(Controller controller, RoutingAlgorithm routingAlgorithm) {
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
		highCostProxy = new HighCostProxy(this.proxy);
		routingAlgorithm.setProxy(highCostProxy);
	}

	@Override
	public Response solveNoChecks(ResilientRequest request) {
		highCostProxy.resetCosts();
		Path path1 = (Path) routingAlgorithm.solve(request);
		if(path1 == null)
			return null;
		highCostProxy.increaseCost(path1);
		Path path2 = (Path) routingAlgorithm.solve(request);
		if(path2 == null)
			return null;
		return new ResilientPath(path1, path2);
	}
}
