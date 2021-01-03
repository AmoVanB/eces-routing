package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcr;

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
public class DCRTest extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new DCRAlgorithm(controller);
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

	@Test
	public final void figure2OfSFDCLCPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

		Graph graph = graphSystem.createGraph();
		Node a = graphSystem.createNode(graph);
		Node b = graphSystem.createNode(graph);
		Node c = graphSystem.createNode(graph);
		Node d = graphSystem.createNode(graph);
		Node e = graphSystem.createNode(graph);

		Edge ab = graphSystem.createEdge(a, b);
		Edge ba = graphSystem.createEdge(b, a);
		Edge ac = graphSystem.createEdge(a, c);
		Edge ca = graphSystem.createEdge(c, a);
		Edge ad = graphSystem.createEdge(a, d);
		Edge da = graphSystem.createEdge(d, a);
		Edge ae = graphSystem.createEdge(a, e);
		Edge ea = graphSystem.createEdge(e, a);
		Edge bc = graphSystem.createEdge(b, c);
		Edge cb = graphSystem.createEdge(c, b);
		Edge bd = graphSystem.createEdge(b, d);
		Edge db = graphSystem.createEdge(d, b);
		Edge be = graphSystem.createEdge(b, e);
		Edge eb = graphSystem.createEdge(e, b);
		Edge cd = graphSystem.createEdge(c, d);
		Edge dc = graphSystem.createEdge(d, c);
		Edge ce = graphSystem.createEdge(c, e);
		Edge ec = graphSystem.createEdge(e, c);
		Edge de = graphSystem.createEdge(d, e);
		Edge ed = graphSystem.createEdge(e, d);

		// Setting cost and delay of the links.
		dummyMapper.attachComponent(ab, new DummyComponent(4, 5, 1, true, 0));
		dummyMapper.attachComponent(ba, new DummyComponent(4, 5, 1, true, 0));
		dummyMapper.attachComponent(ac, new DummyComponent(3, 1, 1, true, 0));
		dummyMapper.attachComponent(ca, new DummyComponent(3, 1, 1, true, 0));
		dummyMapper.attachComponent(ad, new DummyComponent(2, 4, 1, true, 0));
		dummyMapper.attachComponent(da, new DummyComponent(2, 4, 1, true, 0));
		dummyMapper.attachComponent(ae, new DummyComponent(5, 1, 1, true, 0));
		dummyMapper.attachComponent(ea, new DummyComponent(5, 1, 1, true, 0));
		dummyMapper.attachComponent(bc, new DummyComponent(3, 4, 1, true, 0));
		dummyMapper.attachComponent(cb, new DummyComponent(3, 4, 1, true, 0));
		dummyMapper.attachComponent(bd, new DummyComponent(6, 8, 1, true, 0));
		dummyMapper.attachComponent(db, new DummyComponent(6, 8, 1, true, 0));
		dummyMapper.attachComponent(be, new DummyComponent(7, 1, 1, true, 0));
		dummyMapper.attachComponent(eb, new DummyComponent(7, 1, 1, true, 0));
		dummyMapper.attachComponent(cd, new DummyComponent(1, 4, 1, true, 0));
		dummyMapper.attachComponent(dc, new DummyComponent(1, 4, 1, true, 0));
		dummyMapper.attachComponent(ce, new DummyComponent(1, 1, 1, true, 0));
		dummyMapper.attachComponent(ec, new DummyComponent(1, 1, 1, true, 0));
		dummyMapper.attachComponent(de, new DummyComponent(2, 4, 1, true, 0));
		dummyMapper.attachComponent(ed, new DummyComponent(2, 4, 1, true, 0));

		// Deadline set to 3.5.
		dp.setBounds(new double[]{3.5, 2, 3});

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(a, b));

		// Path should have a cost of 12 and delay of 2.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 2", result.getConstraintsValues()[0] == 2);

		// Path should be: a-e-b.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be a-e-b", result.getPath()[0] == ae && result.getPath()[1] == eb);
		checkReferenceAlgorithms(new UnicastRequest(a, b), result);
	}

	@Test
	public final void figure2OfDCRPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

		Graph graph = graphSystem.createGraph();
		Node a = graphSystem.createNode(graph);
		Node b = graphSystem.createNode(graph);
		Node c = graphSystem.createNode(graph);
		Node d = graphSystem.createNode(graph);
		Node e = graphSystem.createNode(graph);

		Edge ab = graphSystem.createEdge(a, b);
		Edge ba = graphSystem.createEdge(b, a);
		Edge ac = graphSystem.createEdge(a, c);
		Edge ca = graphSystem.createEdge(c, a);
		Edge bd = graphSystem.createEdge(b, d);
		Edge db = graphSystem.createEdge(d, b);
		Edge cd = graphSystem.createEdge(c, d);
		Edge dc = graphSystem.createEdge(d, c);
		Edge ce = graphSystem.createEdge(c, e);
		Edge ec = graphSystem.createEdge(e, c);
		Edge de = graphSystem.createEdge(d, e);
		Edge ed = graphSystem.createEdge(e, d);

		// Setting cost and delay of the links.
		dummyMapper.attachComponent(ab, new DummyComponent(20, 2, 1, true, 0));
		dummyMapper.attachComponent(ba, new DummyComponent(20, 2, 1, true, 0));
		dummyMapper.attachComponent(ac, new DummyComponent(30, 1, 1, true, 0));
		dummyMapper.attachComponent(ca, new DummyComponent(30, 1, 1, true, 0));
		dummyMapper.attachComponent(bd, new DummyComponent(10, 1, 1, true, 0));
		dummyMapper.attachComponent(db, new DummyComponent(10, 1, 1, true, 0));
		dummyMapper.attachComponent(cd, new DummyComponent(10, 1, 1, true, 0));
		dummyMapper.attachComponent(dc, new DummyComponent(10, 1, 1, true, 0));
		dummyMapper.attachComponent(ce, new DummyComponent(30, 1, 1, true, 0));
		dummyMapper.attachComponent(ec, new DummyComponent(30, 1, 1, true, 0));
		dummyMapper.attachComponent(de, new DummyComponent(10, 2, 1, true, 0));
		dummyMapper.attachComponent(ed, new DummyComponent(10, 2, 1, true, 0));

		// Deadline set to 4.99.
		dp.setBounds(new double[]{4.99, 2, 3});

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(a, e));

		// Path should have a cost of 50 and delay of 4.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 50", result.getCost() == 50);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 4", result.getConstraintsValues()[0] == 4);

		// Path should be: a-c-d-e.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be a-e-b", result.getPath()[0] == ac && result.getPath()[1] == cd && result.getPath()[2] == de);
		checkReferenceAlgorithms(new UnicastRequest(a, e), result);
	}
}
