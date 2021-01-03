package de.tum.ei.lkn.eces.routing.util;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.KSPAlgorithm;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Base test class for kSP algorithms algorithm.
 * Tests aren't run here but in the subclasses specific to each algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class KSPAlgorithmTest extends BaseTest {

	@Test
	public final void simpleShortestPathTest() {
		createBaseTopology();
		proxy.setProxy(new DummyEdgeProxy(controller));

		UnicastRequest request = new UnicastRequest(nodes[0], nodes[2]);

		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);

		assertTrue(routingAlgorithmUnderTest + ": No path found", ksp.hasNext());
		Path path = ksp.next();
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 2);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[0]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[1]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 2 but is " + path.getCost(), path.getCost() == 2);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));

		assertTrue(routingAlgorithmUnderTest + ": No path found", ksp.hasNext());
		path = ksp.next();
		assertTrue(routingAlgorithmUnderTest + ": Hop count wrong", path.getPath().length == 4);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[0] == edges[8]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[1] == edges[4]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[2] == edges[5]);
		assertTrue(routingAlgorithmUnderTest + ": Wrong edge used", path.getPath()[3] == edges[9]);
		assertTrue(routingAlgorithmUnderTest + ": Cost should be 4 but is " + path.getCost(), path.getCost() == 4);
		assertTrue(routingAlgorithmUnderTest + ": Proxy detects invalid path ", proxy.isValid(path,request));

		assertTrue(routingAlgorithmUnderTest + ": There is no path", !ksp.hasNext());
	}

	@Test
	public final void runAllPaths33() {
		createGridTopology(3,3);
		proxy.setProxy(new DummyEdgeProxy(controller));
		UnicastRequest request = new UnicastRequest(nodes[0], nodes[nodes.length-1]);

		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);
		int count = 0;
		LinkedList<Path> pathes = new LinkedList<>();
		while(ksp.hasNext()){
			Path path = ksp.next();
			for(Path p :pathes){
				assertFalse("There are equal paths " + path + " - " + p,path.equals(p));
			}
			pathes.add(path);
			count++;
		}
		System.out.println("Number of paths: " + count);
	}

	@Test
	public final void runAllPaths44() {
		createGridTopology(4,4);
		proxy.setProxy(new DummyEdgeProxy(controller));
		UnicastRequest request = new UnicastRequest(nodes[0], nodes[nodes.length-1]);

		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);
		int count = 0;
		LinkedList<Path> pathes = new LinkedList<>();
		while(ksp.hasNext()){
			Path path = ksp.next();
			for(Path p :pathes){
				assertFalse("There are equal paths " + path + " - " + p,path.equals(p));
			}
			pathes.add(path);
			count++;
		}
		System.out.println("Number of paths: " + count);
	}

	@Test
	public final void runAllPaths54() {
		createGridTopology(5,4);
		proxy.setProxy(new DummyEdgeProxy(controller));
		UnicastRequest request = new UnicastRequest(nodes[0], nodes[nodes.length-1]);

		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);
		int count = 0;
		LinkedList<Path> pathes = new LinkedList<>();
		while(ksp.hasNext()){
			Path path = ksp.next();
			for(Path p :pathes){
				assertFalse("There are equal paths " + path + " - " + p,path.equals(p));
			}
			pathes.add(path);
			count++;
		}
		System.out.println("Number of paths: " + count);
	}
	@Test
	public final void runAllPaths55Runtime() {
		createGridTopology(5,5);
		proxy.setProxy(new DummyEdgeProxy(controller));
		UnicastRequest request = new UnicastRequest(nodes[0], nodes[nodes.length-1]);

		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);
		int count = 0;
		while(ksp.hasNext()){
			Path path = ksp.next();
			count++;
		}
		System.out.println("Number of paths: " + count);
	}

	@Test
	public final void YenWikipediaTest() {
		Graph graph = graphSystem.createGraph();
		Node c = graphSystem.createNode(graph);
		Node d = graphSystem.createNode(graph);
		Node e = graphSystem.createNode(graph);
		Node f = graphSystem.createNode(graph);
		Node g = graphSystem.createNode(graph);
		Node h = graphSystem.createNode(graph);

		Edge cd = graphSystem.createEdge(c, d);
		dummyMapper.attachComponent(cd, new DummyComponent(3, 5, 1, true, 0));
		Edge ce = graphSystem.createEdge(c, e);
		dummyMapper.attachComponent(ce, new DummyComponent(2, 5, 1, true, 0));
		Edge df = graphSystem.createEdge(d, f);
		dummyMapper.attachComponent(df, new DummyComponent(4, 5, 1, true, 0));
		Edge ed = graphSystem.createEdge(e, d);
		dummyMapper.attachComponent(ed, new DummyComponent(1, 5, 1, true, 0));
		Edge ef = graphSystem.createEdge(e, f);
		dummyMapper.attachComponent(ef, new DummyComponent(2, 5, 1, true, 0));
		Edge eg = graphSystem.createEdge(e, g);
		dummyMapper.attachComponent(eg, new DummyComponent(3, 5, 1, true, 0));
		Edge fg = graphSystem.createEdge(f, g);
		dummyMapper.attachComponent(fg, new DummyComponent(2, 5, 1, true, 0));
		Edge fh = graphSystem.createEdge(f, h);
		dummyMapper.attachComponent(fh, new DummyComponent(1, 5, 1, true, 0));
		Edge gh = graphSystem.createEdge(g, h);
		dummyMapper.attachComponent(gh, new DummyComponent(2, 5, 1, true, 0));

		proxy.setProxy(new DummyEdgeProxy(controller));
		UnicastRequest request = new UnicastRequest(c, h);
		Iterator<Path> ksp = ((KSPAlgorithm)routingAlgorithmUnderTest).iterator(request);
		Path k1 = ksp.next();
		Path k2 = ksp.next();
		Path k3 = ksp.next();

		assertTrue("KSP Algorithm should be able to find 3 paths", k1 != null && k2 != null && k3 != null);

		// k1
		assertTrue("1st path found has cost of " + k1.getCost() + " but should have a cost of 5", k1.getCost() == 5);
		assertTrue("1st path found is of length " + k1.getPath().length + " but should be of length 3", k1.getPath().length == 3);
		assertTrue("1st path should be c-e-f-h", k1.getPath()[0] == ce && k1.getPath()[1] == ef && k1.getPath()[2] == fh);
		// k2
		assertTrue("2nd path found has cost of " + k2.getCost() + " but should have a cost of 7", k2.getCost() == 7);
		assertTrue("2nd path found is of length " + k2.getPath().length + " but should be of length 3", k2.getPath().length == 3);
		assertTrue("2nd path should be c-e-g-h", k2.getPath()[0] == ce && k2.getPath()[1] == eg && k2.getPath()[2] == gh);
		// k3 (3 possibilities)
		if (k3.getPath().length == 3) {
			assertTrue("3rd path found has cost of " + k3.getCost() + " but should have a cost of 8", k3.getCost() == 8);
			assertTrue("3rd path found is of length " + k3.getPath().length + " but should be of length 3", k3.getPath().length == 3);
			assertTrue("3rd path should be c-d-f-h", k3.getPath()[0] == cd && k3.getPath()[1] == df && k3.getPath()[2] == fh);
		}
		else {
			assertTrue("3rd path found has cost of " + k3.getCost() + " but should have a cost of 8", k3.getCost() == 8);
			assertTrue("3rd path found is of length " + k3.getPath().length + " but should be of length 4", k3.getPath().length == 4);
			assertTrue("3rd path should be c-e-f-g-h or c-e-d-f-h", k3.getPath()[0] == ce && (k3.getPath()[1] == ef || k3.getPath()[1] == ed) && (k3.getPath()[2] == fg || k3.getPath()[2] == df) && (k3.getPath()[3] == gh || k3.getPath()[3] == fh));
		}
	}
}
