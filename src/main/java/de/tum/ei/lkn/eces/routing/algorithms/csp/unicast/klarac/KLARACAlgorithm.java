package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.klarac;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 2001
 * "Heuristic Methods for Delay-Constrained Least-Cost Routing Problem Using
 * k-Shortest-Path Algorithms" (Section III)
 * Zhanfeng Jia and Pravin Varaiya.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class KLARACAlgorithm extends CSPAlgorithm implements SolveUnicastRequest {
	/**
	 * Multipliers for considering only the cost as optimization metric.
	 */
	private static final double[] leastCostMultipliers = {1, 0};

	/**
	 * Multipliers for considering only the delay as optimization metric.
	 */
	private static final double[] leastDelayMultipliers = {0, 1};

	/**
	 * Underlying k shortest path algorithm used.
	 */
	private UniversalPriorityQueueAlgorithm kspAlgorithm;

	/**
	 * Plumber proxy given to the underlying SP Algorithm.
	 */
	private PathPlumberProxy plumberProxy;

	/**
	 * Value of K.
	 */
	private int K;

	/**
	 * Maximum number of iterations.
	 */
	private int MAX_ITERATIONS = Integer.MAX_VALUE;

	public KLARACAlgorithm(Controller controller, int k) {
		this(controller, ProxyTypes.EDGE_PROXY, k);
	}

	public KLARACAlgorithm(Controller controller, ProxyTypes maxMode, int k) {
		super(controller);
		if(k < 2)
			throw new RoutingException("If k < 2, use LARAC rather than kLARAC");
		this.K = k;
		kspAlgorithm = new UniversalPriorityQueueAlgorithm(controller, QueueMode.NODE, true, true, k);
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
		kspAlgorithm.setProxy(plumberProxy);
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
		int costID = plumberProxy.getPlumberParameterId(0, request);
		int delayID = plumberProxy.getPlumberParameterId(1, request);
		double deadline = Double.MAX_VALUE;
		if(proxy.getNumberOfConstraints(request) > 0)
			deadline = this.proxy.getConstraintsBounds(request)[0];

		// Least-cost runs.
		this.plumberProxy.setCostMultipliers(leastCostMultipliers);
		Path path = kspAlgorithm.computePath(request);
		// If no LC path can be found: no solution.
		if(path == null)
			return null;

		// If one of the LC paths satisfies the delay constraint: return it.
		Path bestUnfeasiblePath = null;
		double bestUnfeasiblePathCost = Double.MAX_VALUE;
		double bestUnfeasiblePathDelay = Double.MAX_VALUE;
		for(Path newPath: kspAlgorithm.getkPathFromNodeTo(request.getDestination())) {
			if(newPath != null){
				if(!Proxy.violatesBound(newPath.getParametersValues()[delayID], deadline))
					return createPath(newPath, request);

				if(newPath.getParametersValues()[costID] < bestUnfeasiblePathCost) {
					bestUnfeasiblePath = newPath;
					bestUnfeasiblePathCost = bestUnfeasiblePath.getParametersValues()[costID];
					bestUnfeasiblePathDelay = bestUnfeasiblePath.getParametersValues()[delayID];
				}
			}
		}

		// Least-delay runs.
		this.plumberProxy.setCostMultipliers(leastDelayMultipliers);
		path = kspAlgorithm.computePath(request);
		// If no LD path can be found: no solution.
		if(path == null)
			return null;
		if(Proxy.violatesBound(path.getParametersValues()[delayID], deadline))
			return null;

		Set<Path> ldPaths = new LinkedHashSet<>();
		for(Path newPath: kspAlgorithm.getkPathFromNodeTo(request.getDestination())) {
			ldPaths.add(newPath);
		}

		if(getPathSetType(request, ldPaths, deadline) == PathSetType.MIXED_TYPE)
			return createPath(getFeasiblePathWithLeastCost(request, ldPaths, deadline), request);

		Path bestFeasiblePath = getFeasiblePathWithLeastCost(request, ldPaths, deadline);
		double bestFeasiblePathCost = bestFeasiblePath.getParametersValues()[costID];
		double bestFeasiblePathDelay = bestFeasiblePath.getParametersValues()[delayID];

		double lambda = Double.MAX_VALUE;
		for(int i = 0; i < MAX_ITERATIONS; i++) {
			double oldLambda = lambda;
			lambda = (bestUnfeasiblePathCost - bestFeasiblePathCost) / (bestFeasiblePathDelay - bestUnfeasiblePathDelay);
			// Avoid infinite loop if stuck at a lambda.
			if(Proxy.fuzzyEquals(lambda,oldLambda))
				return createPath(bestFeasiblePath, request);

			// Run with new lambda.
			Set<Path> paths = getKPaths(request, new double[]{1, lambda});
			PathSetType type = getPathSetType(request, paths, deadline);
			if(type == PathSetType.MIXED_TYPE)
				return createPath(getFeasiblePathWithLeastCost(request, paths, deadline), request);
			else if(type == PathSetType.PC_TYPE) {
				bestUnfeasiblePath = getPathWithLeastCost(request, paths);
				bestUnfeasiblePathCost = bestUnfeasiblePath.getParametersValues()[costID];
				bestUnfeasiblePathDelay = bestUnfeasiblePath.getParametersValues()[delayID];
			} else { // PD_TYPE
				bestFeasiblePath = getPathWithLeastCost(request, paths);
				bestFeasiblePathCost = bestFeasiblePath.getParametersValues()[costID];
				bestFeasiblePathDelay = bestFeasiblePath.getParametersValues()[delayID];
			}
		}

		/* In case we reached the maximum number of iterations, we return
		 * the current best Path. */
		return createPath(bestFeasiblePath, request);
	}

	private Set<Path> getKPaths(UnicastRequest request, double[] multipliers) {
		Set<Path> result = new LinkedHashSet<>();
		plumberProxy.setCostMultipliers(multipliers);
		kspAlgorithm.computePath(request);
		for(Path newPath: kspAlgorithm.getkPathFromNodeTo(request.getDestination())) {
			if(newPath != null)
				result.add(newPath);
		}
		return result;
	}

	private PathSetType getPathSetType(UnicastRequest request, Set<Path> paths, double deadline) {
		if(paths.size() == 0)
			throw new InternalError("Set of Paths cannot be empty");

		boolean containsFeasible = false;
		boolean containsUnfeasible = false;
		int delayID = plumberProxy.getPlumberParameterId(1, request);
		for(Path path : paths) {
			if(path != null && path.getParametersValues() != null ) {
				if (Proxy.violatesBound(path.getParametersValues()[delayID], deadline))
					containsUnfeasible = true;
				else
					containsFeasible = true;

				if (containsFeasible && containsUnfeasible)
					return PathSetType.MIXED_TYPE;
			} else
				System.out.println("Error");
		}

		if(containsFeasible)
			return PathSetType.PD_TYPE;
		else // meaning: if(containsUnfeasible)
			return PathSetType.PC_TYPE;
	}

	private Path getPathWithLeastCost(UnicastRequest request, Set<Path> paths) {
		if(paths.size() == 0)
			throw new InternalError("Set of Paths cannot be empty");

		int costID = plumberProxy.getPlumberParameterId(0, request);
		Path bestPath = null;
		for(Path path : paths)
			if(path != null && path.getParametersValues() != null ) {
				if(bestPath == null || path.getParametersValues()[costID] < bestPath.getParametersValues()[costID])
					bestPath = path;
			} else
				System.out.println("Error");
		return bestPath;
	}

	private Path getFeasiblePathWithLeastCost(UnicastRequest request, Set<Path> paths, double deadline) {
		if(paths.size() == 0)
			throw new InternalError("Set of Paths cannot be empty");

		int costID = plumberProxy.getPlumberParameterId(0, request);
		int delayID = plumberProxy.getPlumberParameterId(1, request);
		Path bestPath = null;
		for(Path path : paths)
			if(path != null && path.getParametersValues() != null && path.getConstraintsValues() != null ) {
				if ((bestPath == null || path.getParametersValues()[costID] < bestPath.getParametersValues()[costID]) && !Proxy.violatesBound(path.getParametersValues()[delayID], deadline))
					bestPath = path;
			} else
				System.out.println("Error");
		if(bestPath == null)
			return null;
		return bestPath;
	}

	/**
	 * Creates Path with suitable cost and delay to return as a CSP Algorithm.
	 * @param path The underlying Path found by SP Algorithm.
	 * @param request Request for which Path has been found.
	 * @return The Path.
	 */
	private Path createPath(Path path, UnicastRequest request) {
		int costID = plumberProxy.getPlumberParameterId(0, request);
		int delayID = plumberProxy.getPlumberParameterId(1, request);
		if(Proxy.violatesBound(path.getParametersValues()[delayID], proxy.getConstraintsBounds(request)[0]))
			System.out.println("Error");
		return new Path(path.getPath(),
				// Cost is the first additional parameter.
				path.getParametersValues()[costID],
				// The constraint is the second additional parameter (delay).
				new double[] {path.getParametersValues()[delayID]},
				// Parameters.
				plumberProxy.removePlumberParameters(path.getParametersValues(), request));
	}

	@Override
	public boolean isForward() {
		return this.kspAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		return kspAlgorithm.isComplete() && kspAlgorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}

/**
 * Different possible types of a Path Set (upper right part p. 3 of paper).
 */
enum PathSetType {
	PC_TYPE,    // Contains no feasible paths.
	PD_TYPE,    // Contains only feasible paths.
	MIXED_TYPE  // Contains both feasible and unfeasible paths.
}
