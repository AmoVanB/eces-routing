package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.meb;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.LinkedList;
import java.util.Map;

/**
 * MEB algorithm used by DEB.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MEBAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	private LocalMapper<NodeData> nodeDataLocalMapper;
	private boolean forward = true;
	private Triplet bestTriplet;

	/**
	 * Multipliers for considering only the cost as optimization metric.
	 */
	private static final double[] leastCostMultipliers = {1, 0};

	/**
	 * Multipliers for considering only the delay as optimization metric.
	 */
	private static final double[] leastDelayMultipliers = {0, 1};
	PathPlumberProxy plumberProxy;

	private boolean BDfeature = false;
	private boolean delayMode;


	public MEBAlgorithm(Controller controller) {
		super(controller);
		nodeDataLocalMapper = controller.getLocalMapper(this, NodeDataLocal.class);
	}
	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	public Response computePath(UnicastRequest request) {
		int costParameterID = this.plumberProxy.getPlumberParameterId(0, request);
		int delayParameterID = this.plumberProxy.getPlumberParameterId(1, request);
		initializeSingleSource(request);
		int numberOfNodes = request.getSource().getGraph().getNodes().size();
		// Note: we break if no updates has happened in an iteration.
		boolean run = true;
		double deadline = proxy.getConstraintsBounds(request)[0];
		bestTriplet = null;
		for(int hopCount = 0; hopCount < numberOfNodes && run; hopCount++) {
			run = false;
			for(Edge edge : request.getSource().getGraph().getEdges()) {
				if(relax(edge, request, hopCount, deadline, costParameterID, delayParameterID))
					run = true;
			}
		}
		if(bestTriplet == null)
			return null;
		LinkedList<Edge> path = new LinkedList<>();
		for(Edge edge: new TripletIterator(bestTriplet))
			path.addFirst(edge);
		return proxy.createPath(path, request, isForward());
	}

	private void initializeSingleSource(UnicastRequest request) {
		Node source;
		if(isForward())
			source = request.getSource();
		else
			source = request.getDestination();
		for(Node node : source.getGraph().getNodes()) {
			NodeData nodeData = nodeDataLocalMapper.get(node.getEntity());
			nodeData.init();
			nodeData.setNode(node);
			if(node == source) {
				Map<Integer, Triplet> data = nodeData.getData();
				data.put(-1, new Triplet(0, 0, 0, 0, null, nodeData, null, null));
			}
		}
	}

	private boolean relax(Edge edge, UnicastRequest request, int newHopCount, double deadline, int costParameterID, int delayParameterID) {
		Node node;
		Node nextNode;
		if(isForward()){
			node = edge.getSource();
			nextNode = edge.getDestination();
		} else {
			node = edge.getDestination();
			nextNode = edge.getSource();
		}
		NodeData nodeData = nodeDataLocalMapper.get(node.getEntity());
		NodeData nextNodeData = nodeDataLocalMapper.get(nextNode.getEntity());

		Triplet thisTriplet = nodeData.getData().get(newHopCount - 1);
		if(thisTriplet == null)
			return false;

		Iterable<Edge> iterator = new TripletIterator(nodeData.getData().get(newHopCount - 1));
		double[] newParameters = this.plumberProxy.getNewParameters(iterator, edge, thisTriplet.getParameters(), request, isForward());
		if(!this.plumberProxy.hasAccess(iterator, edge, newParameters, request, isForward()))
			return false;

		double edgeCost = this.plumberProxy.getCost(iterator, edge, newParameters, request, isForward());

		Triplet nextTriplet = nextNodeData.getData().get(newHopCount);
		if(nextTriplet == null) {
			nextTriplet = new Triplet(newHopCount, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, null, nextNodeData, edge, nodeData);
			nextNodeData.getData().put(newHopCount, nextTriplet);
		}

		if(thisTriplet.getWeight() != Double.POSITIVE_INFINITY && (nextNodeData.getNode() == request.getDestination() && isForward() ||
				nextNodeData.getNode() == request.getSource() && !isForward() )) {
			if(!Proxy.violatesBound(newParameters[delayParameterID], deadline)) {
				if(bestTriplet == null || newParameters[costParameterID] < bestTriplet.getCost()) {
					nextTriplet.setWeight(thisTriplet.getWeight() + edgeCost);
					nextTriplet.setCost(newParameters[costParameterID]);
					nextTriplet.setDelay(newParameters[delayParameterID]);
					nextTriplet.setParameters(newParameters);
					nextTriplet.setPreviousEdge(edge);
					nextTriplet.setPreviousNode(nodeData);
					bestTriplet = nextTriplet;
					return true;
				}
			}
		} else if(nextTriplet.getWeight() > thisTriplet.getWeight() + edgeCost) {
			if(!BDfeature || !delayMode || !Proxy.violatesBound(newParameters[delayParameterID], deadline)) {
				nextTriplet.setWeight(thisTriplet.getWeight() + edgeCost);
				nextTriplet.setCost(newParameters[costParameterID]);
				nextTriplet.setDelay(newParameters[delayParameterID]);
				nextTriplet.setParameters(newParameters);
				nextTriplet.setPreviousEdge(edge);
				nextTriplet.setPreviousNode(nodeData);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isForward() {
		return forward;
	}

	public void setForward(){
		forward = true;
	}

	public void setBackward(){
		forward = false;
	}

	public void leastCostMode(){
		if(plumberProxy != null){
			plumberProxy.setCostMultipliers(leastCostMultipliers);
		}
		delayMode = false;
	}
	public void leastDelayMode(){
		if(plumberProxy != null){
			plumberProxy.setCostMultipliers(leastDelayMultipliers);
		}
		delayMode = true;
	}
	@Override
	public boolean isOptimal() {
		return false;
	}

	@Override
	public boolean isComplete() {
		if(proxy != null)
			switch(proxy.getType()) {
				case EDGE_PROXY:
					return true;
				case PREVIOUS_EDGE_PROXY:
					return false;
				case PATH_PROXY:
					return false;
			}
		throw new RoutingException("The proxy is missing at the routing algorithm");
	}

	@Override
	public boolean isValid() {
		if(proxy != null)
			switch(proxy.getType()) {
				case EDGE_PROXY:
					return true;
				case PREVIOUS_EDGE_PROXY:
					return false;
				case PATH_PROXY:
					return false;
			}
		throw new RoutingException("The proxy is missing at the routing algorithm");
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
