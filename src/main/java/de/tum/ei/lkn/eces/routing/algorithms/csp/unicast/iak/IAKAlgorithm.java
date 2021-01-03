package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.iak;

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
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 1998
 * "A Delay-Constrained Least-Cost Path Routing Protocol and the
 *  Synthesis Method"
 * Kenji Ishida, Kitsutaro Amano and Naoki Kannari.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class IAKAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Algorithm used to compute the LD path from any currentNode to the destination.
	 */
	private NToOneAlgorithm leastDelayAlgorithm;

	/**
	 * Algorithm used to compute the LC path from source to the destination.
	 */
	private SPAlgorithm leastCostAlgorithm;


	/**
	 * PlumberProxy used by the LD algorithm.
	 */
	private PathPlumberProxy leastDelayProxy;

	/**
	 * PlumberProxy used by the LC algorithm.
	 */
	private PathPlumberProxy leastCostProxy;

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	public IAKAlgorithm(Controller controller, SPAlgorithm leastCostAlgorithm, NToOneAlgorithm leastDelayAlgorithm) {
		super(controller);
		this.leastCostAlgorithm = leastCostAlgorithm;
		this.leastDelayAlgorithm  = leastDelayAlgorithm;
	}

	public IAKAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public IAKAlgorithm(Controller controller, ProxyTypes mode) {
		super(controller);
		leastDelayAlgorithm = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastDelayAlgorithm).setMaximumProxy(mode);
		leastCostAlgorithm  = new AStarAlgorithm(controller);
		((AStarAlgorithm) leastCostAlgorithm).setMaximumProxy(mode);
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
				new int[0]);     // No additional parameters.

		this.leastCostAlgorithm.setProxy(leastCostProxy);
		((RoutingAlgorithm) this.leastDelayAlgorithm).setProxy(leastDelayProxy);
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
		else
			return (Path) leastCostAlgorithm.solve(request);

		// Compute the LD paths.
		if(BDFeature)
			((DijkstraAlgorithm) leastDelayAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, deadline);
		else
			leastDelayAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);

		// Impossible if the LD path has higher delay than the deadline.
		if((BDFeature && leastDelayAlgorithm.getPathToNodeFrom(request.getSource()) == null) || Proxy.violatesBound(leastDelayAlgorithm.getPathToNodeFrom(request.getSource()).getCost() , deadline))
			return null;

		// Compute the LC path.
		Path lcPath;
		if(BDFeature)
			lcPath = (Path) ((BD) leastCostAlgorithm).solve(request, leastDelayAlgorithm.getPathToNodeFrom(request.getSource()).getParametersValues()[leastDelayProxy.getPlumberParameterId(0, request)]);
		else
			lcPath = (Path) leastCostAlgorithm.solve(request);

		if(lcPath == null)
			return null;

		LinkedList<Edge> pathSoFar = new LinkedList<>();
		double delaySoFar = 0;
		double costSoFar = 0;
		double[] parametersSoFar = null;
		Node currentNode = request.getSource();
		boolean delayFlag = false;
		int lcEdgeNumber = 0;
		while(currentNode != request.getDestination()) {
			// If started to follow LD, continue.
			if(delayFlag) {
				// Even with BD, nextEdge will always exist if we started to follow LD.
				Edge nextEdge = leastDelayAlgorithm.getPathToNodeFrom(currentNode).getPath()[0];
				parametersSoFar = proxy.getNewParameters(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
				delaySoFar += proxy.getConstraintsValues(pathSoFar, nextEdge, parametersSoFar, request, this.isForward())[0];
				costSoFar += proxy.getCost(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
				pathSoFar.add(0, nextEdge);
				currentNode = nextEdge.getDestination();
			}
			else {
				Edge nextEdge = lcPath.getPath()[lcEdgeNumber++];
				double[] newParameters = proxy.getNewParameters(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
				double edgeDelay = proxy.getConstraintsValues(pathSoFar, nextEdge, newParameters, request, this.isForward())[0];
				Path upcomingLD = leastDelayAlgorithm.getPathToNodeFrom(nextEdge.getDestination());
				double upcomingDelay;
				// Could be that, because of BD, LD search did not reach this node.
				if(BDFeature && upcomingLD == null)
					upcomingDelay = Double.POSITIVE_INFINITY;
				else
					upcomingDelay = upcomingLD.getCost();
				// Follow LC if leads to feasible path.
				if(!Proxy.violatesBound(delaySoFar + edgeDelay + upcomingDelay, deadline)) {
					parametersSoFar = newParameters;
					delaySoFar += edgeDelay;
					costSoFar += proxy.getCost(pathSoFar, nextEdge, newParameters, request, this.isForward());
					pathSoFar.add(0, nextEdge);
					currentNode = nextEdge.getDestination();
				}
				else {
					// Start to follow LD if LC leads to infeasible path.
					delayFlag = true;
					nextEdge = leastDelayAlgorithm.getPathToNodeFrom(currentNode).getPath()[0];
					parametersSoFar = proxy.getNewParameters(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
					delaySoFar += proxy.getConstraintsValues(pathSoFar, nextEdge, parametersSoFar, request, this.isForward())[0];
					costSoFar += proxy.getCost(pathSoFar, nextEdge, parametersSoFar, request, this.isForward());
					pathSoFar.add(0, nextEdge);
					currentNode = nextEdge.getDestination();
				}
			}
		}

		List<Edge> finalPath = new LinkedList<>();
		// Get the Path in the forward direction.

		Edge[] pathSoFarArray = new Edge[pathSoFar.size()];
		int i = 0;
		for (Edge edge : pathSoFar) {
			pathSoFarArray[i] = edge;
			i++;
		}
		ArrayUtils.reverse(pathSoFarArray);

		// Check for loops and remove them.
		Set<Node> visitedNodes = new HashSet<>();
		visitedNodes.add(pathSoFarArray[0].getSource());
		for(Edge edge : pathSoFarArray) {
			if(visitedNodes.contains(edge.getDestination())) {
				// Loop detected, we remove it.
				while(finalPath.size() > 0 && finalPath.get(finalPath.size() - 1).getDestination() != edge.getDestination()) {
					visitedNodes.remove(finalPath.get(finalPath.size() - 1).getDestination());
					finalPath.remove(finalPath.size() - 1);
				}
			}
			else {
				visitedNodes.add(edge.getDestination());
				finalPath.add(edge);
			}
		}

		return proxy.createPath(finalPath, request, this.isForward());
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
		return ((RoutingAlgorithm) leastDelayAlgorithm).isComplete() &&
				leastCostAlgorithm.isComplete() &&
				((RoutingAlgorithm) leastDelayAlgorithm).isOptimal() &&
				leastCostAlgorithm.isOptimal();
	}

	@Override
	public boolean isValid() {
		return ((RoutingAlgorithm) leastDelayAlgorithm).isValid() &&
				leastCostAlgorithm.isValid();
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
