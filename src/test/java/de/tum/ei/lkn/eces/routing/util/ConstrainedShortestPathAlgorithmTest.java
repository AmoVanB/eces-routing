package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.mocks.DummyPreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Base test class for CSP algorithms algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class ConstrainedShortestPathAlgorithmTest extends ShortestPathAlgorithmTest {
	protected RoutingAlgorithm referenceIdenticalAlgorithm;
	protected RoutingAlgorithm referenceCostAlgorithm;

	@Test
	public final void shortestPathTestWithHighDelayLink() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		dummyMapper.get(edges[1].getEntity()).delay = 5.0;

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[2]);
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", path != null);
		assertTrue("Hop count wrong should be 4 but is " + path.getPath().length, path.getPath().length == 4);
		assertTrue("Wrong edge used", path.getPath()[0] == edges[8]);
		assertTrue("Wrong edge used", path.getPath()[1] == edges[4]);
		assertTrue("Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue("Wrong edge used", path.getPath()[3] == edges[9]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 4 but is" + path.getCost(), path.getCost() == 4);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));
		checkReferenceAlgorithms(request, path);
	}

	@Test
	public final void testPreviousEdgeDelay() {
		DummyPreviousEdgeProxy dummyEdgeProxy = new DummyPreviousEdgeProxy(controller);
		proxy.setProxy(dummyEdgeProxy);

		assumeTrue("Algorithm could not solve previous edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

		createPreviousEdgeTopology();
		dummyEdgeProxy.setConstSwitch(edges[1],edges[5]);

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
		checkReferenceAlgorithms(request, path);
	}

	@Test
	public final void figure1OfLARAC1980PaperCSPOptimal() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);
		assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

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
		dummyMapper.attachComponent(n1n2,  new DummyComponent(1, .7, 0, true, 0));
		Edge n1n3  = graphSystem.createEdge(n1, n3);
		dummyMapper.attachComponent(n1n3,  new DummyComponent(4, .2, 0, true, 0));
		Edge n1n4  = graphSystem.createEdge(n1, n4);
		dummyMapper.attachComponent(n1n4,  new DummyComponent(7, .1, 0, true, 0));
		Edge n1n8  = graphSystem.createEdge(n1, n8);
		dummyMapper.attachComponent(n1n8,  new DummyComponent(9, .45, 0, true, 0));
		Edge n1n9  = graphSystem.createEdge(n1, n9);
		dummyMapper.attachComponent(n1n9,  new DummyComponent(3, .7, 0, true, 0));
		Edge n2n5  = graphSystem.createEdge(n2, n5);
		dummyMapper.attachComponent(n2n5,  new DummyComponent(2, .5, 0, true, 0));
		Edge n2n6  = graphSystem.createEdge(n2, n6);
		dummyMapper.attachComponent(n2n6,  new DummyComponent(2, .5, 0, true, 0));
		Edge n2n8  = graphSystem.createEdge(n2, n8);
		dummyMapper.attachComponent(n2n8,  new DummyComponent(4, .06, 0, true, 0));
		Edge n3n5  = graphSystem.createEdge(n3, n5);
		dummyMapper.attachComponent(n3n5,  new DummyComponent(3, .2, 0, true, 0));
		Edge n3n6  = graphSystem.createEdge(n3, n6);
		dummyMapper.attachComponent(n3n6,  new DummyComponent(7, .3, 0, true, 0));
		Edge n3n7  = graphSystem.createEdge(n3, n7);
		dummyMapper.attachComponent(n3n7,  new DummyComponent(5, .3, 0, true, 0));
		Edge n4n6  = graphSystem.createEdge(n4, n6);
		dummyMapper.attachComponent(n4n6,  new DummyComponent(3, .8, 0, true, 0));
		Edge n4n7  = graphSystem.createEdge(n4, n7);
		dummyMapper.attachComponent(n4n7,  new DummyComponent(7, .15, 0, true, 0));
		Edge n4n9  = graphSystem.createEdge(n4, n9);
		dummyMapper.attachComponent(n4n9,  new DummyComponent(1, .05, 0, true, 0));
		Edge n5n6  = graphSystem.createEdge(n5, n6);
		dummyMapper.attachComponent(n5n6,  new DummyComponent(2, .7, 0, true, 0));
		Edge n5n8  = graphSystem.createEdge(n5, n8);
		dummyMapper.attachComponent(n5n8,  new DummyComponent(1, .15, 0, true, 0));
		Edge n5n10 = graphSystem.createEdge(n5, n10);
		dummyMapper.attachComponent(n5n10, new DummyComponent(2, .8, 0, true, 0));
		Edge n6n7  = graphSystem.createEdge(n6, n7);
		dummyMapper.attachComponent(n6n7,  new DummyComponent(2, .2, 0, true, 0));
		Edge n6n10 = graphSystem.createEdge(n6, n10);
		dummyMapper.attachComponent(n6n10, new DummyComponent(3, .4, 0, true, 0));
		Edge n7n10 = graphSystem.createEdge(n7, n10);
		dummyMapper.attachComponent(n7n10, new DummyComponent(6, .1, 0, true, 0));
		Edge n8n10 = graphSystem.createEdge(n8, n10);
		dummyMapper.attachComponent(n8n10, new DummyComponent(12, .05, 0, true, 0));
		Edge n9n4  = graphSystem.createEdge(n9, n4);
		dummyMapper.attachComponent(n9n4,  new DummyComponent(1, .05, 0, true, 0));
		Edge n9n7  = graphSystem.createEdge(n9, n7);
		dummyMapper.attachComponent(n9n7,  new DummyComponent(2, .8, 0, true, 0));

		// Deadline set to 1.
		dp.setBounds(new double[]{1, 2, 3});

		Request request = new UnicastRequest(n1, n10);
		Path result = (Path) routingAlgorithmUnderTest.solve(request);

		// Path should have a cost of 14 and delay of 0.9.
		assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 14", result.getCost() == 14);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.9", result.getConstraintsValues()[0] == 0.9);

		// Path should be: 1-3-6-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-6-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n6 && result.getPath()[2] == n6n10);
		checkReferenceAlgorithms(request, result);
	}

	@Test
	public final void figure7OfChenNahrstedt1998Paper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);
		assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

		Graph graph = graphSystem.createGraph();
		Node s = graphSystem.createNode(graph);
		Node i = graphSystem.createNode(graph);
		Node j = graphSystem.createNode(graph);
		Node t = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge si = graphSystem.createEdge(s, i);
		dummyMapper.attachComponent(si, new DummyComponent(3, 1, 0, true, 0));
		Edge is = graphSystem.createEdge(i, s);
		dummyMapper.attachComponent(is, new DummyComponent(3, 1, 0, true, 0));
		Edge sj = graphSystem.createEdge(s, j);
		dummyMapper.attachComponent(sj, new DummyComponent(1, 1, 0, true, 0));
		Edge js = graphSystem.createEdge(j, s);
		dummyMapper.attachComponent(js, new DummyComponent(1, 1, 0, true, 0));
		Edge st = graphSystem.createEdge(s, t);
		dummyMapper.attachComponent(st, new DummyComponent(1, 9, 0, true, 0));
		Edge ts = graphSystem.createEdge(t, s);
		dummyMapper.attachComponent(ts, new DummyComponent(1, 9, 0, true, 0));
		Edge tj = graphSystem.createEdge(t, j);
		dummyMapper.attachComponent(tj, new DummyComponent(1, 9, 0, true, 0));
		Edge jt = graphSystem.createEdge(j, t);
		dummyMapper.attachComponent(jt, new DummyComponent(1, 9, 0, true, 0));
		Edge ij = graphSystem.createEdge(i, j);
		dummyMapper.attachComponent(ij, new DummyComponent(2, 3, 0, true, 0));
		Edge ji = graphSystem.createEdge(j, i);
		dummyMapper.attachComponent(ji, new DummyComponent(2, 3, 0, true, 0));
		Edge it = graphSystem.createEdge(i, t);
		dummyMapper.attachComponent(it, new DummyComponent(9, 1, 0, true, 0));
		Edge ti = graphSystem.createEdge(t, i);
		dummyMapper.attachComponent(ti, new DummyComponent(9, 1, 0, true, 0));

		// Deadline set to 8.
		dp.setBounds(new double[]{8, 2, 3});

		Request request = new UnicastRequest(s, t);
		Path result = (Path) routingAlgorithmUnderTest.solve(request);

		// Two paths are possible:
		// s-i-t: cost 12, delay 2.
		assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
		if(result.getPath().length == 2) {
			assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
			assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 2", result.getConstraintsValues()[0] == 2);
			assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
			assertTrue("Path should be s-i-t", result.getPath()[0] == si && result.getPath()[1] == it);
		}
		// s-j-i-t: cost 12, delay 5.
		else if(result.getPath().length == 3) {
			assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
			assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 5", result.getConstraintsValues()[0] == 5);
			assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
			assertTrue("Path should be s-i-t", result.getPath()[0] == sj && result.getPath()[1] == ji && result.getPath()[2] == it);
		}
		else
			assertTrue("The path found should be of length 2 or 3", false);

		checkReferenceAlgorithms(request, result);
	}

	@Test
	public final void figure1ofELARACPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);
		assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test",routingAlgorithmUnderTest.isOptimal());

		Graph graph = graphSystem.createGraph();
		Node s  = graphSystem.createNode(graph);
		Node n1 = graphSystem.createNode(graph);
		Node n2 = graphSystem.createNode(graph);
		Node n3 = graphSystem.createNode(graph);
		Node n4 = graphSystem.createNode(graph);
		Node n5 = graphSystem.createNode(graph);
		Node t  = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge sn2  = graphSystem.createEdge(s, n2);
		dummyMapper.attachComponent(sn2,  new DummyComponent(1, 10, 0, true, 0));
		Edge n2s  = graphSystem.createEdge(n2, s);
		dummyMapper.attachComponent(n2s,  new DummyComponent(1, 10, 0, true, 0));
		Edge sn4  = graphSystem.createEdge(s, n4);
		dummyMapper.attachComponent(sn4,  new DummyComponent(3, 10, 0, true, 0));
		Edge n4s  = graphSystem.createEdge(n4, s);
		dummyMapper.attachComponent(n4s,  new DummyComponent(3, 10, 0, true, 0));
		Edge n3n2 = graphSystem.createEdge(n3, n2);
		dummyMapper.attachComponent(n3n2, new DummyComponent(1, 50, 0, true, 0));
		Edge n2n3 = graphSystem.createEdge(n2, n3);
		dummyMapper.attachComponent(n2n3, new DummyComponent(1, 50, 0, true, 0));
		Edge n4n2 = graphSystem.createEdge(n4, n2);
		dummyMapper.attachComponent(n4n2, new DummyComponent(2, 10, 0, true, 0));
		Edge n2n4 = graphSystem.createEdge(n2, n4);
		dummyMapper.attachComponent(n2n4, new DummyComponent(2, 10, 0, true, 0));
		Edge n5n2 = graphSystem.createEdge(n5, n2);
		dummyMapper.attachComponent(n5n2, new DummyComponent(3, 15, 0, true, 0));
		Edge n2n5 = graphSystem.createEdge(n2, n5);
		dummyMapper.attachComponent(n2n5, new DummyComponent(3, 15, 0, true, 0));
		Edge n3n5 = graphSystem.createEdge(n3, n5);
		dummyMapper.attachComponent(n3n5, new DummyComponent(2, 10, 0, true, 0));
		Edge n5n3 = graphSystem.createEdge(n5, n3);
		dummyMapper.attachComponent(n5n3, new DummyComponent(2, 10, 0, true, 0));
		Edge n3t  = graphSystem.createEdge(n3, t);
		dummyMapper.attachComponent(n3t,  new DummyComponent(3, 20, 0, true, 0));
		Edge tn3  = graphSystem.createEdge(t, n3);
		dummyMapper.attachComponent(tn3,  new DummyComponent(3, 20, 0, true, 0));
		Edge n4n5 = graphSystem.createEdge(n4, n5);
		dummyMapper.attachComponent(n4n5, new DummyComponent(3, 45, 0, true, 0));
		Edge n5n4 = graphSystem.createEdge(n5, n4);
		dummyMapper.attachComponent(n5n4, new DummyComponent(3, 45, 0, true, 0));
		Edge n5t  = graphSystem.createEdge(n5, t);
		dummyMapper.attachComponent(n5t,  new DummyComponent(2, 20, 0, true, 0));
		Edge tn5  = graphSystem.createEdge(t, n5);
		dummyMapper.attachComponent(tn5,  new DummyComponent(2, 20, 0, true, 0));

		// Deadline set to 50.
		dp.setBounds(new double[]{50, 2, 3});

		Request request = new UnicastRequest(s, t);
		Path result = (Path) routingAlgorithmUnderTest.solve(request);

		// s-2-5-t: cost 6, delay 45.
		assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 6", result.getCost() == 6);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 45", result.getConstraintsValues()[0] == 45);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be s-i-t", result.getPath()[0] == sn2 && result.getPath()[1] == n2n5 && result.getPath()[2] == n5t);
		checkReferenceAlgorithms(request, result);
	}

	protected void checkReferenceAlgorithms(Request request, Path path) {
		if(referenceIdenticalAlgorithm != null) {
			Path refPath = (Path) referenceIdenticalAlgorithm.solve(request);

			if((refPath == null && path != null) || (refPath != null && path == null))
				assertTrue(false);

			if(refPath == null && path == null) {
				assertTrue(true);
				return;
			}

			assertTrue(routingAlgorithmUnderTest + ": Path not equal to the one found by reference algorithm\nref:\t" + refPath + "\n\tcost: " + refPath.getCost() + "\nfound:\t" + path + "\n\tcost: " + path.getCost(), path.equals(refPath));
		}

		if(referenceCostAlgorithm != null) {
			Path refPath = (Path) referenceCostAlgorithm.solve(request);
			if((refPath == null && path != null) || (refPath != null && path == null))
				assertTrue(false);

			assertTrue(routingAlgorithmUnderTest + ": Path does not have the same cost as the one found by ref alglorithm\nref:\t" + refPath + "\n\tcost: " + refPath.getCost() + "\nfound:\t" + path + "\n\tcost: " + path.getCost(), path.getCost() == refPath.getCost());
		}
	}
}
