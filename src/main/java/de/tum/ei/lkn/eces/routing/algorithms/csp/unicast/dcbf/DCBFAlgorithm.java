package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcbf;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford.BellmanFordAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

/**
 * 2001
 * "Heuristic Methods for Delay-Constrained Least-Cost Routing Problem Using
 * k-Shortest-Path Algorithms" (Section IV)
 * Zhanfeng Jia and Pravin Varaiya.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCBFAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Underlying Bellman-Ford Algorithm.
	 */
	private BellmanFordAlgorithm bfAlgorithm;

	/**
	 * Proxy given to the BFAlgorithm in order to do the delay test before
	 * updating a Node's routing table.
	 *
	 * This proxy wraps 'proxy', i.e. the Proxy given to DCBF.
	 */
	private DelayTestProxy delayTestProxy;

	public DCBFAlgorithm(Controller controller) {
		super(controller);
		bfAlgorithm = new BellmanFordAlgorithm(controller, 1);
		delayTestProxy = new DelayTestProxy(controller, bfAlgorithm);
		bfAlgorithm.setProxy(delayTestProxy);
	}

	public DCBFAlgorithm(Controller controller, int k) {
		this(controller,k,k);
	}

	public DCBFAlgorithm(Controller controller, int kc, int kd) {
		super(controller);
		bfAlgorithm = new BellmanFordWithGuess(controller, kc);
		delayTestProxy = new DelayTestProxy(controller, bfAlgorithm, kd);
		bfAlgorithm.setProxy(delayTestProxy);
	}

	@Override
	public void setProxy(EdgeProxy proxy) {
		super.setProxy(proxy);
		delayTestProxy.setProxy(proxy);
	}

	@Override
	public void setProxy(PathProxy proxy) {
		super.setProxy(proxy);
		delayTestProxy.setProxy(proxy);
	}

	@Override
	public void setProxy(PreviousEdgeProxy proxy) {
		super.setProxy(proxy);
		delayTestProxy.setProxy(proxy);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	synchronized public Path computePath(UnicastRequest request) {
		this.delayTestProxy.reinit();
		return proxy.createPath(bfAlgorithm.computePath(request), request, this.isForward());
	}

	@Override
	public boolean isForward() {
		return bfAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isValid() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public void enableBD() {
		this.delayTestProxy.enableBD();
	}

	@Override
	public void disableBD() {
		this.delayTestProxy.disableBD();
	}
}
