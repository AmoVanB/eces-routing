package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Base test class for MCSP algorithms algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class MultipleConstrainedShortestPathAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {
	@Test
	public final void shortestPathTestWithHighDelayAndHighLossLink() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		dummyMapper.get(edges[1].getEntity()).delay = 5.0;
		dummyMapper.get(edges[4].getEntity()).loss = 5.0;
		routingAlgorithmUnderTest.setDebugMode();
		Path path = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(nodes[0], nodes[2]));

		assertTrue(routingAlgorithmUnderTest + ": Should Be null" , path == null);
		checkReferenceAlgorithms(new UnicastRequest(nodes[0], nodes[2]), path);
	}

	@Test
	public final void figure1ofAgrawalPaper() {
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
		Node n6 = graphSystem.createNode(graph);
		Node t  = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge sn1  = graphSystem.createEdge(s, n1);
		dummyMapper.attachComponent(sn1,  new DummyComponent(50, 30, 76, true, 0));
		Edge n1s  = graphSystem.createEdge(n1, s);
		dummyMapper.attachComponent(n1s,  new DummyComponent(50, 30, 76, true, 0));
		Edge sn2  = graphSystem.createEdge(s, n2);
		dummyMapper.attachComponent(sn2,  new DummyComponent(43, 91, 83, true, 0));
		Edge n2s  = graphSystem.createEdge(n2, s);
		dummyMapper.attachComponent(n2s,  new DummyComponent(43, 91, 83, true, 0));
		Edge sn3  = graphSystem.createEdge(s, n3);
		dummyMapper.attachComponent(sn3,  new DummyComponent(5, 56, 27, true, 0));
		Edge n3s  = graphSystem.createEdge(n3, s);
		dummyMapper.attachComponent(n3s,  new DummyComponent(5, 56, 27, true, 0));
		Edge n1n2 = graphSystem.createEdge(n1, n2);
		dummyMapper.attachComponent(n1n2, new DummyComponent(45, 68, 68, true, 0));
		Edge n2n1 = graphSystem.createEdge(n2, n1);
		dummyMapper.attachComponent(n2n1, new DummyComponent(45, 68, 68, true, 0));
		Edge n1n4 = graphSystem.createEdge(n1, n4);
		dummyMapper.attachComponent(n1n4, new DummyComponent(51, 13, 59, true, 0));
		Edge n4n1 = graphSystem.createEdge(n4, n1);
		dummyMapper.attachComponent(n4n1, new DummyComponent(51, 13, 59, true, 0));
		Edge n1n5 = graphSystem.createEdge(n1, n5);
		dummyMapper.attachComponent(n1n5, new DummyComponent(55, 38, 21, true, 0));
		Edge n5n1 = graphSystem.createEdge(n5, n1);
		dummyMapper.attachComponent(n5n1, new DummyComponent(55, 38, 21, true, 0));
		Edge n3n2 = graphSystem.createEdge(n3, n2);
		dummyMapper.attachComponent(n3n2, new DummyComponent(28, 42, 42, true, 0));
		Edge n2n3 = graphSystem.createEdge(n2, n3);
		dummyMapper.attachComponent(n2n3, new DummyComponent(28, 42, 42, true, 0));
		Edge n5n2 = graphSystem.createEdge(n5, n2);
		dummyMapper.attachComponent(n5n2, new DummyComponent(57, 70, 65, true, 0));
		Edge n2n5 = graphSystem.createEdge(n2, n5);
		dummyMapper.attachComponent(n2n5, new DummyComponent(57, 70, 65, true, 0));
		Edge n6n2 = graphSystem.createEdge(n6, n2);
		dummyMapper.attachComponent(n6n2, new DummyComponent(35, 51, 9, true, 0));
		Edge n2n6 = graphSystem.createEdge(n2, n6);
		dummyMapper.attachComponent(n2n6, new DummyComponent(35, 51, 9, true, 0));
		Edge n3n6 = graphSystem.createEdge(n3, n6);
		dummyMapper.attachComponent(n3n6, new DummyComponent(19, 66, 70, true, 0));
		Edge n6n3 = graphSystem.createEdge(n6, n3);
		dummyMapper.attachComponent(n6n3, new DummyComponent(19, 66, 70, true, 0));
		Edge n4n5 = graphSystem.createEdge(n4, n5);
		dummyMapper.attachComponent(n4n5, new DummyComponent(49, 79, 52, true, 0));
		Edge n5n4 = graphSystem.createEdge(n5, n4);
		dummyMapper.attachComponent(n5n4, new DummyComponent(49, 79, 52, true, 0));
		Edge n4t = graphSystem.createEdge(n4, t);
		dummyMapper.attachComponent(n4t, new DummyComponent(48, 84, 49, true, 0));
		Edge tn4 = graphSystem.createEdge(t, n4);
		dummyMapper.attachComponent(tn4, new DummyComponent(48, 84, 49, true, 0));
		Edge n5n6 = graphSystem.createEdge(n5, n6);
		dummyMapper.attachComponent(n5n6, new DummyComponent(31, 42, 28, true, 0));
		Edge n6n5 = graphSystem.createEdge(n6, n5);
		dummyMapper.attachComponent(n6n5, new DummyComponent(31, 42, 28, true, 0));
		Edge n5t  = graphSystem.createEdge(n5, t);
		dummyMapper.attachComponent(n5t,  new DummyComponent(47, 15, 28, true, 0));
		Edge tn5  = graphSystem.createEdge(t, n5);
		dummyMapper.attachComponent(tn5,  new DummyComponent(47, 15, 28, true, 0));
		Edge n6t  = graphSystem.createEdge(n6, t);
		dummyMapper.attachComponent(n6t,  new DummyComponent(85, 5, 88, true, 0));
		Edge tn6  = graphSystem.createEdge(t, n6);
		dummyMapper.attachComponent(tn6,  new DummyComponent(85, 5, 88, true, 0));

		// Deadline set to 170-182.
		dp.setBounds(new double[]{170, 182, 3});

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(s, t));

		// s-1-5-t: (152-83-125)
		assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 152", result.getCost() == 152);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 83", result.getConstraintsValues()[0] == 83);
		assertTrue("Path found has delay 2 of " + result.getConstraintsValues()[1] + " but should have a delay 2 of 125", result.getConstraintsValues()[1] == 125);
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be s-1-5-t", result.getPath()[0] == sn1 && result.getPath()[1] == n1n5 && result.getPath()[2] == n5t);
		checkReferenceAlgorithms(new UnicastRequest(s, t), result);
	}
}
