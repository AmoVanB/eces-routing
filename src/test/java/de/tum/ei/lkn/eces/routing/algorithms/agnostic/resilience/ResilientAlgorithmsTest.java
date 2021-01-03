package de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.MapperSpace;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.RoutingSystem;
import de.tum.ei.lkn.eces.routing.SelectedRoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.partial.SPRAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.resilience.simple.SRAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.mappers.ResilientPathMapper;
import de.tum.ei.lkn.eces.routing.mappers.SelectedRoutingAlgorithmMapper;
import de.tum.ei.lkn.eces.routing.mappers.UnicastRequestMapper;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ShortestPathProxy;
import de.tum.ei.lkn.eces.routing.requests.ResilientRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.ResilientPath;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResilientAlgorithmsTest {
    private Controller controller;
    private SRAlgorithm resilientAlgorithm;
    private SPRAlgorithm partialAlgorithm;
    private Node a, b, c, d, h;
    private Edge ab, ba, bc, cb, cd, dc, da, ad, ah, ha;

    @Before
    public void setup() {
        controller = new Controller();
        RoutingSystem routingSystem = new RoutingSystem(controller);
        EdgeProxy proxy = new ShortestPathProxy();
        resilientAlgorithm = new SRAlgorithm(controller, new DijkstraAlgorithm(controller));
        partialAlgorithm = new SPRAlgorithm(controller, new DijkstraAlgorithm(controller));
        resilientAlgorithm.setProxy(proxy);
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
    public final void failResilient() {
        // Resilient from H to C (one common link)
        Entity entity = controller.createEntity();
        try(MapperSpace ms = controller.startMapperSpace()) {
            new UnicastRequestMapper(controller).attachComponent(entity, new ResilientRequest(h, c));
            new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(resilientAlgorithm));
        }

        ResilientPath result = new ResilientPathMapper(controller).get(entity);
        assertTrue("There should not be a possible solution but algorithm found " + result, result == null);
    }

    @Test
    public final void successPartialResilient() {
        // Resilient from H to C should success with partial algorithm (one common link)
        Entity entity = controller.createEntity();
        try(MapperSpace ms = controller.startMapperSpace()) {
            new UnicastRequestMapper(controller).attachComponent(entity, new ResilientRequest(h, c));
            new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(partialAlgorithm));
        }

        ResilientPath result = new ResilientPathMapper(controller).get(entity);
        assertTrue("There should be a solution", result != null);
        Path path1 = result.getPath1();
        Path path2 = result.getPath2();
        assertTrue("First path should be H-A-B-C but is " + path1,
                path1.getPath().length == 3 &&
        path1.getPath()[0] == ha &&
        path1.getPath()[1] == ab &&
        path1.getPath()[2] == bc);
        assertTrue("Second path should be H-A-D-C but is " + path2,
                path2.getPath().length == 3 &&
        path2.getPath()[0] == ha &&
        path2.getPath()[1] == ad &&
        path2.getPath()[2] == dc);
    }

    @Test
    public final void successResilient() {
        // Resilient from A to C
        Entity entity = controller.createEntity();
        try(MapperSpace ms = controller.startMapperSpace()) {
            new UnicastRequestMapper(controller).attachComponent(entity, new ResilientRequest(a, c));
            new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(resilientAlgorithm));
        }

        ResilientPath result = new ResilientPathMapper(controller).get(entity);
        assertTrue("There should be a solution", result != null);
        Path path1 = result.getPath1();
        Path path2 = result.getPath2();
        assertTrue("First path should be A-B-C but is " + path1,
                path1.getPath().length == 2 &&
                        path1.getPath()[0] == ab &&
                        path1.getPath()[1] == bc);
        assertTrue("Second path should be A-D-C but is " + path2,
                path2.getPath().length == 2 &&
                        path2.getPath()[0] == ad &&
                        path2.getPath()[1] == dc);
    }

    @Test
    public final void successPartialOfCourse() {
        // Disjoint from A to C
        Entity entity = controller.createEntity();
        try(MapperSpace ms = controller.startMapperSpace()) {
            new UnicastRequestMapper(controller).attachComponent(entity, new ResilientRequest(a, c));
            new SelectedRoutingAlgorithmMapper(controller).attachComponent(entity, new SelectedRoutingAlgorithm(partialAlgorithm));
        }

        ResilientPath result = new ResilientPathMapper(controller).get(entity);
        assertTrue("There should be a solution", result != null);
        Path path1 = result.getPath1();
        Path path2 = result.getPath2();
        assertTrue("First path should be A-B-C but is " + path1,
                path1.getPath().length == 2 &&
                        path1.getPath()[0] == ab &&
                        path1.getPath()[1] == bc);
        assertTrue("Second path should be A-D-C but is " + path2,
                path2.getPath().length == 2 &&
                        path2.getPath()[0] == ad &&
                        path2.getPath()[1] == dc);
    }
}
