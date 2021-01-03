package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcur;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
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
 * 2000
 * "A Distributed Algorithm for Delay-Constrained Unicast Routing"
 * Douglas S. Reeves and Hussein F. Salama.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCURAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * LocalMapper handling the DCUR DCURNodeData LocalComponents.
	 */
	private LocalMapper<DCURNodeData> DCURDataLocalMapper;

	/**
	 * Algorithm used to compute the LD path from any Node to the destination.
	 */
	private NToOneAlgorithm leastDelayAlgorithm;

	/**
	 * Algorithm used to compute the LC path from any Node to the destination.
	 */
	private NToOneAlgorithm leastCostAlgorithm;

	/**
	 * PlumberProxy used by the LD algorithm.
	 */
	private PathPlumberProxy leastDelayProxy;

	/**
	 * PlumberProxy used by the LC algorithm.
	 */
	private PathPlumberProxy leastCostProxy;

	/**
	 * List of Edges of the Path chosen so far.
	 */
	private LinkedList<Edge> pathSoFar;

	/**
	 * Whether BD is enable or not.
	 */
	private boolean BDFeature = false;

	public DCURAlgorithm(Controller controller, NToOneAlgorithm leastCostAlgorithm, NToOneAlgorithm leastDelayAlgorithm) {
		super(controller);

		this.DCURDataLocalMapper = controller.getLocalMapper(this, DCURNodeDataLocal.class);
		this.leastCostAlgorithm = leastCostAlgorithm;
		this.leastDelayAlgorithm  = leastDelayAlgorithm;
	}

	public DCURAlgorithm(Controller controller) {
		this(controller, ProxyTypes.EDGE_PROXY);
	}

	public DCURAlgorithm(Controller controller, ProxyTypes mode) {
		super(controller);
		this.DCURDataLocalMapper = controller.getLocalMapper(this, DCURNodeDataLocal.class);
		leastDelayAlgorithm = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastDelayAlgorithm).setMaximumProxy(mode);
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
				new int[0]);     // No additional parameters.

		((RoutingAlgorithm) this.leastCostAlgorithm).setProxy(leastCostProxy);
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
		else {
			leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
			return leastCostAlgorithm.getPathToNodeFrom(request.getSource());
		}

		if(BDFeature) {
			// Compute the LD paths.
			((DijkstraAlgorithm) leastDelayAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, deadline);

			// Impossible if the LD path has higher delay than the deadline.
			Path ldp = leastDelayAlgorithm.getPathToNodeFrom(request.getSource());
			if (ldp == null)
				return null;

			// Compute the LC paths.
			((DijkstraAlgorithm) leastCostAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, ldp.getParametersValues()[leastDelayProxy.getPlumberParameterId(0, request)]);
		}
		else {
			// Compute the LD paths.
			leastDelayAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);

			// Impossible if the LD path has higher delay than the deadline.
			Path ldp = leastDelayAlgorithm.getPathToNodeFrom(request.getSource());
			if (ldp == null || Proxy.violatesBound(ldp.getCost(), deadline))
				return null;

			// Compute the LC paths.
			leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
		}

		// Init data structures.
		initNodes(request.getSource());
		pathSoFar = new LinkedList<>();

		// Construct the Path (no previous node, no delay/cost/parameters so far).
		constructPath(request.getSource(), null, 0, 0, null, request, deadline);

		// Create Path out of the result.
		DCURNodeData destinationData = DCURDataLocalMapper.get(request.getDestination().getEntity());
		Path result = new Path(pathSoFar, destinationData.getCostSoFar(), new double[]{destinationData.getDelaySoFar()}, destinationData.getParametersSoFar());

		// DCUR might have been blocked before the destination, we check this.
		if(result.getPath()[result.getPath().length - 1].getDestination() != request.getDestination())
			return null;
		else
			return result;
	}

	private void constructPath(Node activeNode, Node previousNode, double delaySoFar, double costSoFar, double[] parametersSoFar, UnicastRequest request, double deadline) {
		DCURNodeData nodeData = DCURDataLocalMapper.get(activeNode.getEntity());
		if(activeNode == request.getDestination()) {
			nodeData.setDelaySoFar(delaySoFar);
			nodeData.setCostSoFar(costSoFar);
			nodeData.setParametersSoFar(parametersSoFar);
			nodeData.setTraversed(true);
			return;
		}

		if(nodeData.isTraversed()) {
			removeLoop(previousNode, request, deadline);
			return;
		}

		nodeData.setChosenLC();

		Edge lcNextEdge = null;
		Edge ldNextEdge = null;
		Path lcPath = leastCostAlgorithm.getPathToNodeFrom(activeNode);
		Path ldPath = leastDelayAlgorithm.getPathToNodeFrom(activeNode);
		if(lcPath != null)
			lcNextEdge = lcPath.getPath()[0];
		if(ldPath != null)
			ldNextEdge = ldPath.getPath()[0];

		if(lcNextEdge == ldNextEdge)
			nodeData.setChosenLD();

		if(nodeData.isInvalid(lcNextEdge))
			nodeData.setChosenLD();

		if(lcNextEdge != null && nodeData.hasChosenLC()) {
			double[] newParameters = proxy.getNewParameters(pathSoFar, lcNextEdge, parametersSoFar, request, true);
			double constraintValue = proxy.getConstraintsValues(pathSoFar, lcNextEdge, newParameters, request, true)[0];
			Path path = leastDelayAlgorithm.getPathToNodeFrom(lcNextEdge.getDestination());
			if(path != null &&
					!Proxy.violatesBound(delaySoFar + constraintValue + path.getCost() , deadline)) {
				nodeData.setDelaySoFar(delaySoFar);
				nodeData.setCostSoFar(costSoFar);
				nodeData.setParametersSoFar(parametersSoFar);
				nodeData.setTraversed(true);

				double edgeCost = proxy.getCost(pathSoFar, lcNextEdge, newParameters, request, true);
				pathSoFar.addFirst(lcNextEdge);
				constructPath(lcNextEdge.getDestination(), activeNode, delaySoFar + constraintValue, costSoFar + edgeCost, newParameters, request, deadline);
				return;
			}
		}

		// Choose LD if delay of LC is too high or LC is null.
		nodeData.setChosenLD();

		if(ldNextEdge != null && nodeData.hasChosenLD()) {
			nodeData.setDelaySoFar(delaySoFar);
			nodeData.setCostSoFar(costSoFar);
			nodeData.setParametersSoFar(parametersSoFar);
			nodeData.setTraversed(true);

			double[] newParameters = proxy.getNewParameters(pathSoFar, ldNextEdge, parametersSoFar, request, true);
			double constraintValue = proxy.getConstraintsValues(pathSoFar, ldNextEdge, newParameters, request, true)[0];
			double edgeCost = proxy.getCost(pathSoFar, ldNextEdge, newParameters, request, true);
			pathSoFar.addFirst(ldNextEdge);
			constructPath(ldNextEdge.getDestination(), activeNode, delaySoFar + constraintValue, costSoFar + edgeCost, newParameters, request, deadline);
			return;
		}

		// If both LC/LD don't work, go back.
		nodeData.init();
		removeLoop(previousNode, request, deadline);
		return;
	}

	private void removeLoop(Node node, UnicastRequest request, double deadline) {
		if(node == null)
			return;

		// Remove the last EDGE chosen.
		Edge usedEdge = pathSoFar.removeFirst();

		// If LC was chosen, choose now LD.
		DCURNodeData nodeData = DCURDataLocalMapper.get(node.getEntity());
		if(nodeData.hasChosenLC()) {
			nodeData.init();
			nodeData.addInvalidEdge(usedEdge);

			/* This call will choose LD and then continue Path construction.
			 * The point of calling constructPath rather than directly choosing
			 * LD is to handle the case where LD is null. */
			Node previousNode = null;
			if (pathSoFar.size() > 0)
				previousNode = pathSoFar.getFirst().getSource();
			constructPath(node, previousNode, nodeData.getDelaySoFar(), nodeData.getCostSoFar(), nodeData.getParametersSoFar(), request, deadline);
			return;
		}
		else {
			Node previousNode = null;
			if (pathSoFar.size() > 0)
				previousNode = pathSoFar.getFirst().getSource();
			nodeData.init();
			removeLoop(previousNode, request, deadline);
			return;
		}
	}

	private void initNodes(Node source) {
		for(Node node : source.getGraph().getNodes()) {
			DCURNodeData nodeData = DCURDataLocalMapper.get(node.getEntity());
			nodeData.init();
			nodeData.setNode(node);
		}
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
				((RoutingAlgorithm) leastCostAlgorithm).isComplete() &&
				((RoutingAlgorithm) leastDelayAlgorithm).isOptimal() &&
				((RoutingAlgorithm) leastCostAlgorithm).isOptimal();
	}

	@Override
	public boolean isValid() {
		return ((RoutingAlgorithm) leastDelayAlgorithm).isValid() &&
				((RoutingAlgorithm) leastCostAlgorithm).isValid();
	}

	public void enableBD() {
		this.BDFeature = true;
		this.setProxy();
	}

	public void disableBD() {
		this.BDFeature = false;
		this.setProxy();
	}
}

