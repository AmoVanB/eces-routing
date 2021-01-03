package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 1994
 * "The Design and Evaluation of Routing Algorithms for Real-Time Channels"
 * Ron Widyono.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class CBFAlgorithm extends CSPAlgorithm implements SolveUnicastRequest{
	/**
	 * LocalMapper handling the CBF LocalComponents.
	 */
	private LocalMapper<NodeData> nodeDataLocalMapper;

	/**
	 * Whether CBF should keep track of all the paths he finds.
	 */
	private boolean paretoFrontierKeeping;

	/**
	 * If paretoFrontierKeeping is true, will contain all the paths found by CBF
	 * after a run, i.e. the pareto frontier.
	 */
	private List<Path> paretoFrontier;

	private Logger logger;


	public CBFAlgorithm(Controller controller) {
		super(controller);
		this.nodeDataLocalMapper = controller.getLocalMapper(this, NodeDataLocal.class);
		this.paretoFrontierKeeping = false;
		this.logger = Logger.getLogger(CBFAlgorithm.class);
	}

	public List<Path> getParetoFrontier() {
		return paretoFrontier;
	}

	public void activateParetoFrontierKeeping() {
		paretoFrontierKeeping = true;
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	synchronized public Path computePath(UnicastRequest request) {
		paretoFrontier = new ArrayList<>();
		double constraint = Double.MAX_VALUE;
		if (proxy.getNumberOfConstraints(request) > 0)
			constraint = proxy.getConstraintsBounds(request)[0];

		// Initialize data structures.
		logger.debug("Initializing data structures");
		initNodes(request.getSource());
		PriorityQueue<EdgeData> pq = new PriorityQueue<>();
		initSourceEdges(request, pq);
		logger.debug("Priority queue: " + pq);

		int sqnum = 1;

		while(!pq.isEmpty()) {
			EdgeData edgeData = pq.poll();
			logger.debug("Polled " + edgeData + " from priority queue: costSoFar=" + edgeData.getCostSoFar() + " delaySoFar=" + edgeData.getDelaySoFar() + " pathSoFar=" + edgeData.getPathSoFar());
			if(Proxy.violatesBound(edgeData.getDelaySoFar(), constraint)) {
				logger.debug(edgeData + " violates the bound (" + edgeData.getDelaySoFar() + " vs " + constraint + ")");
				break;
			}

			if(relaxed(edgeData, request)) {
				logger.debug("The current path to " + edgeData.getEdge().getDestination() + " is better, we update the next edges");
				for (Edge nextEdge : edgeData.getEdge().getDestination().getOutgoingConnections()) {
					updateEdge(edgeData, nextEdge, request, constraint, pq, sqnum);
				}
				sqnum++;
			}
		}

		logger.trace("Out of the loop");

		NodeData dstNodeData = nodeDataLocalMapper.get(request.getDestination().getEntity());
		if (dstNodeData.getPathSoFar() == null) {
			logger.debug("Destination was never reached, no path found!");
			return null;
		}

		logger.debug("Path found: " + dstNodeData.getPathSoFar());
		return new Path(dstNodeData.getPathSoFar(),isForward(), dstNodeData.getCost(), new double[]{dstNodeData.getDelay()}, dstNodeData.getParameters());
	}

	/**
	 * Relaxes the delay and cost at the nextEdges Node and update path to it if
	 * using the current Edge to reach it leads to a better cost.
	 * @return true if the cost to reach the Node via the new Edge is lower than
	 *         current cost to reach the Node, false otherwise.
	 */
	private boolean relaxed(EdgeData edgeData, UnicastRequest request) {
		logger.trace("Relaxing " + edgeData.getEdge().getDestination() + " with " + edgeData);
		NodeData dstNodeData = nodeDataLocalMapper.get(edgeData.getEdge().getDestination().getEntity());

		if(dstNodeData.getCost() <= edgeData.getCostSoFar()) {
			logger.trace("New cost is lower (" + dstNodeData.getCost() + " <= " + edgeData.getCostSoFar() + "), no relaxing!");
			return false;
		}

		if(paretoFrontierKeeping && edgeData.getEdge().getDestination() == request.getDestination())
				paretoFrontier.add(new Path(edgeData.getPathSoFar(), edgeData.getCostSoFar(), new double[]{edgeData.getDelaySoFar()}, edgeData.getParametersSoFar()));

		logger.trace("Relaxing destination node " + dstNodeData.getNode() + "!");
		dstNodeData.setCost(edgeData.getCostSoFar());
		dstNodeData.setDelay(edgeData.getDelaySoFar());
		dstNodeData.setParameters(edgeData.getParametersSoFar());
		dstNodeData.setPathSoFar(edgeData.getPathSoFar());
		return true;
	}

	private void initSourceEdges(UnicastRequest request, PriorityQueue<EdgeData> pq) {
		CBFIterator emptyPath = new CBFIterator();
		for(Edge edge : request.getSource().getOutgoingConnections()) {
			EdgeData edgeData = new EdgeData();
			double[] newParameters = proxy.getNewParameters(emptyPath, edge, null, request, this.isForward());

			if(proxy.hasAccess(emptyPath, edge, newParameters, request, this.isForward())) {
				edgeData.setEdge(edge);
				edgeData.setPathSoFar(new EdgePath(edge, null));
				edgeData.setCostSoFar(proxy.getCost(emptyPath, edge, newParameters, request, this.isForward()));
				if(proxy.getNumberOfConstraints(request) > 0)
					edgeData.setDelaySoFar(proxy.getConstraintsValues(emptyPath, edge, newParameters, request,this.isForward())[0]);
				else
					edgeData.setDelaySoFar(0);
				edgeData.setParametersSoFar(newParameters);
				pq.add(edgeData);
			}
		}
	}

	/**
	 * Updates the delay, cost and parameters of nextEdge based on their values
	 * at the current Edge. This is done only if the new delay is smaller or
	 * equal to constraint. The new Edge is added to pq.
	 */
	private void updateEdge(EdgeData edge, Edge nextEdge, UnicastRequest request, double constraint, PriorityQueue<EdgeData> pq, int sqnum) {
		logger.trace("Updating " + nextEdge + " from " + edge);
		CBFIterator path = new CBFIterator(edge);
		double[] newParameters = proxy.getNewParameters(path, nextEdge, edge.getParametersSoFar(), request, this.isForward());

		// For handling SP problems.
		double nextEdgeDelay = 0;
		if(proxy.getNumberOfConstraints(request) > 0)
			nextEdgeDelay = proxy.getConstraintsValues(path, nextEdge, newParameters, request, this.isForward())[0];

		if(proxy.hasAccess(path, nextEdge, newParameters, request, this.isForward()) && !Proxy.violatesBound(edge.getDelaySoFar() + nextEdgeDelay, constraint)) {
			logger.trace("Adding to the priority queue");
			EdgeData nextEdgeData = new EdgeData();
			nextEdgeData.setEdge(nextEdge);
			nextEdgeData.setPathSoFar(new EdgePath(nextEdge, edge.getPathSoFar()));
			nextEdgeData.setCostSoFar(edge.getCostSoFar() + proxy.getCost(path, nextEdge, newParameters, request, this.isForward()));
			if(proxy.getNumberOfConstraints(request) > 0)
				nextEdgeData.setDelaySoFar(edge.getDelaySoFar() + proxy.getConstraintsValues(path, nextEdge, newParameters, request, this.isForward())[0]);
			else
				nextEdgeData.setDelaySoFar(0);
			nextEdgeData.setParametersSoFar(newParameters);
			nextEdgeData.setSqnum(sqnum);
			pq.add(nextEdgeData);
		}
		else {
			logger.trace("Not adding to the priority queue because access denied!");
		}
	}

	private void initNodes(Node source) {
		for(Node node : source.getGraph().getNodes()) {
			NodeData nodeData = nodeDataLocalMapper.get(node.getEntity());
			nodeData.init();
			nodeData.setNode(node);
		}

		NodeData data = nodeDataLocalMapper.get(source.getEntity());
		data.setCost(0);
		data.setDelay(0);
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isComplete() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean isValid() {
		return true;
	}
}

class CBFIterator implements Iterable<Edge>, Iterator<Edge> {
	private EdgePath point;

	CBFIterator(EdgeData point) {
		this.point = point.getPathSoFar();
	}

	CBFIterator(EdgePath point) {
		this.point = point;
	}

	CBFIterator() {
		this.point = null;
	}

	@Override
	public boolean hasNext() {
		return point != null && point.getEdge() != null;
	}

	@Override
	public Edge next() {
		Edge edge = point.getEdge();
		point = point.getPreviousEdges();
		return edge;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new CBFIterator(point);
	}
}
