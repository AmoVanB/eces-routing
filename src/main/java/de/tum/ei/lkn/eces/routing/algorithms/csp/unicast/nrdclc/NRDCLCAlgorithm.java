package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.nrdclc;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.hmcp.HMCPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt.MCPOptimisationAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * The NR_DCLC algorithm.
 *
 * 2002
 * "Performance evaluation of delay-constrained least-cost QoS routing algorithms based on linear and nonlinear
 * lagrange relaxation"
 * G. Feng, C. Douligeris, K. Makki, and N. Pissinou.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NRDCLCAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Multipliers for considering only the cost as optimization metric.
	 */
	private static final double[] leastCostMultipliers = {1, 0};

	/**
	 * Multipliers for considering only the delay as optimization metric.
	 */
	private static final double[] leastDelayMultipliers = {0, 1};

	/**
	 * Underlying shortest path algorithm used.
	 */
	private SPAlgorithm spAlgorithm;

	private MCPOptimisationAlgorithm mcpGapClosingAlgorithm;

	/**
	 * Plumber proxy given to the underlying SPAlgorithm.
	 */
	private PathPlumberProxy plumberProxy;

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	public NRDCLCAlgorithm(Controller controller, boolean guess) {
		super(controller);
		if(guess) {
			this.spAlgorithm = new AStarAlgorithm(controller);
			((AStarAlgorithm) this.spAlgorithm).setMaximumProxy(ProxyTypes.EDGE_PROXY);
		}
		else {
			this.spAlgorithm = new DijkstraAlgorithm(controller);
			((DijkstraAlgorithm) this.spAlgorithm).setMaximumProxy(ProxyTypes.EDGE_PROXY);
		}

		this.mcpGapClosingAlgorithm = new MCPOptimisationAlgorithm(controller, new HMCPAlgorithm(controller, guess));
	}

	public NRDCLCAlgorithm(Controller controller) {
		this(controller, true);
	}

	public NRDCLCAlgorithm(Controller controller, SPAlgorithm spAlgorithm, boolean guess) {
		super(controller);
		this.spAlgorithm = spAlgorithm;
		this.mcpGapClosingAlgorithm = new MCPOptimisationAlgorithm(controller, new HMCPAlgorithm(controller, guess));
	}

	public NRDCLCAlgorithm(Controller controller, SPAlgorithm spAlgorithm) {
		this(controller, spAlgorithm, true);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	protected Path computePath(UnicastRequest request) {
		// Steps are document wrt 1980 paper pp. 300-301.
		int costParameterID = this.plumberProxy.getPlumberParameterId(0, request);
		int delayParameterID = this.plumberProxy.getPlumberParameterId(1, request);
		// Return LC path if there are no constraints.
		double deadline;
		if (proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];
		else{
			this.plumberProxy.setCostMultipliers(leastCostMultipliers);
			return (Path) ((SolveUnicastRequest)spAlgorithm).solveNoChecks(request);
		}

		// LC run
		this.plumberProxy.setCostMultipliers(leastCostMultipliers);
		Path lcPath = (Path) this.spAlgorithm.solve(request);
		if(lcPath == null)
			return null;
		else if(!Proxy.violatesBound(lcPath.getParametersValues()[delayParameterID], deadline))
			return proxy.createPath(lcPath, request, true);

		// LD run
		this.plumberProxy.setCostMultipliers(leastDelayMultipliers);
		Path ldPath;
		if(BDFeature)
			ldPath = (Path) ((BD) this.spAlgorithm).solve(request, deadline);
		else
			ldPath = (Path) this.spAlgorithm.solve(request);
		if(ldPath == null || Proxy.violatesBound(ldPath.getParametersValues()[delayParameterID] , deadline))
			return null;

		Path bestUnfeasiblePath = lcPath;
		double bestUnfeasiblePathCost = bestUnfeasiblePath.getParametersValues()[costParameterID];

		Path bestFeasiblePath = ldPath;
		double bestFeasiblePathCost = bestFeasiblePath.getParametersValues()[costParameterID];

		if(!Proxy.fuzzyEquals(bestFeasiblePathCost, bestUnfeasiblePathCost)) {
			Path result = mcpGapClosingAlgorithm.optimizePath(request, bestFeasiblePathCost, BDFeature);
			if (result != null)
				return proxy.createPath(result, request,true);
		}

		return proxy.createPath(bestFeasiblePath, request,true);
	}

	@Override
	public boolean isForward() {
		return false;
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return this.spAlgorithm.isComplete();
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

	private void setProxy() {
		/* The plumber proxy has
		 * - both cost and delay as cost components (2nd arg),
		 * - initially only cost taken into account (3rd arg),
		 * - no constraints (4th arg),
		 * - both cost and delay as additional parameters (5th arg). */
		this.plumberProxy = new PathPlumberProxy(this.proxy, new int[]{0, 1}, new double[]{1, 0}, new int[0], new int[]{0, 1});
		if(spAlgorithm != null)
			spAlgorithm.setProxy(plumberProxy);
		mcpGapClosingAlgorithm.setProxy(proxy);
	}

	public void enableBD() {
		this.BDFeature = true;
	}

	public void disableBD() {
		this.BDFeature = false;
	}
}
