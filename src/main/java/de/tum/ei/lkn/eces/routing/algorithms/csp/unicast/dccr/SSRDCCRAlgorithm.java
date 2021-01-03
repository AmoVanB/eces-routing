package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dccr;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac.LARACAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * 2001
 * "Search Space Reduction in QoS Routing"
 * Liang Guo and Ibrahim Matta.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SSRDCCRAlgorithm extends DCCRAlgorithm {
	private final static int DEFAULT_LARAC_MAX_ITERATIONS = 4;

	private LARACAlgorithm laracAlgorithm;

	public SSRDCCRAlgorithm(Controller controller) {
		this(controller, DEFAULT_K);
	}

	public SSRDCCRAlgorithm(Controller controller, int k) {
		this(controller, new LARACAlgorithm(controller), k, DEFAULT_LARAC_MAX_ITERATIONS);
	}

	public SSRDCCRAlgorithm(Controller controller, int k, int maxLarac) {
		this(controller, new LARACAlgorithm(controller), k, maxLarac);
	}

	public SSRDCCRAlgorithm(Controller controller, LARACAlgorithm laracAlgorithm) {
		this(controller, laracAlgorithm, DEFAULT_K);
	}

	public SSRDCCRAlgorithm(Controller controller, LARACAlgorithm laracAlgorithm, int k) {
		this(controller, laracAlgorithm, k, DEFAULT_LARAC_MAX_ITERATIONS);
	}

	public SSRDCCRAlgorithm(Controller controller, LARACAlgorithm laracAlgorithm, int k, int laracMaxIterations) {
		this(controller, laracAlgorithm, new AStarAlgorithm(controller), k, laracMaxIterations);
	}

	public SSRDCCRAlgorithm(Controller controller, LARACAlgorithm laracAlgorithm, SPAlgorithm spAlgorithm, int k, int laracMaxIterations) {
		super(controller, spAlgorithm, k);
		this.laracAlgorithm = laracAlgorithm;
		this.laracAlgorithm.setMaxIterations(laracMaxIterations);
	}

	@Override
	protected void setProxy() {
		this.laracAlgorithm.setProxy(proxy);
	}

	@Override
	protected Path computePath(UnicastRequest request) {
		double deadline = Double.MAX_VALUE;
		if(proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];

		Path LARACPath = (Path) laracAlgorithm.solve(request);
		if(LARACPath == null)
			return null;

		double costBound = LARACPath.getCost();
		Path bestPath = proxy.createPath(LARACPath, request, isForward());
		return DCCRRouting(request, deadline, costBound, bestPath);
	}

	@Override
	public void enableBD() {
		super.enableBD();
		laracAlgorithm.enableBD();
	}

	@Override
	public void disableBD() {
		super.disableBD();
		laracAlgorithm.disableBD();
	}

	@Override
	public boolean isComplete() {
		return laracAlgorithm.isComplete();
	}

	@Override
	public boolean isValid() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}
}
