package de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.MCPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * Implementation of the optimization strategy (used by NR_DCLC and MH_MCOP) which tries to solve
 * a MCSP problem by repeatedly running an MCP algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MCPOptimisationAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest {
	private boolean doBinarySearch = false;
	private MCPAlgorithm mcpAlgorithm;
	private CostAsConstProxy noCostProxy;

	public MCPOptimisationAlgorithm(Controller controller, MCPAlgorithm mcpAlgorithm) {
		super(controller);
		this.mcpAlgorithm = mcpAlgorithm;
	}

	public Path optimizePath(UnicastRequest request, double bestFeasiblePathCost, boolean bd) {
		return  optimizePath(request, bestFeasiblePathCost, 0, bd);
	}

	public Path optimizePath(UnicastRequest request, double bestFeasiblePathCost, double unFeasiblePathCost, boolean bd) {
		Path bestFeasiblePath = null;
		Path result = null;

		if(bd)
			((BDifiable) mcpAlgorithm).enableBD();
		else if(mcpAlgorithm instanceof BDifiable)
			((BDifiable) mcpAlgorithm).disableBD();

		if(doBinarySearch) {
			do {
				double newCostBound = (bestFeasiblePathCost + unFeasiblePathCost) / 2;
				this.noCostProxy.setCostBound(newCostBound);
				result = (Path) this.mcpAlgorithm.solve(request);
				if(result != null){
					bestFeasiblePath = result;
					bestFeasiblePathCost = bestFeasiblePath.getConstraintsValues()[bestFeasiblePath.getConstraintsValues().length - 1];
				} else{
					unFeasiblePathCost = newCostBound;
				}
			}while (Math.abs(bestFeasiblePathCost - unFeasiblePathCost) > 10E-7);
		}
		else {
			do {
				this.noCostProxy.setCostBound(bestFeasiblePathCost * (1 - 10E-7));
				result = (Path) this.mcpAlgorithm.solve(request);
				if(result != null) {
					bestFeasiblePath = result;
					bestFeasiblePathCost = bestFeasiblePath.getConstraintsValues()[bestFeasiblePath.getConstraintsValues().length - 1];
				}
			}
			while (result != null);
		}
		return bestFeasiblePath;
	}

	public void enableBinarySearch(){
		doBinarySearch = true;
	}

	public void disableBinarySearch(){
		doBinarySearch = false;
	}

	public void setProxy(EdgeProxy edgeProxy) {
		super.setProxy(edgeProxy);
		this.noCostProxy = new CostAsConstProxy(edgeProxy);
		mcpAlgorithm.setProxy(noCostProxy);
	}

	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		this.noCostProxy = new CostAsConstProxy(previousEdgeProxy);
		mcpAlgorithm.setProxy(noCostProxy);
	}

	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		this.noCostProxy = new CostAsConstProxy(pathProxy);
		mcpAlgorithm.setProxy(noCostProxy);;
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		this.noCostProxy.setCostBound(Double.MAX_VALUE);
		Path result = (Path) this.mcpAlgorithm.solve(request);
		if(result == null)
			return null;
		Path optpath = optimizePath(request,
				result.getConstraintsValues()[result.getConstraintsValues().length -1],
				proxy.getGuessForCost((request).getSource(), request.getDestination()),
				false);
		if(optpath == null)
			return proxy.createPath(result, request, true);
		return proxy.createPath(optpath, request, true);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return null;
	}

	@Override
	public boolean isForward() {
		return this.mcpAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return this.mcpAlgorithm.isComplete();
	}

	@Override
	public boolean isComplete() {
		return this.mcpAlgorithm.isComplete();
	}

	@Override
	public boolean isValid() {
		return this.mcpAlgorithm.isValid();
	}

}
