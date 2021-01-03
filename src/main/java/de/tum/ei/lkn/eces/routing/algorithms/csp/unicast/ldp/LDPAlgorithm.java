package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.ldp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
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
 * Least-delay path (LDP) Algorithm.
 * Always return the LD path.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class LDPAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Underlying shortest path algorithm used.
	 */
	private SPAlgorithm spAlgorithm;

	/**
	 * Plumber proxy given to the underlying SPAlgorithm.
	 */
	private PathPlumberProxy plumberProxy;

	/**
	 * Whether BD is activate or not.
	 */
	private boolean BDfeature = false;

	public LDPAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public LDPAlgorithm(Controller controller, ProxyTypes maxProxy) {
		super(controller);
		spAlgorithm = new AStarAlgorithm(controller);
		((AStarAlgorithm) spAlgorithm).setMaximumProxy(maxProxy);
	}

	public LDPAlgorithm(Controller controller, SPAlgorithm spAlgorithm) {
		super(controller);
		this.spAlgorithm = spAlgorithm;
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
		 * - only delay as cost component (2nd and 3rd args),
		 * - no constraints (4th arg),
		 * - cost as additional parameters (5th arg). */
		plumberProxy = new PathPlumberProxy(this.proxy, new int[]{1}, new double[]{1}, new int[0], new int[]{0});
		spAlgorithm.setProxy(plumberProxy);
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
		double deadline = Double.MAX_VALUE;
		if(proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];

		Path path;
		if(BDfeature)
			path = (Path) ((BD) this.spAlgorithm).solve(request, deadline);
		else
			path = (Path) this.spAlgorithm.solve(request);

		if(path != null && !Proxy.violatesBound(path.getCost(), deadline))
			return proxy.createPath(path, request, this.isForward());
		else
			return null;
	}

	@Override
	public boolean isForward() {
		return spAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return spAlgorithm.isComplete() && spAlgorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void enableBD() {
		this.BDfeature = true;
	}

	@Override
	public void disableBD() {
		this.BDfeature = false;
	}
}
