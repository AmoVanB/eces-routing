package de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.MapperSpace;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.RoutingSystem;
import de.tum.ei.lkn.eces.routing.SelectedRoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint.simple.SDAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.disjoint.simplepartial.SPDAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.mappers.DisjointPathsMapper;
import de.tum.ei.lkn.eces.routing.mappers.SelectedRoutingAlgorithmMapper;
import de.tum.ei.lkn.eces.routing.mappers.UnicastRequestMapper;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ShortestPathProxy;
import de.tum.ei.lkn.eces.routing.requests.DisjointRequest;
import de.tum.ei.lkn.eces.routing.responses.DisjointPaths;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DisjointAlgorithmsTest {
	private Controller controller;
	private SDAlgorithm disjointAlgorithm;
	private SPDAlgorithm partialAlgorithm;
	private Node a, b, c, d, h;
	private Edge ab, ba, bc, cb, cd, dc, da, ad, ah, ha;

	@Before
	public void setup() {
		controller = new Controller();
		RoutingSystem routingSystem = new RoutingSystem(controller);
		EdgeProxy proxy = new ShortestPathProxy();
		disjointAlgorithm = new SDAlgorithm(controller, new DijkstraAlgorithm(controller));
		partialAlgorithm = new SPDAlgorithm(controller, new DijkstraAlgorithm(controller));
		disjointAlgorithm.setProxy(proxy);
		partialAlgorithm.setProxy(proxy);

		// Create topology: ring of 4 nodes (a,b,c,d) and 1 host (h) connected to a
		GraphSystem graphSystem = new GraphSystem(controller);

		Graph topology = graphSystem.createGraph();
		a = graphSystem.createNode(topology);
		b = graphSystem.createNode(topology);
		c = graphSystem.createNode(topology);
		d = graphSystem.createNode(topology);
		h = graphSystem.createNode(topology);
		ab = graphSystem.createEdge(a, b);
		ba = graphSystem.createEdge(b, a);
		bc = graphSystem.createEdge(b, c);
		cb = graphSystem.createEdge(c, b);
		cd = graphSystem.createEdge(c, d);
		dc = graphSystem.createEdge(d, c);
		da = graphSystem.createEdge(d, a);
		ad = graphSystem.createEdge(a, d);
		ah = graphSystem.createEdge(a, h);
		ha = graphSystem.createEdge(h, a);
	}

	@Test
	public final void failDisjoint() {
		// Disjoint from H to C and D should fail (one common link)
		Entity entity = controller.createEntity();
		try(MapperSpace ms = controller.startMapperSpace()) {
			List<Node> destinations = new LinkedList<>();
			destinations.add(c);
			destinations.add(d);
			new UnicastRequestMapper(controller).attachComponent(entity, new DisjointRequest(h, destinations));
			new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(disjointAlgorithm));
		}
		DisjointPaths result = new DisjointPathsMapper(controller).get(entity);
		assertTrue("There should not be a possible solution but algorithm found " + result, result == null);
	}

	@Test
	public final void successPartialDisjoint() {
		// Disjoint from H to C and D should success with partial algorithm (one common link)
		Entity entity = controller.createEntity();
		try(MapperSpace ms = controller.startMapperSpace()) {
			List<Node> destinations = new LinkedList<>();
			destinations.add(c);
			destinations.add(d);
			new UnicastRequestMapper(controller).attachComponent(entity, new DisjointRequest(h, destinations));
			new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(partialAlgorithm));
		}
		DisjointPaths result = new DisjointPathsMapper(controller).get(entity);
		assertTrue("There should be a solution", result != null);
		Path path1 = (Path) result.getPaths().toArray()[0];
		Path path2 = (Path) result.getPaths().toArray()[1];
		assertTrue("First path should be H-A-B-C but is " + path1,
				path1.getPath().length == 3 &&
		path1.getPath()[0] == ha &&
		path1.getPath()[1] == ab &&
		path1.getPath()[2] == bc);
		assertTrue("Second path should be H-A-D but is " + path2,
				path2.getPath().length == 2 &&
		path2.getPath()[0] == ha &&
		path2.getPath()[1] == ad);
	}

	@Test
	public final void successDisjoint() {
		// Disjoint from A to C and D should success
		Entity entity = controller.createEntity();
		try(MapperSpace ms = controller.startMapperSpace()) {
			List<Node> destinations = new LinkedList<>();
			destinations.add(c);
			destinations.add(d);
			new UnicastRequestMapper(controller).attachComponent(entity, new DisjointRequest(a, destinations));
			new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(disjointAlgorithm));
		}

		DisjointPaths result = new DisjointPathsMapper(controller).get(entity);
		assertTrue("There should be a solution", result != null);
		Path path1 = (Path) result.getPaths().toArray()[0];
		Path path2 = (Path) result.getPaths().toArray()[1];
		assertTrue("First path should be A-B-C but is " + path1,
				path1.getPath().length == 2 &&
						path1.getPath()[0] == ab &&
						path1.getPath()[1] == bc);
		assertTrue("Second path should be A-D but is " + path2,
				path2.getPath().length == 1 &&
						path2.getPath()[0] == ad);
	}

	@Test
	public final void successPartialOfCourse() {
		// Disjoint from A to C and D should success
		Entity entity = controller.createEntity();
		try(MapperSpace ms = controller.startMapperSpace()) {
			List<Node> destinations = new LinkedList<>();
			destinations.add(c);
			destinations.add(d);
			new UnicastRequestMapper(controller).attachComponent(entity, new DisjointRequest(a, destinations));
			new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(partialAlgorithm));
		}

		DisjointPaths result = new DisjointPathsMapper(controller).get(entity);
		assertTrue("There should be a solution", result != null);
		Path path1 = (Path) result.getPaths().toArray()[0];
		Path path2 = (Path) result.getPaths().toArray()[1];
		assertTrue("First path should be A-B-C but is " + path1,
				path1.getPath().length == 2 &&
						path1.getPath()[0] == ab &&
						path1.getPath()[1] == bc);
		assertTrue("Second path should be A-D but is " + path2,
				path2.getPath().length == 1 &&
						path2.getPath()[0] == ad);
	}
}
