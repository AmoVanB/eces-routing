package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.Record;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.RecordLocal;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempData;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempIterablePath;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.Arrays;

/**
 * Bellman-Ford's algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class BellmanFordAlgorithm extends SPAlgorithm implements SolveUnicastRequest, BD {
	/**
	 * Amount of paths to compute.
	 */
	private int k;

	/**
	 * Cost border (for BD).
	 */
	private double costBorder = Double.POSITIVE_INFINITY;

	/**
	 * Mapper storing BF temporary data.
	 */
	private LocalMapper<Record> nodeDataLocalMapper;
	private LocalMapper<VisitedEdge> visitedEdgeLocalMapper;

	public BellmanFordAlgorithm(Controller controller) {
		this(controller, 1);
	}

	public BellmanFordAlgorithm(Controller controller, int k) {
		super(controller);
		nodeDataLocalMapper = controller.getLocalMapper(this, RecordLocal.class);
		visitedEdgeLocalMapper = controller.getLocalMapper(this, VisitedEdgeLocal.class);
		this.k = k;
	}
	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath( request);
	}

	public Path computePath(UnicastRequest request) {
		initializeSingleSource(request.getSource(), request);
		int numberOfNodes = request.getSource().getGraph().getNodes().size();

		// Note: we break if no updates has happened in an iteration.
		boolean run = true;
		for(int i = 1; i < numberOfNodes && run; i++) {
			run = false;
			for(Edge edge : request.getSource().getGraph().getEdges()) {
				if(visitedEdgeLocalMapper.get(edge.getEntity()).isUpdated()) {
					visitedEdgeLocalMapper.get(edge.getEntity()).setUpdated(false);
					if (relax(edge.getSource(), edge, request)) {
						run = true;
						for(Edge outEdge : edge.getDestination().getOutgoingConnections()){
							visitedEdgeLocalMapper.get(outEdge.getEntity()).setUpdated(true);
						}
					}
				}
			}
		}
		Record record = nodeDataLocalMapper.get(request.getDestination().getEntity());
		int minID = record.getMinID();
		if(minID == -1)
			return null;
		TempIterablePath iterator = record.getTempData(minID).getPath();
		if(!iterator.hasNext())
			return null;

		return new Path(iterator,
				record.getTempData(minID).getCost(),
				new double[0],
				record.getTempData(minID).getParameter());
	}

	private void initializeSingleSource(Node source, Request request) {
		for(Edge edge : source.getGraph().getEdges()) {
			if(edge.getSource() == source) {
				visitedEdgeLocalMapper.get(edge.getEntity()).setUpdated(true);
			}else{
				visitedEdgeLocalMapper.get(edge.getEntity()).setUpdated(false);
			}
		}

		for(Node node : source.getGraph().getNodes()) {
			Record nodeData = nodeDataLocalMapper.get(node.getEntity());
			if(!nodeData.hasArray()){
				TempData[] recordData = new TempData[k];
				for(int i = 0; i < k; i++){
					recordData[i] = new TempDataPredecessor();
				}
				nodeData.setArray(recordData);
			}

			for(int i = 0; i < k; i++)
				nodeData.getArray()[i].init();

			if(node == source) {
				TempDataPredecessor newData = (TempDataPredecessor) nodeData.getArray()[0];
				newData.setCost(0);
				newData.setGuess(0);
				double[] parameters = new double[proxy.getNumberOfParameters(request)];
				double[] constraints = new double[proxy.getNumberOfConstraints(request)];
				Arrays.fill(parameters, 0);
				Arrays.fill(constraints, 0);
				newData.setParameter(parameters);
				newData.setConstraint(constraints);
				newData.setPath(new TempIterablePath(null,null));
			}
		}
	}

	private boolean relax(Node node, Edge edge, UnicastRequest request) {
		Node nextNode = edge.getDestination();
		boolean changes = false;
		for(TempData nodeData : nodeDataLocalMapper.get(node.getEntity()).getArray()) {
			if(nodeData.getCost() < Double.MAX_VALUE) {
				Record nextNodeRecord = nodeDataLocalMapper.get(nextNode.getEntity());

				boolean run = true;
				for(TempData check : nextNodeRecord.getArray())
					if(nodeData.equals(((TempDataPredecessor)check).getPredecessor()) &&
							edge.equals(check.getPath().getEdge())){
						run = false;
						break;
					}
				if(run) {
					int maxID = -1;
					double maxValue = Double.NEGATIVE_INFINITY;
					for(int i = 0; i < nextNodeRecord.getArray().length; i++){
						if(maxValue < ((TempDataPredecessor)nextNodeRecord.getTempData(i)).getSum()){
							maxID = i;
							maxValue = ((TempDataPredecessor)nextNodeRecord.getTempData(i)).getSum();
						}
					}
					if (maxID != -1) {
						TempData nextNodeData = nextNodeRecord.getTempData(maxID);
						TempIterablePath iterator = nodeData.getPath();
						double[] newParameters = proxy.getNewParameters(iterator, edge, nodeData.getParameter(), request, isForward());
						if (!proxy.hasAccess(iterator, edge, newParameters, request, isForward()))
							continue;
						if (updateCosts(nodeData, nextNodeData, iterator, edge, newParameters, request)) {
							TempDataPredecessor newData = new TempDataPredecessor();
							newData.setCost(nextNodeData.getCost());
							newData.setGuess(((TempDataPredecessor)nextNodeData).getGuess());
							newData.setParameter(nextNodeData.getParameter());
							newData.setPath(nextNodeData.getPath());
							newData.setPredecessor((TempDataPredecessor) nodeData);
							nextNodeRecord.getArray()[maxID] = newData;
							changes = true;
						}
					}
				}
			}
		}
		return changes;
	}

	protected boolean updateCosts(TempData nodeData, TempData nextNodeData, TempIterablePath iterator, Edge edge, double[] newParameters,Request request){
		double edgeCost = proxy.getCost(iterator, edge, newParameters, request, isForward());
		double newCost = nodeData.getCost() + edgeCost;
		if(!Proxy.violatesBound(newCost, costBorder) && newCost < nextNodeData.getCost()) {
			nextNodeData.setCost(newCost);
			nextNodeData.setPath(new TempIterablePath(nodeData.getPath(), edge));
			nextNodeData.setParameter(newParameters);
			return true;
		}
		return false;
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
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
	public Response solve(Request request, double costBorder) {
		this.setCostBorder(costBorder);
		Response result = this.computePath((UnicastRequest) request);
		this.removeCostBorder();
		return result;
	}

	@Override
	public void setCostBorder(double costBorder) {
		this.costBorder = costBorder;
	}

	@Override
	public void removeCostBorder() {
		this.costBorder = Double.POSITIVE_INFINITY;
	}
}
