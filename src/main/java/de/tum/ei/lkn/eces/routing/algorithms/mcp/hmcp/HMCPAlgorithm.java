package de.tum.ei.lkn.eces.routing.algorithms.mcp.hmcp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.MCPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.NoCostProxy;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop.HMCOPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.LinkedList;

/**
 * The H_MCP algorithm.
 *
 * 2001
 * "Multi-constrained optimal path selection"
 * T. Korkmaz and M. Krunz.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HMCPAlgorithm extends MCPAlgorithm implements SolveUnicastRequest, BDifiable {
	private HMCPImplementation hmcpImplementation;

	public HMCPAlgorithm(Controller controller, boolean guess) {
		this(controller, guess, Double.POSITIVE_INFINITY);
	}

	public HMCPAlgorithm(Controller controller, boolean guess, double lambda) {
		super(controller);
		hmcpImplementation = new HMCPImplementation(controller, lambda, guess, ProxyTypes.EDGE_PROXY, 1);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return hmcpImplementation.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return hmcpImplementation.computePath(request);
	}

	@Override
	public boolean isForward() {
		return hmcpImplementation.isForward();
	}

	@Override
	public boolean isComplete() {
		return hmcpImplementation.isComplete();
	}

	@Override
	public boolean isValid() {
		return hmcpImplementation.isValid();
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
		hmcpImplementation.setProxy(new NoCostProxy(this.proxy));
	}

	@Override
	public void enableBD() {
		hmcpImplementation.enableBD();
	}

	@Override
	public void disableBD() {
		hmcpImplementation.disableBD();
	}
}

class HMCPImplementation extends HMCOPAlgorithm {
	public HMCPImplementation(Controller controller, double lambda) {
		super(controller, lambda);
	}

	public HMCPImplementation(Controller controller, double lambda, boolean guessMode, ProxyTypes proxyType, int k) {
		super(controller, lambda, guessMode, proxyType, k);
	}

	@Override
	public Path computePath(UnicastRequest request) {
		if(BDFeature) {
			preRunAlgorithm.setCostBorder(proxy.getNumberOfConstraints(request));
			preRunAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
			preRunAlgorithm.removeCostBorder();
			if(preRunAlgorithm.getPathToNodeFrom(request.getSource()) == null)
				return null;
		}
		else {
			preRunAlgorithm.computePathsFromAnyNodeTo(request.getDestination(),request);
			if(preRunAlgorithm.getPathToNodeFrom(request.getSource()) == null ||
					Proxy.violatesBound(preRunAlgorithm.getPathToNodeFrom(request.getSource()).getCost(), proxy.getNumberOfConstraints(request)))
				return null;
		}

		Path result = proxy.createPath(preRunAlgorithm.getPathToNodeFrom(request.getSource()), request,true);
		if(!Proxy.violatesBound(result.getConstraintsValues(), proxy.getConstraintsBounds(request)))
			return result;

		result = computePath(request, new LinkedList<Edge>());
		if(result == null || Proxy.violatesBound(result.getConstraintsValues(), proxy.getConstraintsBounds(request)))
			return null;
		return result;
	}
}
