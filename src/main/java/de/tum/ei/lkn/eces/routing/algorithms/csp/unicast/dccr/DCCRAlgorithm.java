package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dccr;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * 2001
 * "Search Space Reduction in QoS Routing"
 * Liang Guo and Ibrahim Matta.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCCRAlgorithm extends CSPAlgorithm implements SolveUnicastRequest, BDifiable {
	/**
	 * Default value for k.
	 */
	protected static final int DEFAULT_K = 5;

	/**
	 * Value of k used.
	 */
	protected int k;

	/**
	 * LocalMapper to access records.
	 */
	protected LocalMapper<NodeData> nodeDataLocalMapper;

	/**
	 * Underlying SPAlgorithm used.
	 */
	protected SPAlgorithm spAlgorithm;

	/**
	 * PlumberProxy given to the underlying algorithm.
	 */
	protected PathPlumberProxy plumberProxy;

	/**
	 * Whether or not BD is activated.
	 */
	private boolean BDfeature = false;

	public DCCRAlgorithm(Controller controller) {
		this(controller, DEFAULT_K);
	}

	public DCCRAlgorithm(Controller controller, ProxyTypes maxProxy) {
		this(controller, maxProxy, DEFAULT_K);
	}

	public DCCRAlgorithm(Controller controller, int k) {
		this(controller, ProxyTypes.EDGE_PROXY, k);
	}

	public DCCRAlgorithm(Controller controller, ProxyTypes maxProxy, int k) {
		this(controller, new AStarAlgorithm(controller), k);
		((AStarAlgorithm) spAlgorithm).setMaximumProxy(maxProxy);
	}

	public DCCRAlgorithm(Controller controller, SPAlgorithm spAlgorithm) {
		this(controller, spAlgorithm, DEFAULT_K);
	}

	public DCCRAlgorithm(Controller controller, SPAlgorithm spAlgorithm, int k) {
		super(controller);
		this.k = k;
		this.nodeDataLocalMapper = controller.getLocalMapper(this, NodeDataLocal.class);
		this.spAlgorithm = spAlgorithm;
		// Delay as optimization metric and cost as parameter.
		plumberProxy = new PathPlumberProxy(new int[]{1}, new double[]{1}, new int[0], new int[]{0});
		this.spAlgorithm.setProxy(plumberProxy);
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

	protected void setProxy() {
		this.plumberProxy.setProxy(proxy);
	}

	private void initializeSingleSource(UnicastRequest request) {
		for(Node node : request.getSource().getGraph().getNodes()) {
			NodeData data = nodeDataLocalMapper.get(node.getEntity());
			data.init();
			data.records = new Record[k];
			for(int i = 0; i < k; i++)
				data.records[i] = new Record();
		}

		NodeData data = nodeDataLocalMapper.get(request.getSource().getEntity());
		data.records[0].setWeight(0);
		data.records[0].setDelay(0);
		data.records[0].setCost(0);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	protected Path computePath(UnicastRequest request) {
		int costID = this.plumberProxy.getPlumberParameterId(0, request);
		double deadline = Double.MAX_VALUE;
		if(proxy.getNumberOfConstraints(request) > 0)
			deadline = proxy.getConstraintsBounds(request)[0];

		Path LDPath;
		if(BDfeature)
			LDPath = (Path) ((BD) spAlgorithm).solve(request, deadline);
		else
			LDPath = (Path) spAlgorithm.solve(request);
		if(LDPath == null || Proxy.violatesBound(LDPath.getCost(), deadline))
			return null;

		double costBound = LDPath.getParametersValues()[costID];
		Path bestPath = proxy.createPath(LDPath, request, isForward());
		return DCCRRouting(request, deadline, costBound, bestPath);
	}

	protected Path DCCRRouting(UnicastRequest request, double deadline, double costBound, Path bestPath) {
		// 2.
		double cBest = bestPath.getCost();
		int pathCount = 0;

		// 3.
		initializeSingleSource(request);

		// 4.
		PriorityQueue<HeapItem> pq = new PriorityQueue<>();
		pq.add(new HeapItem(request.getSource(), 0, 0));
		int sqnum = 1;
		// 5.
		while(!pq.isEmpty()) {
			// 6.
			HeapItem heapItem = pq.poll();

			// 7.
			NodeData data = nodeDataLocalMapper.get(heapItem.getNode().getEntity());
			Record record = data.records[heapItem.getId()];
			record.setVisited();
			Iterable<Edge> path = new DCCRRecordIterator(record);

			// 8.
			if(heapItem.getNode() == request.getDestination()) {
				// 9.
				LinkedList<Edge> list = new LinkedList<>();
				for(Edge edge : path) {
					list.addFirst(edge);
					if(edge.getSource() == request.getSource())
						break;
				}
				Path newPath = proxy.createPath(list, request, this.isForward());

				// 10.
				pathCount++;

				// 11.
				if(newPath.getCost() < cBest) {
					// 12.
					bestPath = newPath;
					cBest = newPath.getCost();
				}

				// 13.
				if(pathCount == k)
					// 14.
					return bestPath;
			}

			// 15.
			for(Edge edge : heapItem.getNode().getOutgoingConnections()) {
				// Preliminary check not to use forbidden Edges.
				double[] newParameters = proxy.getNewParameters(path, edge, record.getParameters(), request, isForward());
				if(!proxy.hasAccess(path, edge, newParameters,request, isForward()))
					continue;

				// 1. of ComputeWeight()
				double newCost = proxy.getCost(path, edge, newParameters, request, isForward()) + record.getCost();
				double newDelay = 0;
				if(proxy.getNumberOfConstraints(request) > 0)
					newDelay = proxy.getConstraintsValues(path, edge, newParameters, request, isForward())[0] + record.getDelay();

				// 2. of ComputeWeight()
				double newWeight = Double.MAX_VALUE;
				if(!Proxy.violatesBound(newDelay, deadline) && newCost <= costBound)
					newWeight = newDelay /(1 - (newCost/costBound));

				// 17.
				NodeData nextNode = nodeDataLocalMapper.get(edge.getDestination().getEntity());
				Record maxRecord = new Record();
				maxRecord.setWeight(0);
				int index = 0;
				for(int i = 0; i < k; i++) {
					if((nextNode.records[i].getWeight() > maxRecord.getWeight()) && !nextNode.records[i].isVisited()) {
						maxRecord = nextNode.records[i];
						index = i;
					}
				}

				// 18.
				if(newWeight < maxRecord.getWeight() && (!Proxy.violatesBound(newDelay, maxRecord.getDelay()) || newCost <= maxRecord.getCost())) {
					// 19.
					nextNode.records[index].setDelay(newDelay);
					nextNode.records[index].setCost(newCost);
					nextNode.records[index].setWeight(newWeight);
					nextNode.records[index].setPreviousEdge(edge);
					nextNode.records[index].setParameters(newParameters);
					nextNode.records[index].setPreviousRecord(record);
					nextNode.records[index].setUnvisited();

					// 20. - 23.
					HeapItem newHeapItem = new HeapItem(edge.getDestination(), newWeight, index);
					newHeapItem.setSqnum(sqnum);
					pq.remove(newHeapItem);
					pq.add(newHeapItem);
					sqnum++;
				}
			}
		}

		return bestPath;
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
		return spAlgorithm.isComplete();
	}

	@Override
	public boolean isValid() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public void enableBD() {
		BDfeature = true;
	}

	@Override
	public void disableBD() {
		BDfeature = false;
	}
}

class DCCRRecordIterator implements Iterable<Edge>, Iterator<Edge> {
	private Record currentRecord;

	public DCCRRecordIterator(Record record) {
		this.currentRecord = record;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new DCCRRecordIterator(currentRecord);
	}

	@Override
	public boolean hasNext() {
		return currentRecord.getPreviousRecord() != null;
	}

	@Override
	public Edge next() {
		Edge edge = currentRecord.getPreviousEdge();
		currentRecord = currentRecord.getPreviousRecord();
		return edge;
	}
}
