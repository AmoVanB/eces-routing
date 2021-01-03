package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.deb;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.meb.MEBAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
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
 * Dual Extended Bellman-Ford's algorithm.
 *
 * 2003
 * "A new heuristics for finding the delay constrained least cost path"
 * G. Cheng and N. Ansari.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DEBAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	private MEBAlgorithm mebAlgorithm;

	public DEBAlgorithm(Controller controller) {
		super(controller);
		mebAlgorithm = new MEBAlgorithm(controller);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		mebAlgorithm.setForward();
		mebAlgorithm.leastDelayMode();
		Path response = (Path) mebAlgorithm.solve(request);
		if(response == null)
			return null;
		mebAlgorithm.setBackward();
		mebAlgorithm.leastCostMode();
		Path response2 = (Path) mebAlgorithm.solve(request);
		if(response2 == null)
			return proxy.createPath(response.getPath(), request,isForward());
		return proxy.createPath(response2.getPath(), request,isForward());
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return null;
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		if(proxy != null)
			switch(proxy.getType()) {
				case EDGE_PROXY:
					return true;
				case PREVIOUS_EDGE_PROXY:
					return false;
				case PATH_PROXY:
					return false;
			}
		throw new RoutingException("The proxy is missing at the routing algorithm");
	}

	@Override
	public boolean isValid() {
		if(proxy != null)
			switch(proxy.getType()) {
				case EDGE_PROXY:
					return true;
				case PREVIOUS_EDGE_PROXY:
					return false;
				case PATH_PROXY:
					return false;
			}
		throw new RoutingException("The proxy is missing at the routing algorithm");
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
		mebAlgorithm.setProxy(proxy);
	}

	@Override
	public void enableBD() {
		mebAlgorithm.enableBD();
	}

	@Override
	public void disableBD() {
		mebAlgorithm.disableBD();
	}
}
