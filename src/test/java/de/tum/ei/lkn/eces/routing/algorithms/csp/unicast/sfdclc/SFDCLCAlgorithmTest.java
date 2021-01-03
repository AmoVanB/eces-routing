package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sfdclc;

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

public class SFDCLCAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {
    @Before
    public void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest =new SFDCLCAlgorithm(controller);
	    routingAlgorithmUnderTest.setProxy(proxy);
    }

	@Test
	public final void figure2OfPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);
		routingAlgorithmUnderTest.setProxy(proxy);

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

		// Path should have a cost of 11 and delay of 3.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 11", result.getCost() == 11);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 3", result.getConstraintsValues()[0] == 3);

		// Path should be: a-c-e-b.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be a-c-e-b", result.getPath()[0] == ac && result.getPath()[1] == ce && result.getPath()[2] == eb);
		checkReferenceAlgorithms(new UnicastRequest(a, b), result);
    }
}
