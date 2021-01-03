package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.UnableToHandleRequestException;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.OneToNAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.*;

/**
 * Universal priority queue algorithm.
 * 
 * Just has a priority queue, pops out data, relaxes and prunes invalid paths.
 * Can be heavily configured to create Dijkstra, A*, k-SP algorithms, etc.
 *
 * This is right now not very well documented as it encompasses a lot of features so that our priority-queue-based
 * algorithms can all run on the same code basis. However, technically, it's not necessary to understand how this class
 * works, Dijkstra and A* are just deriving from it.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class UniversalPriorityQueueAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest, OneToNAlgorithm, NToOneAlgorithm, BD {
	// Whether paths should be pruned based on their constraints values.
	private boolean pruneBasedOnConstraints = false;
	// Whether paths should be pruned based on their cost.
	private boolean pruneBasedOnCost = false;
	// Cost border for pruning.
	private double costBorder;
	private boolean stopOnFirstKPath = false;
	private boolean autoMode;
	protected QueueMode queueMode;
	final protected boolean guessMode;
	protected boolean isForward;
	private ProxyTypes maximumProxy = ProxyTypes.PATH_PROXY;
	protected LocalMapper<Record> recordLocalMapper;
	protected int k = 1;
	private TempData bestResult = null;

	public UniversalPriorityQueueAlgorithm(Controller controller, QueueMode queueMode, boolean isForward, boolean guessMode, int k) {
		super(controller);
		this.queueMode = queueMode;
		this.isForward = isForward;
		this.guessMode = guessMode;
		recordLocalMapper = controller.getLocalMapper(this, RecordLocal.class);
		if(queueMode == QueueMode.AUTO)
			autoMode = true;
		else
			autoMode = false;
		this.k = k;
	}

	public UniversalPriorityQueueAlgorithm(Controller controller, QueueMode queueMode, boolean isForward, boolean guessMode) {
		super(controller);
		this.queueMode = queueMode;
		this.isForward = isForward;
		this.guessMode = guessMode;
		recordLocalMapper = controller.getLocalMapper(this, RecordLocal.class);
		if(queueMode == QueueMode.AUTO)
			autoMode = true;
		else
			autoMode = false;
	}

	public UniversalPriorityQueueAlgorithm(Controller controller, boolean isForward, boolean guessMode) {
		this(controller, QueueMode.NODE,isForward ,guessMode );
	}

	public UniversalPriorityQueueAlgorithm(Controller controller) {
		this(controller, QueueMode.NODE,true ,false );
	}

	public UniversalPriorityQueueAlgorithm(Controller controller, QueueMode queueMode, boolean isForward) {
		this(controller, queueMode,isForward ,false );
	}

	public UniversalPriorityQueueAlgorithm(Controller controller, QueueMode queueMode) {
		this(controller, queueMode,true ,false );
	}

	public void setMaximumProxy(ProxyTypes maximumProxy) {
		this.maximumProxy = maximumProxy;
		this.autoMode = true;
	}

	protected void initRecord(Record record) {
		TempData[] data = new TempData[k];
		record.setArray(data);
	}

	protected void initDataStructure(UnicastRequest request) {
		Graph graph = null;
		if(request.getSource() != null)
			graph = request.getSource().getGraph();
		else if(request.getDestination() != null)
			graph = request.getDestination().getGraph();
		switch (queueMode) {
			case EDGE:
				for (Edge edge : graph.getEdges()) {
					Record record = recordLocalMapper.get(edge.getEntity());
					if(!record.hasArray()) {
						initRecord(record);
					}
					record.init();
				}
			case PATH_LOOP_DETECTION:
			case PATH:
			case NODE:
				for (Node node :  graph.getNodes()) {
					Record record = recordLocalMapper.get(node.getEntity());
					if(!record.hasArray()) {
						initRecord(record);
					}
					record.init();
				}
		}
	}

	protected void initDataStructure(Node root, UnicastRequest request) {
		initDataStructure(request);
		TempIterablePath dummy = new TempIterablePath(null,null);
		double[] parameters = new double[proxy.getNumberOfParameters(request)];
		double[] constraints = new double[proxy.getNumberOfConstraints(request)];
		Arrays.fill(parameters, 0);
		Arrays.fill(constraints, 0);
		Record record = recordLocalMapper.get(root.getEntity());
		TempData[] rootData = record.getArray();
		for(int i = 0; i < rootData.length; i++) {
			rootData[i] = getNewTempData();
			rootData[i].setConstraint(constraints);
			rootData[i].setParameter(parameters);
			rootData[i].setCost(0);
			rootData[i].setSqnum(0);
			rootData[i].setPath(dummy);
		}
	}

	public void run(Node source, Node destination, UnicastRequest request, Iterable<Edge> path) {
		if(source == destination && !path.iterator().hasNext()) {
			Record newData = null;
			TempIterablePath dummy = new TempIterablePath(null,null);
			newData = recordLocalMapper.get(source.getEntity());
			int index = newData.getMaxIDnotVisited();
			newData.getArray()[index] = getNewTempData();
			newData.getTempData(index).setConstraint(new double[proxy.getNumberOfConstraints(request)]);
			newData.getTempData(index).setCost(0.0);
			newData.getTempData(index).setParameter(new double[proxy.getNumberOfParameters(request)]);
			newData.getTempData(index).setPath(dummy);
			bestResult = newData.getTempData(index);
			return;
		}

		if(autoMode)
			autoConfigure(request);
		else if(!this.handle(request))
			throw new UnableToHandleRequestException("");

		PriorityQueue<TempData> pq;

		Iterator<Edge> pathInit = path.iterator();
		if(!pathInit.hasNext()) {
			initDataStructure(source, request);
			pq = getPriorityQueue();
			populatePriorityQueue(pq, source, request);
		}
		else {
			initDataStructure(request);
			pq = getPriorityQueue();
			TempIterablePath data = null;
			Edge edge = null;
			if(isForward()) {
				while (pathInit.hasNext()) {
					edge = pathInit.next();
					if(pathInit.hasNext())
						data = new TempIterablePath(data, edge);
				}
			}

			TempData queueData = this.getNewTempData();
			queueData.setSqnum(0);
			if(data == null) {
				TempIterablePath dummy = new TempIterablePath(null,null);
				queueData.init();
				queueData.setCost(0.0);
				queueData.setConstraint(new double[proxy.getConstraintsBounds(request).length]);
				queueData.setPath(dummy);

			} else {
				queueData.setPath(data);
				Path subPath = proxy.createPath(data,request,false);
				queueData.setParameter(subPath.getParametersValues());
				queueData.setCost(subPath.getCost());
				queueData.setConstraint(subPath.getConstraintsValues());
			}

			TempData newData = relax(edge, queueData, request, 0, true);
			if(newData != null) {
				pq.add(newData);
			}
		}

		int sqnum = 0;
		int pathCount = 0;
		bestResult = null;

		int i = 0;
		while(!pq.isEmpty()) {
			i++;
			TempData data = pq.poll();
			if(!data.isVisited()) {
				if(destination != null && isDestination(data, destination)) {
					Record newData = null;
					if(isForward())
						newData = recordLocalMapper.get(data.getPath().getEdge().getDestination().getEntity());
					else
						newData = recordLocalMapper.get(data.getPath().getEdge().getSource().getEntity());
					if(queueMode != QueueMode.NODE) {
						int index = newData.getMaxIDnotVisited();
						newData.getTempData(index).setConstraint(data.getConstraint());
						newData.getTempData(index).setCost(data.getCost());
						newData.getTempData(index).setParameter(data.getParameter());
						newData.getTempData(index).setPath(data.getPath());
					}
					if(pathCount == 0)
						bestResult = data;
					pathCount++;
					if(pathCount == k || stopOnFirstKPath) {
						processResults(newData);
						break;
					}
				} else {
					for (Edge nextEdge : getEdgeList(data)) {
						TempData newData = relax(nextEdge, data, request, sqnum);
						if (newData != null) {
							pq.add(newData);
							sqnum++;
						}
					}
				}
				data.setVisited(true);
			}
		}
	}

	public void enableStopOnFirstKPath() {
		stopOnFirstKPath = true;
	}
	public void disableStopOnFirstKPath() {
		stopOnFirstKPath = false;
	}

	protected void autoConfigure(UnicastRequest request) {
		if(proxy.getNumberOfConstraints(request) > 0) {
			queueMode = QueueMode.PATH_LOOP_DETECTION;
			return;
		}
		switch (maximumProxy) {
			case EDGE_PROXY:
				queueMode = QueueMode.NODE;
				break;
			case PREVIOUS_EDGE_PROXY:
				switch (proxy.getProxy().getType()) {
					case EDGE_PROXY:
						queueMode = QueueMode.NODE;
						break;
					case PREVIOUS_EDGE_PROXY:
					case PATH_PROXY:
						queueMode = QueueMode.EDGE;
						break;
				}
				break;
			case PATH_PROXY:
				switch (proxy.getProxy().getType()) {
					case EDGE_PROXY:
						queueMode = QueueMode.NODE;
						break;
					case PREVIOUS_EDGE_PROXY:
						queueMode = QueueMode.EDGE;
						break;
					case PATH_PROXY:
						queueMode = QueueMode.PATH_LOOP_DETECTION;
						break;
				}
				break;
		}

	}

	protected TempData relax(Edge nextEdge, TempData data, UnicastRequest request, int sqnum) {
		return relax(nextEdge, data, request, sqnum, false);
	}


	protected TempData relax(Edge nextEdge, TempData data, UnicastRequest request, int sqnum, boolean noACCheck) {
		double[] parameters = proxy.getNewParameters(data.getPath(), nextEdge, data.getParameter(), request, this.isForward());
		double[] constraints;
		if((proxy.hasAccess(data.getPath(),nextEdge, parameters, request, this.isForward()) || noACCheck) &&
				(constraints = relaxPruneOnConstraints(nextEdge, data, request, parameters)) != null &&
				!relaxPruneOnCost(nextEdge, data, request, parameters, constraints)) {
			TempData tempData = null;
			if(k > 1 && relaxPathLoops(nextEdge, data, request, parameters, constraints) == null)
				return null;

			switch (queueMode) {
				case NODE:
					tempData = relaxNode(nextEdge, data, request, parameters, constraints);
					break;
				case EDGE:
					tempData = relaxEdge(nextEdge, data, request, parameters, constraints);
					break;
				case PATH_LOOP_DETECTION:
					tempData = relaxPathLoops(nextEdge, data, request, parameters, constraints);
					break;
				case PATH:
					tempData = relaxPath(nextEdge, data, request, parameters, constraints);
					break;
			}

			if(tempData == null)
				return null;

			TempIterablePath newPath = new TempIterablePath(data.getPath(), nextEdge);
			if(queueMode != QueueMode.NODE) {
				TempData nodeData = relaxNode(nextEdge, data, request, parameters, constraints);
				if(nodeData != null) {
					nodeData.setPath(newPath);
					nodeData.setParameter(parameters);
					nodeData.setConstraint(constraints);
				}
			}

			tempData.setPath(newPath);
			tempData.setParameter(parameters);
			tempData.setSqnum(sqnum);
			tempData.setConstraint(constraints);

			relaxSetGuess(nextEdge, tempData, request, parameters, constraints);

			return tempData;
		}
		return null;
	}

	protected double[] relaxPruneOnConstraints(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters) {
		double[] newConstraints = proxy.getConstraintsValues(data.getPath(), nextEdge, parameters, request, isForward());
		for (int i = 0; i < newConstraints.length; i++)
			newConstraints[i] += data.getConstraint()[i];
		if (this.pruneBasedOnConstraints) {
			double[] constraints = proxy.getConstraintsBounds(request);
			if ((constraints != null || constraints.length != 0)) {
				double[] newConstraintsWithGuess = new double[constraints.length];
				for (int i = 0; i < constraints.length; i++) {
					if (isForward())
						newConstraintsWithGuess[i] = newConstraints[i] + proxy.getGuessForConstraint(i, nextEdge.getDestination(), request.getDestination());
					else
						newConstraintsWithGuess[i] = newConstraints[i] + proxy.getGuessForConstraint(i, nextEdge.getSource(), request.getSource());
				}
				if (Proxy.violatesBound(newConstraintsWithGuess, constraints))
					return null;
			}
		}
		return newConstraints;
	}
	protected TempData relaxNode(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		double newCost = computeCost(nextEdge, data, request, parameters, newConstraints);
		Record newData = null;
		if(isForward())
			newData = recordLocalMapper.get(nextEdge.getDestination().getEntity());
		else
			newData = recordLocalMapper.get(nextEdge.getSource().getEntity());
		int index = newData.getMaxIDnotVisited();
		if(index == -1)
			return null;
		TempData tempData = newData.getTempData(index);
		if(tempData != null) {
			if(newCost >= tempData.getCost())
				return null;
			tempData.setVisited(true);
		}
		tempData = getNewTempData();
		newData.getArray()[index] = tempData;
		tempData.setCost(newCost);
		return tempData;
	}

	protected TempData relaxEdge(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		double newCost = computeCost(nextEdge, data, request, parameters, newConstraints);
		Record newData = recordLocalMapper.get(nextEdge.getEntity());
		int index = newData.getMaxIDnotVisited();
		if(index == -1)
			return null;
		TempData tempData = newData.getTempData(index);
		if(tempData != null) {
			if(newCost >= tempData.getCost())
				return null;
			tempData.setVisited(true);
		}
		tempData = getNewTempData();
		newData.getArray()[index] = tempData;
		tempData.setCost(newCost);
		return tempData;
	}

	protected TempData relaxPath(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		TempData tempData = this.getNewTempData();
		tempData.setCost(computeCost(nextEdge, data, request, parameters, newConstraints));
		return tempData;
	}

	protected TempData relaxPathLoops(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		TempData tempData = relaxPath( nextEdge, data, request, parameters, newConstraints);
		Node newNode = null;
		if(data.getPath() != null) {
			if(isForward()) {
				newNode = nextEdge.getDestination();

			} else {
				newNode = nextEdge.getSource();
			}
			for(Edge segment : data.getPath()) {
				Node pathNode;
				if(isForward()) {
					pathNode = segment.getSource();

				} else {
					pathNode = segment.getDestination();
				}
				if(pathNode == newNode)
					return null;
			}
		}
		return tempData;
	}

	protected boolean relaxPruneOnCost(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		if(this.pruneBasedOnCost) {
			double pcost;
			if (isForward()) {
				pcost = computeCost(nextEdge, data, request, parameters, newConstraints);
				if(guessMode)
					pcost += this.proxy.getGuessForCost(nextEdge.getDestination(), request.getDestination());
			}
			else {
				pcost = computeCost(nextEdge, data, request, parameters, newConstraints);
				if(guessMode)
					pcost += this.proxy.getGuessForCost(nextEdge.getSource(), request.getSource());
			}

			if(Proxy.violatesBound(pcost, costBorder))
				return true;
		}
		return false;
	}

	protected void relaxSetGuess(Edge nextEdge, TempData newTempData, UnicastRequest request, double[] parameters, double[] newConstraints) {
		if(guessMode) {
			TempIterablePath newPath = newTempData.getPath();
			if (isForward())
				((TempDataGuess) newTempData).setGuess(proxy.getGuessForCost(newPath.getEdge().getDestination(), request.getDestination()));
			else
				((TempDataGuess) newTempData).setGuess(proxy.getGuessForCost(newPath.getEdge().getSource(), request.getSource()));
		}
	}

	protected double computeCost(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		return data.getCost() + proxy.getCost(data.getPath(), nextEdge, parameters, request, this.isForward());
	}

	private boolean isDestination(TempData data, Node destination) {
		if(data.getPath() != null && data.getPath().getEdge() != null) {
			if(isForward())
				return destination == data.getPath().getEdge().getDestination();
			return destination == data.getPath().getEdge().getSource();
		}
		return false;
	}

	private void populatePriorityQueue(PriorityQueue<TempData> pq , Node root, UnicastRequest request) {
		TempIterablePath dummy = new TempIterablePath(null,null);
		TempData data = getNewTempData();
		data.init();
		data.setCost(0.0);
		data.setConstraint(new double[proxy.getConstraintsBounds(request).length]);
		data.setPath(dummy);
		for(Edge edge: getEdgeList(root)) {
			TempData newData = relax(edge, data, request, 0);
			if(newData != null) {
				pq.add(newData);
			}
		}
	}

	protected TempData getNewTempData() {
		if(guessMode) {
			return new TempDataGuess();
		} else {
			return new TempData();
		}
	}

	private List<Edge> getEdgeList(Node root) {
		if(isForward())
			return root.getOutgoingConnections();
		return  root.getIncomingConnections();
	}

	private List<Edge> getEdgeList(TempData data) {
		if(data.getPath() == null)
			return new LinkedList<>();
		if(isForward())
			return getEdgeList(data.getPath().getEdge().getDestination());
		return getEdgeList(data.getPath().getEdge().getSource());
	}

	@Override
	public Response solve(Request request, double costBorder) {
		this.setCostBorder(costBorder);
		Response response = this.solve(request);
		this.removeCostBorder();
		return response;
	}

	@Override
	public void setCostBorder(double costBorder) {
		this.costBorder = costBorder;
		this.pruneBasedOnCost = true;
	}

	@Override
	public void removeCostBorder() {
		this.pruneBasedOnCost = false;
	}

	public void enableConstraintPruning() {
		this.pruneBasedOnConstraints = true;
	}
	public void disableConstraintPruning() {
		this.pruneBasedOnConstraints = false;
	}
	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	public Path computePath(UnicastRequest request) {
		return computePath(request, new LinkedList<Edge>());
	}

	public Path computePath(UnicastRequest request, Iterable<Edge> path) {
		Record record;
		if(isForward()) {
			run(request.getSource(), request.getDestination(), request, path);
			record = recordLocalMapper.get(request.getDestination().getEntity());
		} else {
			run(request.getDestination(), request.getSource(), request, path);
			record = recordLocalMapper.get(request.getSource().getEntity());
		}

		TempData data = bestResult;
		if(data == null || data.getPath() == null) {
			return null;
		}
		return new Path(data.getPath(),
				isForward(),
				data.getCost(),
				data.getConstraint(),
				data.getParameter());

	}

	public QueueMode getQueueMode() {
		return queueMode;
	}

	public void setQueueMode(QueueMode queueMode) {
		this.queueMode = queueMode;
		this.autoMode = false;
	}

	@Override
	public boolean isForward() {
		return isForward;
	}

	@Override
	public boolean isOptimal() {
		if(autoMode) {
			switch (this.maximumProxy) {
				case EDGE_PROXY:
					if(this.getProxy().getType() == ProxyTypes.PREVIOUS_EDGE_PROXY)
						return false;
				case PREVIOUS_EDGE_PROXY:
					if(this.getProxy().getType() == ProxyTypes.PATH_PROXY)
						return false;
			}
		}
		else {
			switch (this.getProxy().getType()) {
				case PATH_PROXY:
					if(this.queueMode == QueueMode.EDGE)
						return false;
				case PREVIOUS_EDGE_PROXY:
					if(this.queueMode == QueueMode.NODE)
						return false;
			}
		}

		return true;
	}

	@Override
	public boolean isComplete() {
		return isOptimal();
	}

	@Override
	public boolean isValid() {
		return isOptimal();
	}

	@Override
	public boolean handle(Request request) {
		if(this.getProxy().handle(request, isForward())) {
			if(this.getProxy().getNumberOfConstraints(request) != 0 && !this.pruneBasedOnConstraints)
				return false;
			return true;
		}
		return false;
	}

	@Override
	public void computePathsFromAnyNodeTo(Node destination, Request request) {
		if(this.getProxy().handle(request, false)) {
			boolean save = isForward;
			isForward = false;
			run(destination, null, ((UnicastRequest)request), new LinkedList<>());
			isForward = save;
		}
	}

	@Override
	public Path getPathToNodeFrom(Node source) {
		return getPathX(source, false);
	}

	@Override
	public Path getPathFromNodeTo(Node destination) {
		return getPathX(destination, true);
	}

	private Path getPathX(Node source, boolean direct) {
		Record record = recordLocalMapper.get(source.getEntity());
		int minID = record.getMinID();
		if(minID == -1)
			return null;
		TempData data = record.getTempData(minID);
		if(data != null)
			return new Path(data.getPath(),
					direct,
					data.getCost(),
					data.getConstraint(),
					data.getParameter());
		return null;
	}

	@Override
	public void computePathsToAnyNodeFrom(Node source, Request request) {
		if(this.getProxy().handle(request, true)) {
			boolean save = isForward;
			isForward = true;
			run(source, null, ((UnicastRequest)request),  new LinkedList<Edge>());
			isForward = save;
		}
	}

	public Set<Path> getkPathToNodeFrom(Node source) {
		return getkPathX(source,false);
	}

	public Set<Path> getkPathFromNodeTo(Node destination) {
		return getkPathX(destination,true);
	}

	private Set<Path> getkPathX(Node node, boolean direct) {
		Record record = recordLocalMapper.get(node.getEntity());
		SortedSet<Path> result = new TreeSet<>((o1, o2) -> {
			if (o2 == null)
				return -1;
			if (o1 == null)
				return 1;
			if (o1.getCost() > o2.getCost())
				return 1;
			if (o1.getCost() < o2.getCost())
				return -1;
			if(o1.hashCode() < o2.hashCode())
				return -1;
			if(o1.hashCode() > o2.hashCode())
				return 1;
			return 0;
		});
		for(int i = 0; i < k; i++) {
			TempData data = record.getTempData(i);
			if(data != null && data.getPath() != null )
				result.add(new Path(data.getPath(),
						direct,
						data.getCost(),
						data.getConstraint(),
						data.getParameter()));
		}
		return result;
	}

	public PriorityQueue<TempData> getPriorityQueue() {
		return  new PriorityQueue<>();
	}

	public void processResults(Record newData) {

	}

	public void computePathsFromAnyNodeTo(Node destination, Request request, double costBorder) {
		this.setCostBorder(costBorder);
		this.computePathsFromAnyNodeTo(destination, request);
		this.removeCostBorder();
	}

	public void computePathsToAnyNodeFrom(Node source, Request request, double costBorder) {
		this.setCostBorder(costBorder);
		this.computePathsToAnyNodeFrom(source, request);
		this.removeCostBorder();
	}
}
