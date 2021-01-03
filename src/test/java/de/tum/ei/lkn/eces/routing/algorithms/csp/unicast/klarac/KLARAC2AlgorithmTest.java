package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.klarac;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.csp.CSPAlgorithm;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class KLARAC2AlgorithmTest extends ConstrainedShortestPathAlgorithmTest {

	@Before
	public final void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest =new KLARACAlgorithm(controller, 3);
		routingAlgorithmUnderTest.setProxy(proxy);
	}

	@Test
	public final void figure1Of1980PaperNoGapClosingk2() {
		// Note that the test here is without closing the gap.
		CSPAlgorithm routingAlgorithm = new KLARACAlgorithm(controller, 2);
		PathPlumberProxy plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
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

		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(n1, n10));

		// Path should have a cost of 15 and delay of 0.6.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 15", result.getCost() == 15);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.6", result.getConstraintsValues()[0] == 0.6);

		// Path should be: 1-3-7-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-7-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n7 && result.getPath()[2] == n7n10);
	}

	@Test
	public final void figure1Of1980PaperNoGapClosingk3() {
		// Note that the test here is without closing the gap.
		CSPAlgorithm routingAlgorithm = new KLARACAlgorithm(controller, 3);
		PathPlumberProxy plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
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

		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(n1, n10));

		// Path should have a cost of 15 and delay of 0.6.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 15", result.getCost() == 15);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.6", result.getConstraintsValues()[0] == 0.6);

		// Path should be: 1-3-7-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-7-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n7 && result.getPath()[2] == n7n10);
	}

	@Test
	public final void figure1Of1980PaperNoGapClosingk4() {
		// Note that the test here is without closing the gap.
		CSPAlgorithm routingAlgorithm = new KLARACAlgorithm(controller, 4);
		PathPlumberProxy plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
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

		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(n1, n10));

		// Path should have a cost of 15 and delay of 0.6.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 15", result.getCost() == 15);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.6", result.getConstraintsValues()[0] == 0.6);

		// Path should be: 1-3-7-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-7-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n7 && result.getPath()[2] == n7n10);
	}

	@Test
	public final void figure1Of1980PaperNoGapClosingk5() {
		// Note that the test here is without closing the gap.
		CSPAlgorithm routingAlgorithm = new KLARACAlgorithm(controller, 5);
		PathPlumberProxy plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
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

		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(n1, n10));

		// Path should have a cost of 14 and delay of 0.9.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 14", result.getCost() == 14);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 0.9", result.getConstraintsValues()[0] == 0.9);

		// Path should be: 1-3-6-10.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 1-3-6-10", result.getPath()[0] == n1n3 && result.getPath()[1] == n3n6 && result.getPath()[2] == n6n10);
	}
}
