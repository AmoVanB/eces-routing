package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.distancevector.DistanceVectorSystem;
import de.tum.ei.lkn.eces.routing.easygraph.MetricsMapper;
import de.tum.ei.lkn.eces.routing.mappers.PathMapper;
import de.tum.ei.lkn.eces.routing.mappers.UnicastRequestMapper;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponentMapper;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

import java.util.LinkedList;

import static org.junit.Assert.assertFalse;

/**
 * Base test class for each algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class BaseTest {
	protected Controller controller;
	protected GraphSystem graphSystem;
	protected DistanceVectorSystem distanceVectorSystem;
	protected Node[] nodes;
	protected Edge[] edges;
	protected Mapper<DummyComponent> dummyMapper;
	protected Mapper<Path> edgePathMapper;
	protected Mapper<UnicastRequest> requestMapper;
	protected MetricsMapper metricsMapper;

	public RoutingAlgorithm routingAlgorithmUnderTest;
	public PathPlumberProxy proxy;

	public void setUp() {
		controller = new Controller();
		graphSystem = new GraphSystem(controller);
		distanceVectorSystem = new DistanceVectorSystem(controller);
		dummyMapper = new DummyComponentMapper(controller);
		edgePathMapper = new PathMapper(controller);
		requestMapper = new UnicastRequestMapper(controller);
		metricsMapper = new MetricsMapper(controller);

	}

	protected void createBaseTopology() {
		Graph graph = graphSystem.createGraph();

		nodes = new Node[8];
		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = graphSystem.createNode(graph, "N" + i);
		}

		edges = new Edge[10];
		edges[0] =  graphSystem.createEdge(nodes[0], nodes[1]);
		edges[1] =  graphSystem.createEdge(nodes[1], nodes[2]);
		edges[2] =  graphSystem.createEdge(nodes[2], nodes[3]);
		edges[3] =  graphSystem.createEdge(nodes[3], nodes[0]);
		edges[4] =  graphSystem.createEdge(nodes[4], nodes[5]);
		edges[5] =  graphSystem.createEdge(nodes[5], nodes[6]);
		edges[6] =  graphSystem.createEdge(nodes[6], nodes[7]);
		edges[7] =  graphSystem.createEdge(nodes[7], nodes[4]);
		edges[8] =  graphSystem.createEdge(nodes[0], nodes[4]);
		edges[9] =  graphSystem.createEdge(nodes[6], nodes[2]);

		for(Edge edge : edges)
			dummyMapper.attachComponent(edge, new DummyComponent());

		distanceVectorSystem.update(graph);
	}

	protected void createLineTopology(int m) {
		Graph graph = graphSystem.createGraph();
		this.nodes = new Node[m];

		for(int i = 0; i < m; i++){
			nodes[i] = graphSystem.createNode(graph, "N-" + i);
		}
		LinkedList<Edge> edgeList = new LinkedList<>();
		for(int i = 0; i < m - 1; i++){
			edgeList.add(graphSystem.createEdge(nodes[i + 1], nodes[i]));
		}
		for(int i = 1; i < m ; i++){
			edgeList.add(graphSystem.createEdge(nodes[i - 1], nodes[i]));
		}
		edges = edgeList.toArray(new Edge[edgeList.size()]);
		for(Edge edge : edges)
			dummyMapper.attachComponent(edge, new DummyComponent());

		distanceVectorSystem.update(graph);
	}

	protected void createGridTopology(){
		createGridTopology(5, 5);
	}

	protected void createGridTopology(int n, int m) {
		Graph graph = graphSystem.createGraph();

		LinkedList<Node> nodeList = new LinkedList<>();
		Node grid[][] = new Node[n][m];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				grid[i][j] = graphSystem.createNode(graph,graph.getId() +": " + i +"-" +j);
				nodeList.add(grid[i][j]);
			}
		}
		nodes = nodeList.toArray(new Node[nodeList.size()]);

		LinkedList<Edge> edgeList = new LinkedList<>();
		int z = 0;
		for(int i = 0; i < n; i++){
			for(int j = 1; j < m; j++){
				edgeList.add(graphSystem.createEdge(grid[i][j-1], grid[i][j]));
				edgeList.add(graphSystem.createEdge(grid[i][j], grid[i][j-1]));
			}
		}

		for(int i = 1; i < n; i++){
			for(int j = 0; j < m; j++){
				edgeList.add(graphSystem.createEdge(grid[i-1][j], grid[i][j]));
				edgeList.add(graphSystem.createEdge(grid[i][j], grid[i-1][j]));
			}
		}
		edges = edgeList.toArray(new Edge[edgeList.size()]);
		for(Edge edge : edges)
			dummyMapper.attachComponent(edge, new DummyComponent());

		distanceVectorSystem.update(graph);
	}

	protected RoutingAlgorithm getBestAlgorithm(UnicastRequest request, ProxyTypes proxyTypes) {
		UniversalPriorityQueueAlgorithm ba = new UniversalPriorityQueueAlgorithm(controller, QueueMode.PATH_LOOP_DETECTION,true,true);
		ba.enableConstraintPruning();
		ba.setDebugMode();
		ba.setProxy(proxy);
		return ba;
	}

	protected void createPreviousEdgeTopology() {
		Graph graph = graphSystem.createGraph();

		nodes = new Node[7];
		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = graphSystem.createNode(graph, "N" + String.valueOf(i));
		}

		edges = new Edge[8];
		edges[0] =  graphSystem.createEdge(nodes[0], nodes[1]);
		edges[1] =  graphSystem.createEdge(nodes[1], nodes[3]);
		edges[2] =  graphSystem.createEdge(nodes[0], nodes[2]);
		edges[3] =  graphSystem.createEdge(nodes[2], nodes[3]);
		edges[4] =  graphSystem.createEdge(nodes[3], nodes[4]);
		edges[5] =  graphSystem.createEdge(nodes[3], nodes[5]);
		edges[6] =  graphSystem.createEdge(nodes[4], nodes[6]);
		edges[7] =  graphSystem.createEdge(nodes[5], nodes[6]);

		dummyMapper.attachComponent(edges[0], new DummyComponent(1,0,0,true,0));
		dummyMapper.attachComponent(edges[1], new DummyComponent(1,0,0,true,0));
		dummyMapper.attachComponent(edges[2], new DummyComponent(5,0,0,true,0));
		dummyMapper.attachComponent(edges[3], new DummyComponent(5,0,0,true,0));
		dummyMapper.attachComponent(edges[4], new DummyComponent(10,0,0,true,0));
		dummyMapper.attachComponent(edges[5], new DummyComponent(1,0,0,true,0));
		dummyMapper.attachComponent(edges[6], new DummyComponent(10,0,0,true,0));
		dummyMapper.attachComponent(edges[7], new DummyComponent(1,0,0,true,0));
	}

	final public void pathCheck(Path path, UnicastRequest request) {
		if(path == null)
			return;
		LinkedList<Edge> pathIterator = new LinkedList<>();
		double[] parameter = null;
		double cost = 0;

		double[] bounds = this.proxy.getConstraintsBounds(request);
		double[] constr = new double[bounds.length];
		assertFalse(routingAlgorithmUnderTest +": Source and destination do not fit to path", path.getPath()[0].getSource() != request.getSource() &&
					path.getPath()[path.getPath().length -1].getDestination() != request.getDestination());
		Edge oldEdge = null;
		for(Edge edge : path.getPath()) {
			assertFalse(routingAlgorithmUnderTest + ": path is not connected", oldEdge != null && oldEdge.getDestination() != edge.getSource());
		}

		for(Edge edge : path) {
			parameter = this.proxy.getNewParameters(pathIterator, edge, parameter, request, true);
			assertFalse(routingAlgorithmUnderTest + ": edge access on path denied", !this.proxy.hasAccess(pathIterator, edge, parameter, request, true));
			cost += this.proxy.getCost(pathIterator, edge, parameter, request, true);
			double newConstraints[] = this.proxy.getConstraintsValues(pathIterator, edge, parameter, request, true);
			for(int i = 0; i < constr.length; i++)
				constr[i] += newConstraints[i];
			oldEdge = edge;
			pathIterator.addFirst(edge);
		}

		for(int i = 0; i < constr.length; i++) {
			assertFalse(routingAlgorithmUnderTest +": Constraint violated " + i+": "+ constr[i]+ " "+  bounds[i], Proxy.violatesBound(constr[i],bounds[i]));
			assertFalse(routingAlgorithmUnderTest +": Constraint wrong " + i+": "+ constr[i]+ " "+   path.getConstraintsValues()[i],Math.abs(constr[i] - path.getConstraintsValues()[i]) > 0.0000000001);
		}
		assertFalse(routingAlgorithmUnderTest +": Cost wrong : should be "+ cost+ " but is "+   path.getCost(),Math.abs(cost - path.getCost()) > 0.0000000001);
		for(int i = 0; i < parameter.length; i++)
			assertFalse(routingAlgorithmUnderTest +": Parameter wrong " + i+": "+ parameter[i] + " "+   path.getParametersValues()[i],Math.abs(parameter[i] - path.getParametersValues()[i]) > 0.0000000001);
	}
}
