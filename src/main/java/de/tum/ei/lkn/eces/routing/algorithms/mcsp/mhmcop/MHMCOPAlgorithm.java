package de.tum.ei.lkn.eces.routing.algorithms.mcsp.mhmcop;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.hmcp.HMCPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop.HMCOPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt.MCPOptimisationAlgorithm;
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
 * The MH_MCOP algorithm.
 *
 * 2002
 * "Heuristic and exact algorithms for QoS routing with multiple constraints"
 * G. Feng, K. Makki, N. Pissinou, and C. Douligeris,
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MHMCOPAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest, BDifiable {
	private HMCOPAlgorithm mcopAlgorithm;
	private MCPOptimisationAlgorithm mcpGapClosingAlgorithm;
	private boolean BDfeature = false;

	public MHMCOPAlgorithm(Controller controller) {
		this(controller, new HMCOPAlgorithm(controller, Double.POSITIVE_INFINITY));
	}

	public MHMCOPAlgorithm(Controller controller, HMCOPAlgorithm mcopAlgorithm) {
		super(controller);
		this.mcpGapClosingAlgorithm = new MCPOptimisationAlgorithm(controller, new HMCPAlgorithm(controller, false));
		this.mcopAlgorithm = mcopAlgorithm;
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}
	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath( request);
	}

	protected Path computePath(UnicastRequest request) {
		Path bestFeasiblePath = mcopAlgorithm.computePath(request);
		if(bestFeasiblePath == null)
			return null;
		Path result = mcpGapClosingAlgorithm.optimizePath(request, bestFeasiblePath.getCost(), BDfeature);
		if(result == null) {
			return proxy.createPath(
					bestFeasiblePath,
					request,
					true);
		}else {
			return proxy.createPath(
					result,
					request,
					true);
		}
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
		return this.mcopAlgorithm.isComplete();
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

	protected void setProxy() {
		mcpGapClosingAlgorithm.setProxy(proxy);
		mcopAlgorithm.setProxy(proxy);
	}

	@Override
	public void enableBD() {
		mcopAlgorithm.enableBD();
		BDfeature = true;
	}

	@Override
	public void disableBD() {
		mcopAlgorithm.disableBD();
		BDfeature = false;
	}
}
