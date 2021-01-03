package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcr;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.LinkedList;
import java.util.List;

/**
 * 1997
 * "A new distributed routing algorithm for supporting delay-sensitive
 *  applications."
 * Quan Sun and Horst Langendörfer.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCRAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Algorithm used to compute the LD path from source to the destination.
	 */
	private SPAlgorithm leastDelayAlgorithm;

	/**
	 * PlumberProxy used by the LD algorithm.
	 */
	private PathPlumberProxy leastDelayProxy;

	/**
	 * Algorithm used to compute the LC path from any currentNode to the destination.
	 */
	private NToOneAlgorithm leastCostAlgorithm;

	/**
	 * PlumberProxy used by the LC algorithm.
	 */
	private PathPlumberProxy leastCostProxy;

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	public DCRAlgorithm(Controller controller, NToOneAlgorithm leastCostAlgorithm, SPAlgorithm leastDelayAlgorithm) {
		super(controller);
		this.leastCostAlgorithm = leastCostAlgorithm;
		this.leastDelayAlgorithm = leastDelayAlgorithm;
	}

	public DCRAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public DCRAlgorithm(Controller controller, ProxyTypes mode) {
		super(controller);
		leastDelayAlgorithm = new AStarAlgorithm(controller);
		((AStarAlgorithm) leastDelayAlgorithm).setMaximumProxy(mode);
		leastCostAlgorithm  = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastCostAlgorithm).setMaximumProxy(mode);
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
		if(this.BDFeature) {
			leastDelayProxy = new PathPlumberProxy(proxy,
					new int[]{1},    // Delay as cost.
					new double[]{1},
					new int[0],
					new int[]{0} // Cost is needed if BD.
			);
		}
		else {
			leastDelayProxy = new PathPlumberProxy(proxy,
					new int[]{1},    // Delay as cost.
					new double[]{1},
					new int[0],
					new int[0]);
		}

		leastCostProxy = new PathPlumberProxy(proxy,
				new int[]{0},    // Cost as cost.
				new double[]{1}, // Cost * 1.
				new int[0],      // No constraints.
				new int[]{1});   // Delay as additional parameter.

		((RoutingAlgorithm) this.leastCostAlgorithm).setProxy(leastCostProxy);
		this.leastDelayAlgorithm.setProxy(leastDelayProxy);
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
		// Return LC path if there are no constraints.
		double deadline;
		if (proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];
		else {
			leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
			return leastCostAlgorithm.getPathToNodeFrom(request.getSource());
		}

		// Compute the LD path.
		Path ldPath;
		if(BDFeature)
			ldPath = (Path) ((BD) leastDelayAlgorithm).solve(request, deadline);
		else
			ldPath = (Path) leastDelayAlgorithm.solve(request);

		// Impossible if the LD path has higher delay than the deadline.
		if((BDFeature && ldPath == null) || Proxy.violatesBound(ldPath.getCost(), deadline))
			return null;

		// Compute the LC paths.
		if(BDFeature)
			((DijkstraAlgorithm)leastCostAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, ldPath.getParametersValues()[leastDelayProxy.getPlumberParameterId(0, request)]);
		else
			leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);

		List<Edge> pathSoFar = new LinkedList<>();
		double costSoFar = 0;
		double delaySoFar = 0;
		double[] parametersSoFar = null;

		/* Description from
		 * 1998
		 * "An Overview of Quality of Service Routing for Next-Generation
		 *	High-Speed Networks: Problems and Solutions"
		 *  Shigang Chen and Klara Nahrstedt.
		 * "Travel along the least-delay path until reaching a node from which
		 *  the delay of the least-cost path satisfies the delay constraint.
		 *  From that node on, the message travels along the least-path all the
		 *  way to the destination." */
		boolean followingLD = true;
		int ldEdgeNumber = 0;
		Node currentNode = request.getSource();
		while(currentNode != request.getDestination()) {
			if(followingLD) {
				/* Even with BD, LC path should always exist for nodes on the LD
				 * path. Indeed, LC search is blocked at the cost of the LD path.
				 * Hence, the LC search will never be blocked before reaching the
				 * source from the destination. Indeed, there is for sure one path
				 * with a lower or equal cost: the LD path.
				 */
				double LCPathDelay = leastCostAlgorithm.getPathToNodeFrom(currentNode).getParametersValues()[leastCostProxy.getPlumberParameterId(0, request)];
				if (!Proxy.violatesBound(LCPathDelay + delaySoFar, deadline))
					followingLD = false;
			}

			Edge nextEdge;
			if(followingLD)
				nextEdge = ldPath.getPath()[ldEdgeNumber++];
			else
				nextEdge = leastCostAlgorithm.getPathToNodeFrom(currentNode).getPath()[0];

			parametersSoFar = proxy.getNewParameters(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
			costSoFar += proxy.getCost(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
			delaySoFar += proxy.getConstraintsValues(pathSoFar, nextEdge, parametersSoFar, request, this.isForward())[0];
			pathSoFar.add(0, nextEdge);
			currentNode = nextEdge.getDestination();
		}

		return new Path(pathSoFar, costSoFar, new double[]{delaySoFar}, parametersSoFar);
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
		return leastDelayAlgorithm.isComplete() &&
				((RoutingAlgorithm) leastCostAlgorithm).isComplete() &&
				leastDelayAlgorithm.isOptimal() &&
				((RoutingAlgorithm) leastCostAlgorithm).isOptimal();
	}

	@Override
	public boolean isValid() {
		return leastDelayAlgorithm.isValid() &&
				((RoutingAlgorithm) leastCostAlgorithm).isValid();
	}

	@Override
	public void enableBD() {
		this.BDFeature = true;
		this.setProxy();
	}

	@Override
	public void disableBD() {
		this.BDFeature = false;
		this.setProxy();
	}
}
