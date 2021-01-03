package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LARACAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {
    @Before
    public void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest = new LARACAlgorithm(controller,new DijkstraAlgorithm(controller));
	    routingAlgorithmUnderTest.setProxy(proxy);
		referenceIdenticalAlgorithm = new LARACAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
    }

	@Test
	public final void figure3Of1978Paper() {
		CSPAlgorithm routingAlgorithm = (CSPAlgorithm) routingAlgorithmUnderTest;
		PathPlumberProxy plumberProxy = proxy;
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		plumberProxy.setProxy(dp);
		routingAlgorithm.setProxy(plumberProxy);

		Graph graph = graphSystem.createGraph();
		Node s  = graphSystem.createNode(graph);
		Node n1 = graphSystem.createNode(graph);
		Node n2 = graphSystem.createNode(graph);
		Node n3 = graphSystem.createNode(graph);
		Node n4 = graphSystem.createNode(graph);
		Node n5 = graphSystem.createNode(graph);
		Node n6 = graphSystem.createNode(graph);
		Node t  = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge sn1  = graphSystem.createEdge(s, n1);
		dummyMapper.attachComponent(sn1,  new DummyComponent(3, 8, 0, true, 0));
		Edge n1s  = graphSystem.createEdge(n1, s);
		dummyMapper.attachComponent(n1s,  new DummyComponent(3, 8, 0, true, 0));
		Edge sn2  = graphSystem.createEdge(s, n2);
		dummyMapper.attachComponent(sn2,  new DummyComponent(5, 3, 0, true, 0));
		Edge n2s  = graphSystem.createEdge(n2, s);
		dummyMapper.attachComponent(n2s,  new DummyComponent(5, 3, 0, true, 0));
		Edge sn3  = graphSystem.createEdge(s, n3);
		dummyMapper.attachComponent(sn3,  new DummyComponent(2, 9, 0, true, 0));
		Edge n3s  = graphSystem.createEdge(n3, s);
		dummyMapper.attachComponent(n3s,  new DummyComponent(2, 9, 0, true, 0));
		Edge n1n2 = graphSystem.createEdge(n1, n2);
		dummyMapper.attachComponent(n1n2, new DummyComponent(7, 3, 0, true, 0));
		Edge n2n1 = graphSystem.createEdge(n2, n1);
		dummyMapper.attachComponent(n2n1, new DummyComponent(7, 3, 0, true, 0));
		Edge n1n4 = graphSystem.createEdge(n1, n4);
		dummyMapper.attachComponent(n1n4, new DummyComponent(6, 6, 0, true, 0));
		Edge n4n1 = graphSystem.createEdge(n4, n1);
		dummyMapper.attachComponent(n4n1, new DummyComponent(6, 6, 0, true, 0));
		Edge n1n5 = graphSystem.createEdge(n1, n5);
		dummyMapper.attachComponent(n1n5, new DummyComponent(6, 1, 0, true, 0));
		Edge n5n1 = graphSystem.createEdge(n5, n1);
		dummyMapper.attachComponent(n5n1, new DummyComponent(6, 1, 0, true, 0));
		Edge n2n3 = graphSystem.createEdge(n2, n3);
		dummyMapper.attachComponent(n2n3, new DummyComponent(4, 5, 0, true, 0));
		Edge n3n2 = graphSystem.createEdge(n3, n2);
		dummyMapper.attachComponent(n3n2, new DummyComponent(4, 5, 0, true, 0));
		Edge n2n4 = graphSystem.createEdge(n2, n4);
		dummyMapper.attachComponent(n2n4, new DummyComponent(1, 10, 0, true, 0));
		Edge n4n2 = graphSystem.createEdge(n4, n2);
		dummyMapper.attachComponent(n4n2, new DummyComponent(1, 10, 0, true, 0));
		Edge n2n5 = graphSystem.createEdge(n2, n5);
		dummyMapper.attachComponent(n2n5, new DummyComponent(8, 4, 0, true, 0));
		Edge n5n2 = graphSystem.createEdge(n5, n2);
		dummyMapper.attachComponent(n5n2, new DummyComponent(8, 4, 0, true, 0));
		Edge n2n6 = graphSystem.createEdge(n2, n6);
		dummyMapper.attachComponent(n2n6, new DummyComponent(1, 11, 0, true, 0));
		Edge n6n2 = graphSystem.createEdge(n6, n2);
		dummyMapper.attachComponent(n6n2, new DummyComponent(1, 11, 0, true, 0));
		Edge n3n5 = graphSystem.createEdge(n3, n5);
		dummyMapper.attachComponent(n3n5, new DummyComponent(6, 1, 0, true, 0));
		Edge n5n3 = graphSystem.createEdge(n5, n3);
		dummyMapper.attachComponent(n5n3, new DummyComponent(6, 1, 0, true, 0));
		Edge n3n6 = graphSystem.createEdge(n3, n6);
		dummyMapper.attachComponent(n3n6, new DummyComponent(10, 3, 0, true, 0));
		Edge n6n3 = graphSystem.createEdge(n6, n3);
		dummyMapper.attachComponent(n6n3, new DummyComponent(10, 3, 0, true, 0));
		Edge n4n5 = graphSystem.createEdge(n4, n5);
		dummyMapper.attachComponent(n4n5, new DummyComponent(9, 4, 0, true, 0));
		Edge n5n4 = graphSystem.createEdge(n5, n4);
		dummyMapper.attachComponent(n5n4, new DummyComponent(9, 4, 0, true, 0));
		Edge n4t  = graphSystem.createEdge(n4, t);
		dummyMapper.attachComponent(n4t,  new DummyComponent(7, 4, 0, true, 0));
		Edge tn4  = graphSystem.createEdge(t, n4);
		dummyMapper.attachComponent(tn4,  new DummyComponent(7, 4, 0, true, 0));
		Edge n5n6 = graphSystem.createEdge(n5, n6);
		dummyMapper.attachComponent(n5n6, new DummyComponent(7, 5, 0, true, 0));
		Edge n6n5 = graphSystem.createEdge(n6, n5);
		dummyMapper.attachComponent(n6n5, new DummyComponent(7, 5, 0, true, 0));
		Edge n5t  = graphSystem.createEdge(n5, t);
		dummyMapper.attachComponent(n5t,  new DummyComponent(2, 8, 0, true, 0));
		Edge tn5  = graphSystem.createEdge(t, n5);
		dummyMapper.attachComponent(tn5,  new DummyComponent(2, 8, 0, true, 0));
		Edge n6t  = graphSystem.createEdge(n6, t);
		dummyMapper.attachComponent(n6t,  new DummyComponent(6, 2, 0, true, 0));
		Edge tn6  = graphSystem.createEdge(t, n6);
		dummyMapper.attachComponent(tn6,  new DummyComponent(6, 2, 0, true, 0));

		// Deadline set to 17.
		dp.setBounds(new double[]{17,2,3});

		Request request = new UnicastRequest(s, t);
		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(s, t));

		// Path should have a cost of 12 and delay of 16.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 16", result.getConstraintsValues()[0] == 16);

		// Path should be: s-2-6-t.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be s-2-6-t", result.getPath()[0] == sn2 && result.getPath()[1] == n2n6 && result.getPath()[2] == n6t);
		checkReferenceAlgorithms(request, result);
	}

	@Test
	public final void figure1Of1980PaperNoGapClosing() {
		// Note that the test here is without closing the gap.
		CSPAlgorithm routingAlgorithm = (CSPAlgorithm) routingAlgorithmUnderTest;
		PathPlumberProxy plumberProxy = proxy;
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		plumberProxy.setProxy(dp);
		routingAlgorithm.setProxy(plumberProxy);

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
		Path result = (Path) routingAlgorithm.solve(request);

		// Path should have a cost of 15 and delay of 0.6.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 15", result.getCost() == 15);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.6", result.getConstraintsValues()[0] == 0.6);

		// Path should be: 1-3-7-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-7-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n7 && result.getPath()[2] == n7n10);
		checkReferenceAlgorithms(request, result);
	}

	@Test
	public final void figure3Of2001Paper() {
		CSPAlgorithm routingAlgorithm = (CSPAlgorithm) routingAlgorithmUnderTest;
		PathPlumberProxy plumberProxy = proxy;
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		plumberProxy.setProxy(dp);
		routingAlgorithm.setProxy(plumberProxy);

		Graph graph = graphSystem.createGraph();
		Node a = graphSystem.createNode(graph);
		Node b = graphSystem.createNode(graph);
		Node c = graphSystem.createNode(graph);
		Node d = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge ab = graphSystem.createEdge(a, b);
		dummyMapper.attachComponent(ab, new DummyComponent(1, 4, 0, true, 0));
		Edge ba = graphSystem.createEdge(b, a);
		dummyMapper.attachComponent(ba, new DummyComponent(1, 4, 0, true, 0));
		Edge ac = graphSystem.createEdge(a, c);
		dummyMapper.attachComponent(ac, new DummyComponent(4, 3, 0, true, 0));
		Edge ca = graphSystem.createEdge(c, a);
		dummyMapper.attachComponent(ca, new DummyComponent(4, 3, 0, true, 0));
		Edge ad = graphSystem.createEdge(a, d);
		dummyMapper.attachComponent(ad, new DummyComponent(16, 2, 0, true, 0));
		Edge da = graphSystem.createEdge(d, a);
		dummyMapper.attachComponent(da, new DummyComponent(16, 2, 0, true, 0));
		Edge bc = graphSystem.createEdge(b, c);
		dummyMapper.attachComponent(bc, new DummyComponent(2, 1, 0, true, 0));
		Edge cb = graphSystem.createEdge(c, b);
		dummyMapper.attachComponent(cb, new DummyComponent(2, 1, 0, true, 0));
		Edge bd = graphSystem.createEdge(b, d);
		dummyMapper.attachComponent(bd, new DummyComponent(1, 8, 0, true, 0));
		Edge db = graphSystem.createEdge(d, b);
		dummyMapper.attachComponent(db, new DummyComponent(1, 8, 0, true, 0));
		Edge cd = graphSystem.createEdge(c, d);
		dummyMapper.attachComponent(cd, new DummyComponent(4, 3, 0, true, 0));
		Edge dc = graphSystem.createEdge(d, c);
		dummyMapper.attachComponent(dc, new DummyComponent(4, 3, 0, true, 0));

		// Deadline set to 7.
		dp.setBounds(new double[]{7,2,3});

		Request request = new UnicastRequest(a, d);
		Path result = (Path) routingAlgorithm.solve(request);

		// Path should have a cost of 8 and delay of 6.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 8", result.getCost() == 8);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 6", result.getConstraintsValues()[0] == 6);

		// Path should be: a-c-d.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be a-c-d", result.getPath()[0] == ac && result.getPath()[1] == cd);
		checkReferenceAlgorithms(request, result);
	}
}
