package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sfdclc;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
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

/**
 * 2005
 * "An efficient quality of service routing algorithm for delay-sensitive
 *  applications"
 * Wei Liu, Wenjing Lou, Yuguang Fang.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SFDCLCAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Algorithm used to compute LD paths.
	 */
	private NToOneAlgorithm leastDelay;

	/**
	 * Algorithm used to compute LC paths.
	 */
	private NToOneAlgorithm leastCost;

	/**
	 * Whether BD is enabled or not.
	 */
	private boolean BDFeature = false;

	/**
	 * PlumberProxy used by the LD algorithm.
	 */
	private PathPlumberProxy leastDelayProxy;

	/**
	 * PlumberProxy used by the LC algorithm.
	 */
	private PathPlumberProxy leastCostProxy;

	public SFDCLCAlgorithm(Controller controller, NToOneAlgorithm leastDelay, NToOneAlgorithm leastCost) {
		super(controller);

		this.leastDelay = leastDelay;
		this.leastCost = leastCost;
	}

	public SFDCLCAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public SFDCLCAlgorithm(Controller controller, ProxyTypes mode) {
        super(controller);
		leastDelay = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastDelay).setMaximumProxy(mode);
		leastCost  = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastCost).setMaximumProxy(mode);
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
		leastDelayProxy = new PathPlumberProxy(proxy,
				new int[]{1},    // Only delay as cost.
				new double[]{1}, // Delay * 1.
				new int[0],      // No constraints.
				new int[]{0});   // Cost as parameter.
		leastCostProxy  = new PathPlumberProxy(proxy,
				new int[]{0},    // Only cost as cost.
				new double[]{1}, // Cost * 1.
				new int[0],      // No constraints.
				new int[]{1});   // Delay as parameter.
		((RoutingAlgorithm) leastDelay).setProxy(leastDelayProxy);
		((RoutingAlgorithm) leastCost).setProxy(leastCostProxy);
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
			leastCost.computePathsFromAnyNodeTo(request.getDestination(), request);
			return leastCost.getPathToNodeFrom(request.getSource());
		}

		// Compute the LD paths.
		if(BDFeature)
			((DijkstraAlgorithm) leastDelay).computePathsFromAnyNodeTo(request.getDestination(), request, deadline);
		else
			leastDelay.computePathsFromAnyNodeTo(request.getDestination(), request);

		// Impossible if the LD path has higher delay than the deadline.
		Path ldp = leastDelay.getPathToNodeFrom(request.getSource());
		if((BDFeature && ldp == null) || Proxy.violatesBound(ldp.getCost(), deadline))
			return null;

		// Compute the LC paths.
		if(BDFeature)
			((DijkstraAlgorithm) leastCost).computePathsFromAnyNodeTo(request.getDestination(), request, ldp.getParametersValues()[leastDelayProxy.getPlumberParameterId(0, request)]);
		else
			leastCost.computePathsFromAnyNodeTo(request.getDestination(), request);

	    double delaySoFar = 0;
	    double costSoFar = 0;
		double parametersSoFar[] = null;
	    LinkedList<Edge> path = new LinkedList<>();

		// Step 1.
		// (1) (done at line 135)
		Node currentNode = request.getSource();

		// Step 2.
		while(currentNode != request.getDestination()) {
			// (2)
			if(!Proxy.violatesBound(getLCPDelay(currentNode, request) + delaySoFar , deadline)) {
				Edge nextEdge = getLCPEdge(currentNode);
				if (nextEdge != null) {
					currentNode = nextEdge.getDestination();
					parametersSoFar = proxy.getNewParameters(path, nextEdge, parametersSoFar, request, this.isForward());
					delaySoFar += proxy.getConstraintsValues(path, nextEdge, parametersSoFar, request, this.isForward())[0];
					costSoFar += proxy.getCost(path, nextEdge, parametersSoFar, request, this.isForward());
					path.addFirst(nextEdge);
					continue;
				}
			}

			// (3)
			Edge bestEdge = null;
			double bestEdgeWeight = Double.MAX_VALUE;
			double bestEdgeParameters[] = null;

			for(Edge edge : currentNode.getOutgoingConnections()) {
				double newParameters[] = proxy.getNewParameters(path, edge, parametersSoFar, request, this.isForward());
				if(proxy.hasAccess(path, edge, newParameters, request, this.isForward())) {
					double weight = getWeight(path, edge, newParameters, delaySoFar, request);
					if(weight < bestEdgeWeight) {
						bestEdge = edge;
						bestEdgeWeight = weight;
						bestEdgeParameters = newParameters;
					}
				}
			}

			// No access to any Edge, we are blocked: no Path found.
			if(bestEdge == null)
				return null;

			// Updating delay, cost and path so far.
			currentNode = bestEdge.getDestination();
			parametersSoFar = bestEdgeParameters;
			delaySoFar += proxy.getConstraintsValues(path, bestEdge, parametersSoFar, request, this.isForward())[0];
			costSoFar += proxy.getCost(path, bestEdge, parametersSoFar, request, this.isForward());
			path.addFirst(bestEdge);
		}

		return new Path(path, costSoFar, new double[]{delaySoFar}, parametersSoFar);
    }

	/**
	 * Gets the delay of the LD path from a given Node to the destination.
	 * @param node Given Node.
	 * @param request Request for which a Path is being computed.
	 * @return Delay of the LD path or + infinity if it does not exist.
	 */
	private double getLDPDelay(Node node, UnicastRequest request) {
		Path path = leastDelay.getPathToNodeFrom(node);
		if(path == null)
			if(node == request.getDestination())
				return 0;
			else
				return Double.MAX_VALUE;
		return path.getCost();
	}

	/**
	 * Gets the cost of the LD path from a given Node to the destination.
	 * @param node Given Node.
	 * @param request Request for which a Path is being computed.
	 * @return Cost of the LD path or + infinity if it does not exist.
	 */
	private double getLDPCost(Node node, UnicastRequest request) {
		Path path = leastDelay.getPathToNodeFrom(node);
		if(path == null)
			if(node == request.getDestination())
				return 0;
			else
				return Double.MAX_VALUE;
		return path.getParametersValues()[path.getParametersValues().length - 1];
	}

	/**
	 * Gets the next Edge of the LC path from a given Node to the destination.
	 * @param node Given Node.
	 * @return Next Edge or null if such a Path does not exist.
	 */
	private Edge getLCPEdge(Node node) {
		Path path = leastCost.getPathToNodeFrom(node);
		if(path == null)
			return null;
		else
			return path.getPath()[0];
	}

	/**
	 * Gets the delay of the LC path from a given Node to the destination.
	 * @param node Given Node.
	 * @param request Request for which a Path is being computed.
	 * @return Delay of the LC path or + infinity if it does not exist.
	 */
	private double getLCPDelay(Node node, UnicastRequest request) {
		Path path = leastCost.getPathToNodeFrom(node);
		if(path == null)
			if(node == request.getDestination())
				return 0;
			else
				return Double.MAX_VALUE;
		return path.getParametersValues()[path.getParametersValues().length - 1];
	}

	/**
	 * Gets the cost of the LC path from a given Node to the destination.
	 * @param node Given Node.
	 * @param request Request for which a Path is being computed.
	 * @return Cost of the LC path or + infinity if it does not exist.
	 */
	private double getLCPCost(Node node, UnicastRequest request) {
		Path path = leastCost.getPathToNodeFrom(node);
		if(path == null)
			if(node == request.getDestination())
				return 0;
			else
				return Double.MAX_VALUE;
		return path.getCost();
	}

	/**
	 * Computes the weight of a candidate Edge for the next hop (p. 92 of the
	 * paper).
	 * @param path Current Path chosen before the candidate Edge.
	 * @param edge Candidate Edge.
	 * @param parameters Parameters of the Path so far including the candidate
	 *                   Edge.
	 * @param delaySoFar Delay so far (excluding candidate Edge).
	 * @param request Request for which a Path is being computed.
	 * @return Weight of the candidate Edge.
	 */
	private double getWeight(Iterable<Edge> path, Edge edge, double[] parameters, double delaySoFar, UnicastRequest request) {
		double deadline = proxy.getConstraintsBounds(request)[0];
        if(!Proxy.violatesBound(delaySoFar + proxy.getConstraintsValues(path, edge, parameters, request, this.isForward())[0] + getLDPDelay(edge.getDestination(), request) ,deadline))
            return proxy.getCost(path, edge, parameters, request, this.isForward()) + getSFCost(path, edge, parameters, delaySoFar, request);
		else
        	return Double.MAX_VALUE;
    }

	/**
	 * Computes the cost' (residual cost) of a candidate Edge (p. 92 of the
	 * paper).
	 * @param path Current Path chosen before the candidate Edge.
	 * @param edge Candidate Edge.
	 * @param parameters Parameters of the Path so far including the candidate
	 *                   Edge.
	 * @param delaySoFar Delay so far (excluding candidate Edge).
	 * @param request Request for which a Path is being computed.
	 * @return Cost' of the candidate Edge.
	 */
    private double getSFCost(Iterable<Edge> path, Edge edge, double[] parameters, double delaySoFar, UnicastRequest request) {
		double deadline = proxy.getConstraintsBounds(request)[0];
        double leastCostPathDelay = getLCPDelay(edge.getDestination(), request);

        if(!Proxy.violatesBound(delaySoFar + proxy.getConstraintsValues(path, edge, parameters, request, this.isForward())[0] + leastCostPathDelay , deadline))
            return getLCPCost(edge.getDestination(), request);
        else
            return getLDPCost(edge.getDestination(), request);
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
		return ((RoutingAlgorithm) leastDelay).isComplete() &&
				((RoutingAlgorithm) leastCost).isComplete() &&
				((RoutingAlgorithm) leastDelay).isOptimal() &&
				((RoutingAlgorithm) leastCost).isOptimal();
	}

	@Override
	public boolean isValid() {
		return ((RoutingAlgorithm) leastDelay).isValid() &&
				((RoutingAlgorithm) leastCost).isValid();
	}

	@Override
	public void enableBD() {
		this.BDFeature = true;
	}

	@Override
	public void disableBD() {
		this.BDFeature = false;
	}
}
