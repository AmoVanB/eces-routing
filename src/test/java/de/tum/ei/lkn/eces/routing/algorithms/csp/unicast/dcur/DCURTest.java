package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcur;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf.CBFAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcr.DCRAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.iak.IAKAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac.LARACAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
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
 * Test class for the DCUR Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCURTest  extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new DCURAlgorithm(controller);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
	}

	@Test
	public final void figure1OfPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

		Graph graph = graphSystem.createGraph();
		Node a = graphSystem.createNode(graph);
		Node b = graphSystem.createNode(graph);
		Node c = graphSystem.createNode(graph);
		Node d = graphSystem.createNode(graph);
		Node e = graphSystem.createNode(graph);
		Node f = graphSystem.createNode(graph);
		Node g = graphSystem.createNode(graph);

		Edge ab = graphSystem.createEdge(a, b);
		Edge ag = graphSystem.createEdge(a, g);
		Edge gf = graphSystem.createEdge(g, f);
		Edge ge = graphSystem.createEdge(g, e);
		Edge bc = graphSystem.createEdge(b, c);
		Edge cd = graphSystem.createEdge(c, d);
		Edge ce = graphSystem.createEdge(c, e);
		Edge de = graphSystem.createEdge(d, e);

		// Setting cost and delay of the links.
		dummyMapper.attachComponent(ab, new DummyComponent(1, 1, 1, true,0));
		dummyMapper.attachComponent(ag, new DummyComponent(2, 1, 1, true,0));
		dummyMapper.attachComponent(gf, new DummyComponent(1, 1, 1, true,0));
		dummyMapper.attachComponent(ge, new DummyComponent(5, 1, 1, true,0));
		dummyMapper.attachComponent(bc, new DummyComponent(1, 1, 1, true,0));
		dummyMapper.attachComponent(cd, new DummyComponent(1, 1, 1, true,0));
		dummyMapper.attachComponent(ce, new DummyComponent(4, 1, 1, true,0));
		dummyMapper.attachComponent(de, new DummyComponent(1, 1, 1, true,0));

		// Deadline set to 3.
		dp.setBounds(new double[]{3, 2, 3});

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(a, e));

		// Path should have a cost of 6 and delay of 3.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 6", result.getCost() == 6);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 3", result.getConstraintsValues()[0] == 3);

		// Path should be: a-b-c-e.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be a-b-c-e", result.getPath()[0] == ab && result.getPath()[1] == bc && result.getPath()[2] == ce);
		checkReferenceAlgorithms(new UnicastRequest(a, e), result);
	}

	@Test
	public final void figure2OfPaper() {
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
		Edge bc = graphSystem.createEdge(b, c);
		Edge cb = graphSystem.createEdge(c, b);
		Edge ac = graphSystem.createEdge(a, c);
		Edge ca = graphSystem.createEdge(c, a);
		Edge bd = graphSystem.createEdge(b, d);
		Edge db = graphSystem.createEdge(d, b);
		Edge ae = graphSystem.createEdge(a, e);
		Edge ea = graphSystem.createEdge(e, a);
		Edge de = graphSystem.createEdge(d, e);
		Edge ed = graphSystem.createEdge(e, d);
		Edge ce = graphSystem.createEdge(c, e);
		Edge ec = graphSystem.createEdge(e, c);

		// Setting cost and delay of the links.
		dummyMapper.attachComponent(ab, new DummyComponent(2, 1, 0, true, 0));
		dummyMapper.attachComponent(ba, new DummyComponent(2, 1, 0, true, 0));
		dummyMapper.attachComponent(bc, new DummyComponent(1, 3, 0, true, 0));
		dummyMapper.attachComponent(cb, new DummyComponent(1, 3, 0, true, 0));
		dummyMapper.attachComponent(ac, new DummyComponent(2, 1, 0, true, 0));
		dummyMapper.attachComponent(ca, new DummyComponent(2, 1, 0, true, 0));
		dummyMapper.attachComponent(ae, new DummyComponent(1, 5, 0, true, 0));
		dummyMapper.attachComponent(ea, new DummyComponent(1, 5, 0, true, 0));
		dummyMapper.attachComponent(ce, new DummyComponent(1, 1, 0, true, 0));
		dummyMapper.attachComponent(ec, new DummyComponent(1, 1, 0, true, 0));
		dummyMapper.attachComponent(de, new DummyComponent(1, 5, 0, true, 0));
		dummyMapper.attachComponent(ed, new DummyComponent(1, 5, 0, true, 0));
		dummyMapper.attachComponent(db, new DummyComponent(8, 1, 0, true, 0));
		dummyMapper.attachComponent(bd, new DummyComponent(8, 1, 0, true, 0));

		// Deadline set to 8.
		dp.setBounds(new double[]{8,2,3});

		Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(a, d));

		// Path should have a cost of 10 and delay of 2.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 10", result.getCost() == 10);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 2", result.getConstraintsValues()[0] == 2);

		// Path should be: a-b-d.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 2", result.getPath().length == 2);
		assertTrue("Path should be a-b-d", result.getPath()[0] == ab && result.getPath()[1] == bd);
		checkReferenceAlgorithms(new UnicastRequest(a, d), result);
	}

	@Test
	public final void figure7OfChenNahrstedt1998PaperDCUR() {
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

	private Edge[] createEdge(Node source, Node destination, double cost, double delay) {
		Edge[] result = new Edge[2];
		result[0] = graphSystem.createEdge(source, destination);
		dummyMapper.attachComponent(result[0], new DummyComponent(cost, delay, 0, true, 0));
		result[1] = graphSystem.createEdge(destination, source);
		dummyMapper.attachComponent(result[1], new DummyComponent(cost, delay, 0, true, 0));
		return result;
	}

	@Test
	public final void figureOfOurOwnPaper() {
		DummyEdgeProxy dp = new DummyEdgeProxy(controller);
		proxy.setProxy(dp);

		Graph graph = graphSystem.createGraph();
		Node n1 = graphSystem.createNode(graph);
		Node n2 = graphSystem.createNode(graph);
		Node n3 = graphSystem.createNode(graph);
		Node n4 = graphSystem.createNode(graph);
		Node n5 = graphSystem.createNode(graph);
		Node n6 = graphSystem.createNode(graph);
		Node n7 = graphSystem.createNode(graph);
		Node n8 = graphSystem.createNode(graph);
		Node s  = graphSystem.createNode(graph);
		Node d  = graphSystem.createNode(graph);

		// Creating Edges and assigning costs and delays
		Edge[] sn1  = createEdge(s,  n1, 3, 5);
		Edge[] sn2  = createEdge(s,  n2, 2, 4);
		Edge[] sn3  = createEdge(s,  n3, 4, 3);
		Edge[] n1n3 = createEdge(n1, n3, 2, 3);
		Edge[] n1n4 = createEdge(n1, n4, 4, 3);
		Edge[] n2n3 = createEdge(n2, n3, 3, 1);
		Edge[] n2n5 = createEdge(n2, n5, 1, 3);
		Edge[] n3n4 = createEdge(n3, n4, 3, 1);
		Edge[] n3n5 = createEdge(n3, n5, 4, 2);
		Edge[] n3n6 = createEdge(n3, n6, 2, 2);
		Edge[] n4n6 = createEdge(n4, n6, 4, 4);
		Edge[] n4d  = createEdge(n4,  d, 3, 2);
		Edge[] n5n6 = createEdge(n5, n6, 2, 5);
		Edge[] n5n7 = createEdge(n5, n7, 5, 2);
		Edge[] n6n7 = createEdge(n6, n7, 2, 2);
		Edge[] n6n8 = createEdge(n6, n8, 3, 5);
		Edge[] n6d  = createEdge(n6,  d, 2, 3);
		Edge[] n7n8 = createEdge(n7, n8, 2, 2);
		Edge[] n8d  = createEdge(n8,  d, 1, 7);

		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(controller);

		PathPlumberProxy plumberProxy = new PathPlumberProxy(new int[]{0},
				new double[]{1.0},
				new int[0],
				new int[0]);
		DummyEdgeProxy dp2 = new DummyEdgeProxy(controller);
		plumberProxy.setProxy(dp2);
		dijkstra.setProxy(plumberProxy);

		System.out.println("SP from any node to destination: ");
		System.out.println("\tS: " + dijkstra.solve(new UnicastRequest(s, d)));
		System.out.println("\t1: " + dijkstra.solve(new UnicastRequest(n1, d)));
		System.out.println("\t2: " + dijkstra.solve(new UnicastRequest(n2, d)));
		System.out.println("\t3: " + dijkstra.solve(new UnicastRequest(n3, d)));
		System.out.println("\t4: " + dijkstra.solve(new UnicastRequest(n4, d)));
		System.out.println("\t5: " + dijkstra.solve(new UnicastRequest(n5, d)));
		System.out.println("\t6: " + dijkstra.solve(new UnicastRequest(n6, d)));
		System.out.println("\t7: " + dijkstra.solve(new UnicastRequest(n7, d)));
		System.out.println("\t8: " + dijkstra.solve(new UnicastRequest(n8, d)));
		System.out.println("");

		// Deadline set to 10.
		System.out.println("CSP: (deadline: " + 10 + ")");
		dp.setBounds(new double[]{10,2,3});
		RoutingAlgorithm dcr = new DCRAlgorithm(controller);
		dcr.setProxy(proxy);
		Path dcrResult = (Path) dcr.solve(new UnicastRequest(s, d));
		System.out.println("\tDCR:");
		System.out.println("\t\t" + dcrResult);
		System.out.println("\t\tCost:  " + dcrResult.getCost());
		System.out.println("\t\tDelay: " + dcrResult.getConstraintsValues()[0]);
		RoutingAlgorithm iak = new IAKAlgorithm(controller);
		iak.setProxy(proxy);
		Path iakResult = (Path) iak.solve(new UnicastRequest(s, d));
		System.out.println("\tIAK:");
		System.out.println("\t\t" + iakResult);
		System.out.println("\t\tCost:  " + iakResult.getCost());
		System.out.println("\t\tDelay: " + iakResult.getConstraintsValues()[0]);
		Path dcurResult = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(s, d));
		System.out.println("\tDCUR:");
		System.out.println("\t\t" + dcurResult);
		System.out.println("\t\tCost:  " + dcurResult.getCost());
		System.out.println("\t\tDelay: " + dcurResult.getConstraintsValues()[0]);
		RoutingAlgorithm larac = new LARACAlgorithm(controller);
		larac.setProxy(proxy);
		Path laracResult = (Path) larac.solve(new UnicastRequest(s, d));
		System.out.println("\tLARAC (the monster):");
		System.out.println("\t\t" + laracResult);
		System.out.println("\t\tCost:  " + laracResult.getCost());
		System.out.println("\t\tDelay: " + laracResult.getConstraintsValues()[0]);
		RoutingAlgorithm cbf = new CBFAlgorithm(controller);
		cbf.setProxy(proxy);
		Path cbfResult = (Path) cbf.solve(new UnicastRequest(s, d));
		System.out.println("\tCBF:");
		System.out.println("\t\t" + cbfResult);
		System.out.println("\t\tCost:  " + cbfResult.getCost());
		System.out.println("\t\tDelay: " + cbfResult.getConstraintsValues()[0]);

		/*
		Path result = (Path) routingAlgorithm.solve(new UnicastRequest(s, t));

		// Path should have a cost of 12 and delay of 16.
		assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);
		assertTrue("Path found has delay of " + result.getConstraintsValues()[0] + " but should have a delay of 16", result.getConstraintsValues()[0] == 16);

		// Path should be: s-2-6-t.
		assertTrue("Path found is of length " + result.getPath().length + " but should be of length 3", result.getPath().length == 3);
		assertTrue("Path should be s-2-6-t", result.getPath()[0] == sn2 && result.getPath()[1] == n2n6 && result.getPath()[2] == n6t);*/
	}
}
