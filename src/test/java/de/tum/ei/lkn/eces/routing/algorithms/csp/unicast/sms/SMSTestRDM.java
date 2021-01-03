package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sms;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for the DCR Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SMSTestRDM extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new SMSAlgorithm(controller, PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING, Integer.MAX_VALUE);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
	}

	@Test
	public final void figureOfPaperSMSPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

		Graph graph = graphSystem.createGraph();
		Node n1 = graphSystem.createNode(graph);
		Node n2 = graphSystem.createNode(graph);
		Node n3 = graphSystem.createNode(graph);
		Node n4 = graphSystem.createNode(graph);
		Node n5 = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge n1n2 = graphSystem.createEdge(n1, n2);
		dummyMapper.attachComponent(n1n2, new DummyComponent(3, 3, 0, true, 0));
		Edge n2n1 = graphSystem.createEdge(n2, n1);
		dummyMapper.attachComponent(n2n1, new DummyComponent(3, 3, 0, true, 0));
		Edge n1n4 = graphSystem.createEdge(n1, n4);
		dummyMapper.attachComponent(n1n4, new DummyComponent(7, 2, 0, true, 0));
		Edge n4n1 = graphSystem.createEdge(n4, n1);
		dummyMapper.attachComponent(n4n1, new DummyComponent(7, 2, 0, true, 0));
		Edge n1n5 = graphSystem.createEdge(n1, n5);
		dummyMapper.attachComponent(n1n5, new DummyComponent(3, 3, 0, true, 0));
		Edge n5n1 = graphSystem.createEdge(n5, n1);
		dummyMapper.attachComponent(n5n1, new DummyComponent(3, 3, 0, true, 0));
		Edge n3n2 = graphSystem.createEdge(n3, n2);
		dummyMapper.attachComponent(n3n2, new DummyComponent(1, 6, 0, true, 0));
		Edge n2n3 = graphSystem.createEdge(n2, n3);
		dummyMapper.attachComponent(n2n3, new DummyComponent(1, 6, 0, true, 0));
		Edge n5n2 = graphSystem.createEdge(n5, n2);
		dummyMapper.attachComponent(n5n2, new DummyComponent(1, 2, 0, true, 0));
		Edge n2n5 = graphSystem.createEdge(n2, n5);
		dummyMapper.attachComponent(n2n5, new DummyComponent(1, 2, 0, true, 0));
		Edge n3n5 = graphSystem.createEdge(n3, n5);
		dummyMapper.attachComponent(n3n5, new DummyComponent(2, 2, 0, true, 0));
		Edge n5n3 = graphSystem.createEdge(n5, n3);
		dummyMapper.attachComponent(n5n3, new DummyComponent(2, 2, 0, true, 0));
		Edge n3n4 = graphSystem.createEdge(n3, n4);
		dummyMapper.attachComponent(n3n4, new DummyComponent(1, 2, 0, true, 0));
		Edge n4n3 = graphSystem.createEdge(n4, n3);
		dummyMapper.attachComponent(n4n3, new DummyComponent(1, 2, 0, true, 0));

		// Call1: 1 -> 3 (deadline = 6).
		dp.setBounds(new double[]{6, 2, 3});
		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n1, n3));
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 5", result.getCost() == 5);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 5", result.getConstraintsValues()[0] == 5);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be 1-5-3", result.getPath()[0] == n1n5 && result.getPath()[1] == n5n3);
		checkReferenceAlgorithms(new UnicastRequest(n1, n3), result);

		// Call2: 2 -> 4 (deadline = 7).
		dp.setBounds(new double[]{7, 2, 3});
		result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n2, n4));
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 4", result.getCost() == 4);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 6", result.getConstraintsValues()[0] == 6);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 2-5-3-4", result.getPath()[0] == n2n5 && result.getPath()[1] == n5n3 && result.getPath()[2] == n3n4);
		checkReferenceAlgorithms(new UnicastRequest(n2, n4), result);

		// Call3: 2 -> 4 (deadline = 6).
		// -> Paper was wrong with behavior of own algorithm...
		dp.setBounds(new double[]{6, 2, 3});
		result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n2, n4));
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 10", result.getCost() == 10);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 5", result.getConstraintsValues()[0] == 5);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be 2-5-3-4", result.getPath()[0] == n2n1 && result.getPath()[1] == n1n4);
		checkReferenceAlgorithms(new UnicastRequest(n2, n4), result);

		// Let's change the test of the paper which is wrong. Now (1-2) is saturated.
		DummyComponent cp21 = dummyMapper.get(n2n1.getEntity());
		dummyMapper.updateComponent(cp21, () -> cp21.use = false);

		// Call4: 2 -> 4 (deadline = 6) should now go through 2-5-3-4.
		dp.setBounds(new double[]{6, 2, 3});
		result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n2, n4));
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 4", result.getCost() == 4);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 6", result.getConstraintsValues()[0] == 6);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be 2-5-3-4", result.getPath()[0] == n2n5 && result.getPath()[1] == n5n3 && result.getPath()[2] == n3n4);
		checkReferenceAlgorithms(new UnicastRequest(n2, n4), result);

		// Let's saturate (2-5), the next call shouldn't be successful.
		DummyComponent cp25 = dummyMapper.get(n2n5.getEntity());
		dummyMapper.updateComponent(cp25, () -> cp25.use = false);

		// Call5: 2 -> 4 (deadline = 7).
		dp.setBounds(new double[]{7, 2, 3});
		result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(n2, n4));
		assertTrue("No path is feasible", result == null);
		checkReferenceAlgorithms(new UnicastRequest(n2, n4), result);
	}
}
