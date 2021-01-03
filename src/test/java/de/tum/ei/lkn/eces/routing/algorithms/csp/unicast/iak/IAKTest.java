package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.iak;

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

public class IAKTest extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new IAKAlgorithm(controller);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
	}

	@Test
	public final void figure7OfChenNahrstedt1998PaperSL() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

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

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(s, t));

		// Path should have a cost of 12 and delay of 2.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 2", result.getConstraintsValues()[0] == 2);

		// Path should be: s-i-t.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be s-i-t", result.getPath()[0] == si && result.getPath()[1] == it);
		checkReferenceAlgorithms(new UnicastRequest(s, t), result);
	}
}
