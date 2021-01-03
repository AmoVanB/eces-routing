package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.scrc;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.KSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen.YenAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.Iterator;

/**
 * 2007
 * "An improved solution algorithm for the constrained shortest path problem"
 * Luis Santos, Joao Coutinho-Rodrigues, John R. Current.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SCRCAlgorithm extends CSPAlgorithm implements SolveUnicastRequest {
	/**
	 * Plumber proxy for changing costs used for routing.
	 */
	private PathPlumberProxy plumberProxy;

	/**
	 * Underlying KSPAlgorithm.
	 */
	private KSPAlgorithm algorithm;

	public SCRCAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public SCRCAlgorithm(Controller controller, ProxyTypes mode) {
		this(controller, new YenAlgorithm(controller, mode));
	}

	public SCRCAlgorithm(Controller controller, KSPAlgorithm kspAlgorithm) {
		super(controller);
		algorithm = kspAlgorithm;
		this.plumberProxy = new PathPlumberProxy(
				new int[]{0, 1},    // Cost and delay as cost.
				new double[]{1, 0}, // Only cost as cost.
				new int[0],         // No constraints.
				new int[]{0, 1});   // Cost and delay as parameters.
		algorithm.setProxy(plumberProxy);
	}

	@Override
	public void setProxy(PathProxy proxy) {
		plumberProxy.setProxy(proxy);
		super.setProxy(proxy);
	}

	@Override
	public void setProxy(PreviousEdgeProxy proxy) {
		plumberProxy.setProxy(proxy);
		super.setProxy(proxy);
	}

	@Override
	public void setProxy(EdgeProxy proxy) {
		plumberProxy.setProxy(proxy);
		super.setProxy(proxy);
	}

	/**
	 * Creates Path with suitable cost and delay to return as a CSP Algorithm.
	 * @param path The underlying Path found by SP Algorithm.
	 * @param request Request for which Path has been found.
	 * @return The Path.
	 */
	private Path createPath(Path path, UnicastRequest request) {
		return new Path(path.getPath(),
				// Cost is the first additional parameter.
				path.getParametersValues()[plumberProxy.getPlumberParameterId(0, request)],
				// Constraints is the second additional parameter (delay).
				new double[] {path.getParametersValues()[plumberProxy.getPlumberParameterId(1, request)]},
				// Parameters.
				plumberProxy.removePlumberParameters(path.getParametersValues(), request));
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
		final int costParameterID = plumberProxy.getPlumberParameterId(0, request);
		final int constraintParameterID = plumberProxy.getPlumberParameterId(1, request);

		// Least-cost path.
		this.plumberProxy.setCostMultipliers(new double[]{1.0, 0.0});
		Path pathA = (Path) algorithm.solve(request);

		// Return LC path if there are no constraints.
		double deadline;
		if (proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];
		else
			return pathA;

		// Case 1 (least-cost is fine - done).
		if(!Proxy.violatesBound(pathA.getParametersValues()[constraintParameterID], deadline))
			return createPath(pathA, request);

		// Least-delay path.
		this.plumberProxy.setCostMultipliers(new double[]{0.0, 1.0});
		Path pathB = (Path) algorithm.solve(request);

		// Case 2 (least-delay is not fine - no solution).
		if(Proxy.violatesBound(pathB.getParametersValues()[constraintParameterID], deadline))
			return null;

		// Case 3 (least-delay is on the delay border - check for other paths
		// with least-cost on this border).
		if(Proxy.violatesBound(pathB.getParametersValues()[constraintParameterID], deadline)) {
			Path pathBS = pathB;
			this.plumberProxy.setCostMultipliers(new double[]{0.0, 1.0});
			Iterator<Path> ksp = algorithm.iterator(request);
			if(!ksp.hasNext())
				return createPath(pathBS, request);

			Path pathX = ksp.next();
			while(!Proxy.violatesBound(pathX.getParametersValues()[constraintParameterID], deadline)) {
				if(pathBS.getParametersValues()[costParameterID] > pathX.getParametersValues()[costParameterID])
					pathBS = pathX;
				if(!ksp.hasNext())
					return pathBS;
				pathX = ksp.next();
			}

			return createPath(pathBS, request);
		}

		// Case 4.
		double p =  (deadline - pathB.getParametersValues()[constraintParameterID]) /
					(pathA.getParametersValues()[constraintParameterID] - pathB.getParametersValues()[constraintParameterID]);
		double w =  ((1 - Math.sqrt(p)) * (pathB.getParametersValues()[costParameterID] - pathA.getParametersValues()[costParameterID])) /
					(Math.sqrt(p) * (pathA.getParametersValues()[constraintParameterID] - pathB.getParametersValues()[constraintParameterID]));

		Path pathBS = pathB;
		this.plumberProxy.setCostMultipliers(new double[]{1.0, w});
		Iterator<Path> ksp = algorithm.iterator(request);
		if(!ksp.hasNext())
			return createPath(pathBS, request);

		Path pathX = ksp.next();
		while(w * (pathX.getParametersValues()[constraintParameterID] - deadline) + pathX.getParametersValues()[costParameterID] < pathBS.getParametersValues()[costParameterID]) {
			if((!Proxy.violatesBound(pathX.getParametersValues()[constraintParameterID], deadline)) && pathX.getParametersValues()[costParameterID] < pathBS.getParametersValues()[costParameterID])
				pathBS = pathX;

			if(!ksp.hasNext())
				return createPath(pathBS, request);
			pathX = ksp.next();
		}

		return createPath(pathBS, request);
	}

	@Override
	public boolean isForward() {
		return this.algorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return this.algorithm.isOptimal();
	}

	@Override
	public boolean isComplete() {
		return this.algorithm.isComplete() && this.algorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
