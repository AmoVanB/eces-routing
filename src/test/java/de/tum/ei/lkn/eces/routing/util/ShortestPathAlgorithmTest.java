package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.easygraph.EasyGraphProxy;
import de.tum.ei.lkn.eces.routing.easygraph.Metrics;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.mocks.DummyPreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.mocks.GridPathProxy;
import de.tum.ei.lkn.eces.routing.mocks.GridPreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Base test class for SP algorithms algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class ShortestPathAlgorithmTest extends BaseTest {
	@Test
	public final void verySimpleShortestPathTest() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		UnicastRequest request = new UnicastRequest(nodes[1], nodes[2]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);
		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 1);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[1]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 2 but is " + path.getCost(), path.getCost() == 1);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public final void simpleShortestPathTest() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[2]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);
		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 2);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[0]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[1]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 2 but is " + path.getCost(), path.getCost() == 2);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public final void shortestPathTestWithBlockedLink() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		dummyMapper.get(edges[1].getEntity()).use = false;

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[2]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 4);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[8]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[4]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[3] == edges[9]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 4 but is " + path.getCost(), path.getCost() == 4);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public final void parameterSummation() {
		createBaseTopology();
		DummyEdgeProxy dummyEdgeProxy = new DummyEdgeProxy(controller);
		proxy.setProxy(dummyEdgeProxy);

		dummyMapper.get(edges[1].getEntity()).use = false;

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[2]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 4);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[8]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[4]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[3] == edges[9]);
		assertTrue(routingAlgorithmUnderTest + ": parameter[0] should be 4 but is " + path.getParametersValues()[0], path.getParametersValues()[0] == 4);
		assertTrue(routingAlgorithmUnderTest + ": parameter[1] should be 8 but is " + path.getParametersValues()[1], path.getParametersValues()[1] == 8);
		assertTrue(routingAlgorithmUnderTest + ": parameter[2] should be -4 but is " + path.getParametersValues()[2], path.getParametersValues()[2] == -4);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public final void testPreviousEdgeAccess() {
		DummyPreviousEdgeProxy dummyEdgeProxy = new DummyPreviousEdgeProxy(controller);
		proxy.setProxy(dummyEdgeProxy);

		assumeTrue("Algorithm could not solve previous edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

		createPreviousEdgeTopology();
		dummyEdgeProxy.setAccessSwitch(edges[1],edges[5]);

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[6]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 4);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[2]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[3]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[3] == edges[7]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 10 but is " + path.getCost(), path.getCost() == 12);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public final void testPreviousEdgeCost() {
		DummyPreviousEdgeProxy dummyEdgeProxy = new DummyPreviousEdgeProxy(controller);
		proxy.setProxy(dummyEdgeProxy);

		assumeTrue("Algorithm could not solve previous edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

		createPreviousEdgeTopology();
		dummyEdgeProxy.setCostSwitch(edges[1],edges[5]);
		UnicastRequest request = new UnicastRequest(nodes[0], nodes[6]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 4);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[2]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[3]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[3] == edges[7]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 10 but is " + path.getCost(), path.getCost() == 12);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
	}

	@Test
	public void checkEdgeProxyPathValidOptimalityConsistency() {
		this.proxy.setProxy(new DummyEdgeProxy(controller));
		routingAlgorithmUnderTest.setDebugMode();
		RoutingAlgorithm bestRoutingAlgorithm = getBestAlgorithm(new UnicastRequest(null,null), ProxyTypes.EDGE_PROXY);
		createGridTopology();

		for(Node src: nodes){
			for(Node dest: nodes){
				if(src != dest){
					UnicastRequest request = new UnicastRequest(src,dest);
					Path path = (Path) routingAlgorithmUnderTest.solve(request);
					Path copath = (Path) bestRoutingAlgorithm.solve(request);
					if(path == null && copath == null)
						continue;
					if(routingAlgorithmUnderTest.isValid())
						this.pathCheck(path,request);
					if(routingAlgorithmUnderTest.isComplete()){
						if(copath != null && !proxy.isValid(copath,request))
							throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is invalid");
						if( path != null && copath == null)
							throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not complete");
						assertFalse(routingAlgorithmUnderTest + ": The algorithm ( tested by "+ bestRoutingAlgorithm+ " ) is not complete" , path == null && copath != null);
						if(routingAlgorithmUnderTest.isOptimal()){
							if(path.getCost() > copath.getCost())
								path = (Path) routingAlgorithmUnderTest.solve(request);
							if(path.getCost() < copath.getCost())
								throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not optimal");
							assertTrue(routingAlgorithmUnderTest + ": dit not find the optimal path aut path is "+ path.getCost() + " optimal path is " + copath.getCost(), Math.abs(path.getCost() - copath.getCost()) < 0.00000001 );
						}
					}
					if(path != null)
						proxy.getProxy().register(path,request);
				}
			}
		}
		assertTrue("Algorithm could not solve EdgeProxy valid",routingAlgorithmUnderTest.isValid());
	}

	@Test
	public void checkPreviousEdgeProxyPathValidOptimalityConsistency() {
		this.proxy.setProxy(new GridPreviousEdgeProxy(controller));
		routingAlgorithmUnderTest.setDebugMode();
		RoutingAlgorithm bestRoutingAlgorithm = getBestAlgorithm(new UnicastRequest(null,null), ProxyTypes.PREVIOUS_EDGE_PROXY);
		createGridTopology();

		for(Node src: nodes){
			for(Node dest: nodes){
				if(src != dest){
					UnicastRequest request = new UnicastRequest(src,dest);
					Path path = (Path) routingAlgorithmUnderTest.solve(request);
					Path copath = (Path) bestRoutingAlgorithm.solve(request);
					if(path == null && copath == null)
						continue;
					if(routingAlgorithmUnderTest.isValid())
						this.pathCheck(path,request);
					if(routingAlgorithmUnderTest.isComplete()){
						if(copath != null && !proxy.isValid(copath,request)) {
							proxy.isValid(copath,request);
							throw new RuntimeException("Best algorithm ( " + bestRoutingAlgorithm + " ) is invalid");
						}
						if( path != null && copath == null)
							throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not complete");
						assertFalse(routingAlgorithmUnderTest + ": The algorithm is not complete" , path == null && copath != null);
						if(routingAlgorithmUnderTest.isOptimal()){
							if(path.getCost() < copath.getCost())
								throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not optimal");
							assertTrue(routingAlgorithmUnderTest + ": dit not find the optimal path ", Math.abs(path.getCost() - copath.getCost()) < 0.00000001 );
						}
					}
					if(path != null)
						proxy.getProxy().register(path,request);
				}
			}
		}
		assumeTrue("Algorithm could not solve PreviousEdgeProxy valid --> Skip test",routingAlgorithmUnderTest.isValid());
	}

	@Test
	public void checkPathProxyPathValidOptimalityConsistency() {
		this.proxy.setProxy(new GridPathProxy(controller));
		routingAlgorithmUnderTest.setDebugMode();
		RoutingAlgorithm bestRoutingAlgorithm = getBestAlgorithm(new UnicastRequest(null,null), ProxyTypes.PATH_PROXY);
		createGridTopology();

		for(Node src: nodes){
			for(Node dest: nodes){
				if(src != dest){
					UnicastRequest request = new UnicastRequest(src,dest);
					Path path = (Path) routingAlgorithmUnderTest.solve(request);
					Path copath = (Path) bestRoutingAlgorithm.solve(request);
					if(path == null && copath == null)
						continue;
					if(routingAlgorithmUnderTest.isValid())
						this.pathCheck(path,request);
					if(routingAlgorithmUnderTest.isComplete()){
						if(copath != null && !proxy.isValid(copath,request))
							throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is invalid");
						if( path != null && copath == null)
							throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not complete");
						assertFalse(routingAlgorithmUnderTest + ": The algorithm is not complete" , path == null && copath != null);
						if(routingAlgorithmUnderTest.isOptimal()){
							if(path.getCost() < copath.getCost())
								throw new RuntimeException("Best algorithm ( "+ bestRoutingAlgorithm+ " ) is not optimal");
							assertTrue(routingAlgorithmUnderTest + ": dit not find the optimal path ", Math.abs(path.getCost() - copath.getCost()) < 0.00000001 );
						}
					}
					if(path != null)
						proxy.getProxy().register(path,request);
				}
			}
		}
		assumeTrue("Algorithm could not solve PathProxy valid --> Skip test",routingAlgorithmUnderTest.isValid());
	}

	@Test
	public final void figure1OfLARAC1980PaperSPOptimal() {
		EasyGraphProxy dp = new EasyGraphProxy(controller, new double[]{10, 10, 10});
		proxy.setProxy(dp);
		assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test", routingAlgorithmUnderTest.isOptimal());

		Graph graph = graphSystem.createGraph();
		Node n1  = graphSystem.createNode(graph);
		Node n2  = graphSystem.createNode(graph);
		Node n3  = graphSystem.createNode(graph);
		Node n4  = graphSystem.createNode(graph);
		Node n5  = graphSystem.createNode(graph);
		Node n6  = graphSystem.createNode(graph);
		Node n7  = graphSystem.createNode(graph);
		Node n8  = graphSystem.createNode(graph);
		Node n9  = graphSystem.createNode(graph);
		Node n10 = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge n1n2  = graphSystem.createEdge(n1, n2);
		metricsMapper.attachComponent(n1n2, new Metrics(1, new double[]{0, 0, 0}));
		Edge n1n3  = graphSystem.createEdge(n1, n3);
		metricsMapper.attachComponent(n1n3,  new Metrics(4, new double[]{0, 0, 0}));
		Edge n1n4  = graphSystem.createEdge(n1, n4);
		metricsMapper.attachComponent(n1n4,  new Metrics(7, new double[]{0, 0, 0}));
		Edge n1n8  = graphSystem.createEdge(n1, n8);
		metricsMapper.attachComponent(n1n8,  new Metrics(9, new double[]{0, 0, 0}));
		Edge n1n9  = graphSystem.createEdge(n1, n9);
		metricsMapper.attachComponent(n1n9,  new Metrics(3, new double[]{0, 0, 0}));
		Edge n2n5  = graphSystem.createEdge(n2, n5);
		metricsMapper.attachComponent(n2n5,  new Metrics(2, new double[]{0, 0, 0}));
		Edge n2n6  = graphSystem.createEdge(n2, n6);
		metricsMapper.attachComponent(n2n6,  new Metrics(2, new double[]{0, 0, 0}));
		Edge n2n8  = graphSystem.createEdge(n2, n8);
		metricsMapper.attachComponent(n2n8,  new Metrics(4, new double[]{0, 0, 0}));
		Edge n3n5  = graphSystem.createEdge(n3, n5);
		metricsMapper.attachComponent(n3n5,  new Metrics(3, new double[]{0, 0, 0}));
		Edge n3n6  = graphSystem.createEdge(n3, n6);
		metricsMapper.attachComponent(n3n6,  new Metrics(7, new double[]{0, 0, 0}));
		Edge n3n7  = graphSystem.createEdge(n3, n7);
		metricsMapper.attachComponent(n3n7,  new Metrics(5, new double[]{0, 0, 0}));
		Edge n4n6  = graphSystem.createEdge(n4, n6);
		metricsMapper.attachComponent(n4n6,  new Metrics(3, new double[]{0, 0, 0}));
		Edge n4n7  = graphSystem.createEdge(n4, n7);
		metricsMapper.attachComponent(n4n7,  new Metrics(7, new double[]{0, 0, 0}));
		Edge n4n9  = graphSystem.createEdge(n4, n9);
		metricsMapper.attachComponent(n4n9,  new Metrics(1, new double[]{0, 0, 0}));
		Edge n5n6  = graphSystem.createEdge(n5, n6);
		metricsMapper.attachComponent(n5n6,  new Metrics(2, new double[]{0, 0, 0}));
		Edge n5n8  = graphSystem.createEdge(n5, n8);
		metricsMapper.attachComponent(n5n8,  new Metrics(1, new double[]{0, 0, 0}));
		Edge n5n10 = graphSystem.createEdge(n5, n10);
		metricsMapper.attachComponent(n5n10, new Metrics(2, new double[]{0, 0, 0}));
		Edge n6n7  = graphSystem.createEdge(n6, n7);
		metricsMapper.attachComponent(n6n7,  new Metrics(2, new double[]{0, 0, 0}));
		Edge n6n10 = graphSystem.createEdge(n6, n10);
		metricsMapper.attachComponent(n6n10, new Metrics(3, new double[]{0, 0, 0}));
		Edge n7n10 = graphSystem.createEdge(n7, n10);
		metricsMapper.attachComponent(n7n10, new Metrics(6, new double[]{0, 0, 0}));
		Edge n8n10 = graphSystem.createEdge(n8, n10);
		metricsMapper.attachComponent(n8n10, new Metrics(12, new double[]{0, 0, 0}));
		Edge n9n4  = graphSystem.createEdge(n9, n4);
		metricsMapper.attachComponent(n9n4,  new Metrics(1, new double[]{0, 0, 0}));
		Edge n9n7  = graphSystem.createEdge(n9, n7);
		metricsMapper.attachComponent(n9n7,  new Metrics(2, new double[]{0, 0, 0}));

        Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n1, n10));


		// Path should have a cost of 5.
		assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 5", result.getCost() == 5);

		// Path should be: 1-2-5-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-2-5-10", result.getPath()[0] == n1n2 && result.getPath()[1] == n2n5 && result.getPath()[2] == n5n10);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    @Test
    public final void fig2point1BhandariBookP24() {
        EasyGraphProxy dp = new EasyGraphProxy(controller, new double[]{15, 15, 15});
        proxy.setProxy(dp);
        assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test", routingAlgorithmUnderTest.isOptimal());

        Graph graph = graphSystem.createGraph();
        Node A  = graphSystem.createNode(graph);
        Node B  = graphSystem.createNode(graph);
        Node C  = graphSystem.createNode(graph);
        Node D  = graphSystem.createNode(graph);
        Node E  = graphSystem.createNode(graph);
        Node Z  = graphSystem.createNode(graph);

        // Creating Edges and assigning costs and delays
        Edge AB  = graphSystem.createEdge(A, B);
        metricsMapper.attachComponent(AB, new Metrics(1, new double[]{0, 0, 0}));
        Edge BA  = graphSystem.createEdge(B, A);
        metricsMapper.attachComponent(BA, new Metrics(1, new double[]{0, 0, 0}));
        Edge AC  = graphSystem.createEdge(A, C);
        metricsMapper.attachComponent(AC, new Metrics(3, new double[]{0, 0, 0}));
        Edge CA  = graphSystem.createEdge(C, A);
        metricsMapper.attachComponent(CA, new Metrics(3, new double[]{0, 0, 0}));
        Edge AD  = graphSystem.createEdge(A, D);
        metricsMapper.attachComponent(AD, new Metrics(5, new double[]{0, 0, 0}));
        Edge DA  = graphSystem.createEdge(D, A);
        metricsMapper.attachComponent(DA, new Metrics(5, new double[]{0, 0, 0}));
        Edge CB  = graphSystem.createEdge(C, B);
        metricsMapper.attachComponent(CB, new Metrics(1, new double[]{0, 0, 0}));
        Edge BC  = graphSystem.createEdge(B, C);
        metricsMapper.attachComponent(BC, new Metrics(1, new double[]{0, 0, 0}));
        Edge ZB  = graphSystem.createEdge(Z, B);
        metricsMapper.attachComponent(ZB, new Metrics(3, new double[]{0, 0, 0}));
        Edge BZ  = graphSystem.createEdge(B, Z);
        metricsMapper.attachComponent(BZ, new Metrics(3, new double[]{0, 0, 0}));
        Edge ZC  = graphSystem.createEdge(Z, C);
        metricsMapper.attachComponent(ZC, new Metrics(1, new double[]{0, 0, 0}));
        Edge CZ  = graphSystem.createEdge(C, Z);
        metricsMapper.attachComponent(CZ, new Metrics(1, new double[]{0, 0, 0}));
        Edge DC  = graphSystem.createEdge(D, C);
        metricsMapper.attachComponent(DC, new Metrics(2, new double[]{0, 0, 0}));
        Edge CD  = graphSystem.createEdge(C, D);
        metricsMapper.attachComponent(CD, new Metrics(2, new double[]{0, 0, 0}));
        Edge DZ  = graphSystem.createEdge(D, Z);
        metricsMapper.attachComponent(DZ, new Metrics(4, new double[]{0, 0, 0}));
        Edge ZD  = graphSystem.createEdge(Z, D);
        metricsMapper.attachComponent(ZD, new Metrics(4, new double[]{0, 0, 0}));
        Edge DE  = graphSystem.createEdge(D, E);
        metricsMapper.attachComponent(DE, new Metrics(2, new double[]{0, 0, 0}));
        Edge ED  = graphSystem.createEdge(E, D);
        metricsMapper.attachComponent(ED, new Metrics(2, new double[]{0, 0, 0}));
        Edge ZE  = graphSystem.createEdge(Z, E);
        metricsMapper.attachComponent(ZE, new Metrics(2, new double[]{0, 0, 0}));
        Edge EZ  = graphSystem.createEdge(E, Z);
        metricsMapper.attachComponent(EZ, new Metrics(2, new double[]{0, 0, 0}));

        Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(A, Z));

        // Path should have a cost of 3.
        assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
        assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 3", result.getCost() == 3);

        // Path should be: ABCZ
        assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
        assertTrue("Path should be ABCZ", result.getPath()[0] == AB && result.getPath()[1] == BC && result.getPath()[2] == CZ);

        result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(A, D));

        // Path should have a cost of 3.
        assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
        assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 3", result.getCost() == 4);

        // Path should be: ABCD
        assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
        assertTrue("Path should be ABCZ", result.getPath()[0] == AB && result.getPath()[1] == BC && result.getPath()[2] == CD);

        result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(A, E));

        // Path should have a cost of 5.
        assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
        assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 3", result.getCost() == 5);

        // Path should be: ABCZE
        assertTrue("Path found is of length " + result.getPath().length + " but should be of length 4", result.getPath().length == 4);
        assertTrue("Path should be ABCZ", result.getPath()[0] == AB && result.getPath()[1] == BC && result.getPath()[2] == CZ && result.getPath()[3] == ZE);
    }
}
