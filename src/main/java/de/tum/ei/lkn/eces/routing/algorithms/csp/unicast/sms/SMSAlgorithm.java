package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sms;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.*;

/**
 * 1998
 * "Preferred link based delay-constrained least-cost routing
 *  in wide area networks."
 * R. Sriram, G. Manimaran and C. Siva Ram Murthy.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SMSAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * LocalMapper handling the SMSNodeData LocalComponents.
	 */
	private LocalMapper<SMSNodeData> SMSDataLocalMapper;

	/**
	 * Algorithm used to compute the LD path from any Node to the destination.
	 */
	private NToOneAlgorithm leastDelayAlgorithm;

	/**
	 * Algorithm used to compute the LC path from any Node to the destination.
	 */
	private NToOneAlgorithm leastCostAlgorithm;

	/**
	 * Heuristics function used for choosing the next link.
	 */
	private PreferredLinkMode heuristicFunction;

	/**
	 * Maximum number of preferred links which will be tried out.
	 */
	private int maxTries;

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

	public SMSAlgorithm(Controller controller, PreferredLinkMode heuristicFunction, int maxTries, NToOneAlgorithm leastCostAlgorithm, NToOneAlgorithm leastDelayAlgorithm) {
		super(controller);

		// Checking the provided algorithms are SP.
		if(!(leastCostAlgorithm instanceof SPAlgorithm) || !(leastDelayAlgorithm instanceof SPAlgorithm))
			throw new RoutingException("The underlying algorithms for SMS must be SPAlgorithms.");

		this.SMSDataLocalMapper = controller.getLocalMapper(this, SMSNodeDataLocal.class);
		this.leastCostAlgorithm = leastCostAlgorithm;
		this.leastDelayAlgorithm  = leastDelayAlgorithm;
		this.heuristicFunction = heuristicFunction;
		this.maxTries = maxTries;
	}

	public SMSAlgorithm(Controller controller) {
		this(controller, PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING);
	}

	public SMSAlgorithm(Controller controller, PreferredLinkMode heuristicFunction) {
		this(controller, heuristicFunction, 5);
	}

	public SMSAlgorithm(Controller controller, PreferredLinkMode heuristicFunction, int maxTries) {
		this(controller, heuristicFunction, maxTries, ProxyTypes.EDGE_PROXY);
	}

	public SMSAlgorithm(Controller controller, PreferredLinkMode heuristicFunction, int maxTries, ProxyTypes mode) {
		super(controller);
		this.SMSDataLocalMapper = controller.getLocalMapper(this, SMSNodeDataLocal.class);
		leastDelayAlgorithm = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastDelayAlgorithm).setMaximumProxy(mode);
		leastCostAlgorithm  = new DijkstraAlgorithm(controller);
		((DijkstraAlgorithm) leastCostAlgorithm).setMaximumProxy(mode);
		this.heuristicFunction = heuristicFunction;
		this.maxTries = maxTries;
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
		leastCostProxy = new PathPlumberProxy(proxy,
				new int[]{0},    // Cost as cost.
				new double[]{1}, // Cost * 1.
				new int[0],      // No constraints.
				new int[0]);     // No additional parameters.
		leastDelayProxy = new PathPlumberProxy(proxy,
				new int[]{1},    // Delay as cost.
				new double[]{1},
				new int[0],
				new int[0]);
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
		if(proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];
		else {
			leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
			return leastCostAlgorithm.getPathToNodeFrom(request.getSource());
		}

		// Compute the LD paths if RDM or CDP.
		Path ldp = null;
		if(heuristicFunction == PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING || heuristicFunction == PreferredLinkMode.COST_DELAY_PRODUCT) {
			if(BDFeature)
				((DijkstraAlgorithm) leastDelayAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, deadline);
			else
				leastDelayAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);

			// Impossible if the LD path has higher delay than the deadline.
			ldp = leastDelayAlgorithm.getPathToNodeFrom(request.getSource());
			if((BDFeature && ldp == null) || Proxy.violatesBound(ldp.getCost(), deadline))
				return null;
		}

		// Compute the LC paths if CDP and maxTries is infinite.
		if(heuristicFunction == PreferredLinkMode.COST_DELAY_PRODUCT && maxTries == Integer.MAX_VALUE) {
			if(BDFeature)
				((DijkstraAlgorithm) leastCostAlgorithm).computePathsFromAnyNodeTo(request.getDestination(), request, ldp.getParametersValues()[leastDelayProxy.getPlumberParameterId(0, request)]);
			else
				leastCostAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
		}

		// Init data structures.
		initNodes(request.getSource());

		Set<Node> visitedNodes = new HashSet<>();
		visitedNodes.add(request.getSource());
		boolean success = actionOnSetup(null, null, request.getSource(), visitedNodes, new LinkedList<>(), 0, 0, null, request, deadline);
		if(!success)
			return null;
		else {
			SMSNodeData dstData = SMSDataLocalMapper.get(request.getDestination().getEntity());
			return new Path(new SMSNodeDataIterator(dstData), dstData.getCostSoFar(), new double[]{dstData.getDelaySoFar()}, dstData.getParametersSoFar());
		}
	}

	private boolean actionOnSetup(SMSNodeData previousNodeData, Edge previousEdge, Node node, Set<Node> visitedNodes, LinkedList<Edge> pathSoFar, double costSoFar, double delaySoFar, double[] parametersSoFar, UnicastRequest request, double deadline) {
		SMSNodeData nodeData = SMSDataLocalMapper.get(node.getEntity());
		nodeData.setPreviousNodeData(previousNodeData);
		nodeData.setPreviousEdge(previousEdge);

		if(node == request.getDestination()) {
			nodeData.setDelaySoFar(delaySoFar);
			nodeData.setCostSoFar(costSoFar);
			nodeData.setParametersSoFar(parametersSoFar);
			return true;
		}

		int tries = 0;
		List<Edge> PLT = new ArrayList<>();

		/* Since cost/delay depends on the request, all the heuristics are
		 * call-specific heuristics for us. Hence, we have to compute PLT
		 * each time. */
		if(heuristicFunction == PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING) {
			Map<Edge, Double> edgeRDMs = new HashMap<>();
			List<Edge> edges = new ArrayList<>();
			for(Edge outEdge : node.getOutgoingConnections()) {
				double[] newParameters = proxy.getNewParameters(pathSoFar, outEdge, parametersSoFar, request, this.isForward());
				if(proxy.hasAccess(pathSoFar, outEdge, newParameters, request, this.isForward())) {
					double linkCost = proxy.getCost(pathSoFar, outEdge, newParameters, request, this.isForward());
					double linkDelay = proxy.getConstraintsValues(pathSoFar, outEdge, newParameters, request, this.isForward())[0];
					Path LDPath = leastDelayAlgorithm.getPathToNodeFrom(outEdge.getDestination());
					double leastDelay;
					if(LDPath == null)
						leastDelay = Double.MAX_VALUE;
					else
						leastDelay = LDPath.getCost();
					double denominator = deadline - delaySoFar - linkDelay - leastDelay;
					if(denominator < 0)
						continue;
					double RDM = linkCost / denominator;
					edgeRDMs.put(outEdge, RDM);
					edges.add(outEdge);
				}
			}

			// Sorting PLT.
			Collections.sort(edges, (left, right) -> {
				double leftRDM = edgeRDMs.get(left);
				double rightRDM = edgeRDMs.get(right);
				if(leftRDM < rightRDM)
					return -1;
				else if(leftRDM > rightRDM)
					return 1;
				else
					return 0;
			});

			// Storing first maxTries entries.
			if(edges.size() > maxTries)
				PLT = edges.subList(0, maxTries);
			else
				PLT = edges;
		}

		if(heuristicFunction == PreferredLinkMode.COST_DELAY_PRODUCT) {
			Map<Edge, Double> edgeCPDs = new LinkedHashMap<>();
			List<Edge> edges = new ArrayList<>();
			for(Edge outEdge : node.getOutgoingConnections()) {
				double[] newParameters = proxy.getNewParameters(pathSoFar, outEdge, parametersSoFar, request, this.isForward());
				if(proxy.hasAccess(pathSoFar, outEdge, newParameters, request, this.isForward())) {
					double linkCost = proxy.getCost(pathSoFar, outEdge, newParameters, request, this.isForward());
					double linkDelay = proxy.getConstraintsValues(pathSoFar, outEdge, newParameters, request, this.isForward())[0];
					Path LDPath = leastDelayAlgorithm.getPathToNodeFrom(outEdge.getDestination());
					double leastDelay;
					if(LDPath == null)
						leastDelay = Double.MAX_VALUE;
					else
						leastDelay = LDPath.getCost();
					double CDP = linkCost * (linkDelay + leastDelay);
					edgeCPDs.put(outEdge, CDP);
					edges.add(outEdge);
				}
			}

			// Sorting PLT.
			Collections.sort(edges, (left, right) -> {
				double leftCDP = edgeCPDs.get(left);
				double rightCDP = edgeCPDs.get(right);
				if(leftCDP < rightCDP)
					return -1;
				else if(leftCDP > rightCDP)
					return 1;
				else
					return 0;
			});

			// Take the first maxTries entries.
			if(edges.size() > maxTries)
				edges = edges.subList(0, maxTries);

			if(maxTries < Integer.MAX_VALUE) {
				// Add LC at the beginning if not already in.
				Path lcPath = leastCostAlgorithm.getPathToNodeFrom(node);
				if((BDFeature && lcPath != null) || !BDFeature) {
					Edge LC = lcPath.getPath()[0];
					if (!edges.contains(LC)) {
						edges.add(0, LC);
						edges = edges.subList(0, edges.size() - 1);
					}
				}

				// Replace last element by LD if not already in.
				Path ldPath = leastDelayAlgorithm.getPathToNodeFrom(node);
				if((BDFeature && ldPath != null) || !BDFeature) {
					Edge LD = ldPath.getPath()[0];
					if (!edges.contains(LD)) {
						edges.set(edges.size() - 1, LD);
					}
				}
			}

			// Storing PLT.
			PLT = edges;
		}

		if(heuristicFunction == PreferredLinkMode.PARTITION_BASED_ORDERING) {
			HashMap<Edge, Double> edgeCosts = new HashMap<>();
			HashMap<Edge, Double> edgeDelays = new HashMap<>();
			List<Edge> edges = new ArrayList<>();

			double totalCost  = 0;
			int numberOfCosts = 0;
			for(Edge outEdge : node.getOutgoingConnections()) {
				double[] newParameters = proxy.getNewParameters(pathSoFar, outEdge, parametersSoFar, request, isForward());
				if(proxy.hasAccess(pathSoFar, outEdge, newParameters, request, this.isForward())) {
					double cost = proxy.getCost(pathSoFar, outEdge, newParameters, request, isForward());
					double delay = proxy.getConstraintsValues(pathSoFar, outEdge, newParameters, request, isForward())[0];
					totalCost += cost;
					numberOfCosts++;
					edgeCosts.put(outEdge, cost);
					edgeDelays.put(outEdge, delay);
					edges.add(outEdge);
				}
			}

			// Computing average cost.
			double averageCost = totalCost / numberOfCosts;

			List<Edge> below = new ArrayList<>();
			List<Edge> above = new ArrayList<>();
			for(Edge edge : edges)
				if(edgeCosts.get(edge) <= averageCost)
					below.add(edge);
				else
					above.add(edge);

			// Sort below and above by delay value.
			Collections.sort(below, (left, right) -> {
				double leftDelay = edgeDelays.get(left);
				double rightDelay = edgeDelays.get(right);
				if(leftDelay < rightDelay)
					return -1;
				else if(leftDelay > rightDelay)
					return 1;
				else
					return 0;
			});

			Collections.sort(above, (left, right) -> {
				double leftDelay = edgeDelays.get(left);
				double rightDelay = edgeDelays.get(right);
				if(leftDelay < rightDelay)
					return -1;
				else if(leftDelay > rightDelay)
					return 1;
				else
					return 0;
			});

			// Add above at the end of below.
			below.addAll(above);

			// Storing first maxTries entries.
			if(below.size() > maxTries)
				PLT = below.subList(0, maxTries);
			else
				PLT = below;
		}

		if(PLT.size() > 16)
			throw new RuntimeException();

		while(tries < maxTries && tries < PLT.size()) {
			Edge tryEdge = PLT.get(tries++);

			// Bandwidth check: done by the hasAccess.

			// Loop and delay checks.
			double[] newParameters = proxy.getNewParameters(pathSoFar, tryEdge, parametersSoFar, request, this.isForward());
			double delay = proxy.getConstraintsValues(pathSoFar, tryEdge, newParameters, request, this.isForward())[0];
			if(!visitedNodes.contains(tryEdge.getDestination()) && !Proxy.violatesBound(delaySoFar + delay , deadline)) {
				double cost = proxy.getCost(pathSoFar, tryEdge, newParameters, request, this.isForward());
				pathSoFar.add(0, tryEdge);
				visitedNodes.add(tryEdge.getDestination());
				boolean success = actionOnSetup(nodeData, tryEdge, tryEdge.getDestination(), visitedNodes, pathSoFar, costSoFar + cost, delaySoFar + delay, newParameters, request, deadline);
				if(success) {
					return true;
				}
				else {
					pathSoFar.remove(0);
					visitedNodes.remove(tryEdge.getDestination());
				}
			}
		}

		// sent == false.
		return false;
	}

	private void initNodes(Node source) {
		for(Node node : source.getGraph().getNodes()) {
			SMSNodeData nodeData = SMSDataLocalMapper.get(node.getEntity());
			nodeData.init();
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
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isValid() {
		return ((RoutingAlgorithm) leastDelayAlgorithm).isValid() &&
				((RoutingAlgorithm) leastCostAlgorithm).isValid();
	}

	@Override
	public void enableBD() {
		this.BDFeature = true;
		if(heuristicFunction == PreferredLinkMode.COST_DELAY_PRODUCT && maxTries == Integer.MAX_VALUE) {
			leastDelayProxy = new PathPlumberProxy(proxy,
					new int[]{1},    // Delay as cost.
					new double[]{1},
					new int[0],
					new int[]{0});   // Cost as additional parameter if BD for CDP.
			((RoutingAlgorithm) this.leastDelayAlgorithm).setProxy(leastDelayProxy);
		}
	}

	@Override
	public void disableBD() {
		this.BDFeature = false;
		leastDelayProxy = new PathPlumberProxy(proxy,
				new int[]{1},    // Delay as cost.
				new double[]{1},
				new int[0],
				new int[0]);
		((RoutingAlgorithm) this.leastDelayAlgorithm).setProxy(leastDelayProxy);
	}
}

