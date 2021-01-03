package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.KSPAlgorithm;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 1978
 * "The Constrained Shortest Path Problem"
 * Y.P. Aneja and K.P.K. Nair.
 *
 * 1980
 * "A Dual Algorithm for the Constrained Shortest Path Problem"
 * Gabriel Y. Handler and Israel Zang.
 *
 * 1996
 * "An approximate algorithm for combinatorial optimization problems
 *  with two parameters"
 * David Blokh and Gregory Gutin.
 *
 * 2001
 * "Lagrange Relaxation Based Method for the QoS Routing Problem"
 * Alpár Jüttner, Balázs Szviatovszki, Ildikó Mécs and Zsolt Rajkó.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class LARACAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Multipliers for considering only the cost as optimization metric.
	 */
	private static final double[] leastCostMultipliers = {1, 0};

	/**
	 * Multipliers for considering only the delay as optimization metric.
	 */
	private static final double[] leastDelayMultipliers = {0, 1};

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	/**
	 * Underlying shortest path algorithm used.
	 */
	private SPAlgorithm spAlgorithm;

	/**
	 * Plumber proxy given to the underlying SPAlgorithm.
	 */
	private PathPlumberProxy plumberProxy;

	/**
	 * Maximal Difference (MD) parameter (See Section E.2, p. 864 of 2001
	 * paper). The algorithm will stop the Lagrangian optimization earlier if
	 * this value is higher. This parameter cannot be used in conjunction with
	 * the gap closing mechanism.
	 */
	private double maximalDifference;

	/**
	 * Algorithm used for closing the gap (Section III, p. 299 paper of 1980).
	 * If null, the gap is not closed.
	 */
	private KSPAlgorithm kSPAlgorithm;

	/**
	 * Gap closing will stop when UB is not more than delta % bigger than LB.
	 */
	private double delta;

	/**
	 * Maximum number of iterations.
	 */
	private int MAX_ITERATIONS = Integer.MAX_VALUE;

	public LARACAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public LARACAlgorithm(Controller controller, ProxyTypes maxMode) {
		this(controller, new AStarAlgorithm(controller));
		((AStarAlgorithm) this.spAlgorithm).setMaximumProxy(maxMode);
	}

	public LARACAlgorithm(Controller controller, SPAlgorithm spAlgorithm) {
		this(controller, spAlgorithm, 0);
	}

	public LARACAlgorithm(Controller controller, double maximalDifference) {
		this(controller, ProxyTypes.EDGE_PROXY, maximalDifference);
	}

	public LARACAlgorithm(Controller controller, ProxyTypes maxMode, double maximalDifference) {
		this(controller, new AStarAlgorithm(controller), maximalDifference);
		((AStarAlgorithm) this.spAlgorithm).setMaximumProxy(maxMode);
	}

	public LARACAlgorithm(Controller controller, SPAlgorithm spAlgorithm, double maximalDifference) {
		super(controller);
		if(spAlgorithm instanceof KSPAlgorithm) {
			this.spAlgorithm = spAlgorithm;
			this.kSPAlgorithm = (KSPAlgorithm) spAlgorithm;
			this.delta = maximalDifference;
		}
		else {
			this.spAlgorithm = spAlgorithm;
			this.maximalDifference = maximalDifference;
			this.kSPAlgorithm = null;
		}
		spAlgorithm.setDebugMode();
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
		if(kSPAlgorithm != null)
			kSPAlgorithm.setProxy(plumberProxy);
	}

	/**
	 * Sets the maximum number of iterations of LARAC to a given value.
	 * @param nIterations New max number of iterations.
	 */
	public void setMaxIterations(int nIterations) {
		this.MAX_ITERATIONS = nIterations;
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	public synchronized Path computePath(UnicastRequest request) {
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

		// 0.1: Least-cost run.
		this.plumberProxy.setCostMultipliers(leastCostMultipliers);
		Path lcPath = (Path) this.spAlgorithm.solve(request);

		// If no LC path can be found: no solution.
		if(lcPath == null)
			return null;

		// 0.1.1: If LC path satisfies the delay constraint: return it.
		if(!Proxy.violatesBound(lcPath.getParametersValues()[delayParameterID], deadline))
			return createPath(lcPath, request);

		// 0.1.2: bestUnfeasiblePath corresponds to X+ in 1980 paper.
		Path bestUnfeasiblePath = lcPath;
		double bestUnfeasiblePathCost = bestUnfeasiblePath.getParametersValues()[costParameterID];
		double bestUnfeasiblePathDelay = bestUnfeasiblePath.getParametersValues()[delayParameterID];

		// 0.2: Least-delay run.
		this.plumberProxy.setCostMultipliers(leastDelayMultipliers);
		Path ldPath;
		if(BDFeature)
			ldPath = (Path) ((BD) this.spAlgorithm).solve(request, deadline);
		else
			ldPath = (Path) this.spAlgorithm.solve(request);

		// 0.2.1: If no LD path can be found or if it does not satisfy the constraint: no solution.
		if(ldPath == null || Proxy.violatesBound(ldPath.getParametersValues()[delayParameterID], deadline))
			return null;

		// 0.2.2: bestFeasiblePath corresponds to X- in 1980 paper.
		Path bestFeasiblePath = ldPath;
		double bestFeasiblePathCost = bestFeasiblePath.getParametersValues()[costParameterID];
		double bestFeasiblePathDelay = bestFeasiblePath.getParametersValues()[delayParameterID];

		double UB = Double.MAX_VALUE;
		boolean stop = false;

		Set<Double> lambdas = new HashSet<>();

		for(int i = 0; i < MAX_ITERATIONS && !stop; i++) {
			/* Not in 1980 paper: additional optional stop condition from 2001
			 * paper to stop earlier and improve running time. If
			 * maximalDifference is 0, will not have any effect. */
			if(bestFeasiblePathCost <= ((1 + maximalDifference) * bestUnfeasiblePathCost))
				return createPath(bestFeasiblePath, request);

			// 1.1: New lambda (u in the 1980 paper), L and UB.
			double lambda = (bestFeasiblePathCost - bestUnfeasiblePathCost) / (bestUnfeasiblePathDelay - bestFeasiblePathDelay);

			double L = bestUnfeasiblePathCost + lambda * (bestUnfeasiblePathDelay - deadline);

			if(lambdas.contains(lambda)){
				return closeGap(bestFeasiblePath, L, UB, lambda, request, deadline);
			}
			lambdas.add(lambda);

			// 1.2: Run with new lambda.
			this.plumberProxy.setCostMultipliers(new double[]{1, lambda});

			Path result = (Path) this.spAlgorithm.solve(request);

			// 1.2.1.
			if (Proxy.fuzzyEquals(result.getParametersValues()[delayParameterID], deadline))
				return createPath(result, request);

			//Deadlock elimination: Gang Feng Performance of Delay-Constrained Least-Cost QoS Routing...
			if (Proxy.fuzzyEquals(result.getParametersValues()[costParameterID], bestFeasiblePathCost) ||
					Proxy.fuzzyEquals(result.getParametersValues()[costParameterID], bestUnfeasiblePathCost))
				stop = true;

			double Lu = result.getParametersValues()[costParameterID] + lambda * (result.getParametersValues()[delayParameterID] - deadline);
			if(Proxy.fuzzyEquals(Lu , L)) {
				double LB = Lu;
				// 1.2.2.
				if(!Proxy.violatesBound(result.getParametersValues()[delayParameterID], deadline)) {
					UB = Math.min(UB, result.getParametersValues()[costParameterID]);
					bestFeasiblePath = result;
				}
				// 1.2.3.
				else
					UB = bestFeasiblePathCost;
				return closeGap(bestFeasiblePath, LB, UB, lambda, request, deadline);
			}

			// 1.2.4.
			if(Proxy.violatesBound(result.getParametersValues()[delayParameterID] , deadline)) {
				bestUnfeasiblePath = result;
				bestUnfeasiblePathCost = bestUnfeasiblePath.getParametersValues()[costParameterID];
				bestUnfeasiblePathDelay = bestUnfeasiblePath.getParametersValues()[delayParameterID];
			}
			// 1.2.5.
			else {
				bestFeasiblePath = result;
				bestFeasiblePathCost = bestFeasiblePath.getParametersValues()[costParameterID];
				bestFeasiblePathDelay = bestFeasiblePath.getParametersValues()[delayParameterID];
				UB = Math.min(UB, bestFeasiblePathCost);
			}
		}

		/* In case we reached the maximum number of iterations, we return
		 * the current best Path. */
		return createPath(bestFeasiblePath, request);
	}

	private Path closeGap(Path feasibleSolution, double LB, double UB, double lambda, UnicastRequest request, double deadline) {
		if(kSPAlgorithm == null)
			return createPath(feasibleSolution, request);

		/* We get the Iterator used for computing the shortest path so far
		 * so that we already know the shortest path and can directly get
		 * the second one. */
		Iterator<Path> pathIterator = kSPAlgorithm.getCurrentIterator(request);
		Path bestPath = feasibleSolution;
		while (LB * (1 + delta) < UB) {
			// 2.1.2.
			Path nextPath = null;
			double nextPathCost = Double.MAX_VALUE;
			if(!pathIterator.hasNext()) {
				LB = Double.MAX_VALUE;
			}
			else {
				nextPath = pathIterator.next();
				nextPathCost = nextPath.getParametersValues()[this.plumberProxy.getPlumberParameterId(0, request)];
				double nextPathDelay = nextPath.getParametersValues()[this.plumberProxy.getPlumberParameterId(1, request)];
				LB = nextPathCost + lambda * (nextPathDelay - deadline);
			}

			// 2.1.2.1.
			if(nextPathCost < UB && nextPath != null && !Proxy.violatesBound(nextPath.getParametersValues()[this.plumberProxy.getPlumberParameterId(1, request)]  , deadline)) {
				UB = nextPathCost;
				bestPath = nextPath;
			}

			// 2.1.2.2.
			continue;
		}

		// 2.1.1.
		return createPath(bestPath, request);
	}

	/**
	 * Creates Path with suitable cost, delay and parameters to return as a
	 * CSPAlgorithm.
	 * @param path The underlying Path found by SP Algorithm.
	 * @param request Request for which Path has been found.
	 * @return The Path.
	 */
	private Path createPath(Path path, UnicastRequest request) {
		return new Path(path.getPath(),
				// Cost is the first additional parameter.
				path.getParametersValues()[plumberProxy.getPlumberParameterId(0, request)],
				// The constraint is the second additional parameter (delay).
				new double[] {path.getParametersValues()[plumberProxy.getPlumberParameterId(1, request)]},
				// Parameters.
				plumberProxy.removePlumberParameters(path.getParametersValues(), request));
	}

	@Override
	public boolean isForward() {
		return spAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY &&
				kSPAlgorithm != null &&
				spAlgorithm.isOptimal() &&
				spAlgorithm.isComplete() &&
				kSPAlgorithm.isComplete() &&
				kSPAlgorithm.isOptimal() &&
				maximalDifference == 0.0 &&
				delta == 0.0;
	}

	@Override
	public boolean isComplete() {
		return spAlgorithm.isComplete() && spAlgorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return spAlgorithm.isValid();
	}

	public void enableBD() {
		this.BDFeature = true;
	}

	public void disableBD() {
		this.BDFeature = false;
	}
}
