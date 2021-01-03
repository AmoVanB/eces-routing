package de.tum.ei.lkn.eces.routing;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.core.MapperSpace;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.cbf.CBFAlgorithm;
import de.tum.ei.lkn.eces.routing.mappers.PathListMapper;
import de.tum.ei.lkn.eces.routing.mappers.PathMapper;
import de.tum.ei.lkn.eces.routing.mappers.SelectedRoutingAlgorithmMapper;
import de.tum.ei.lkn.eces.routing.mappers.UnicastRequestMapper;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponent;
import de.tum.ei.lkn.eces.routing.mocks.DummyComponentMapper;
import de.tum.ei.lkn.eces.routing.mocks.DummyEdgeProxy;
import de.tum.ei.lkn.eces.routing.pathlist.PathList;
import de.tum.ei.lkn.eces.routing.pathlist.PathListSystem;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class PathListTest {
    protected Controller controller;
    private GraphSystem gs;
    private Node[] n;
    protected Edge[] e;
    protected Mapper<DummyComponent> dummyMapper;
    private Mapper<Path> edgePathMapper;
    private Mapper<UnicastRequest> requestMapper;
    private Mapper<PathList> pathListMapper;
    private RoutingAlgorithm ra;
    private Mapper<SelectedRoutingAlgorithm> selectedRoutingAlgorithmMapper;

    @Before
    public void setUp() throws Exception {
        controller = new Controller();
        gs   = new GraphSystem(controller);
        selectedRoutingAlgorithmMapper = new SelectedRoutingAlgorithmMapper(controller);
        dummyMapper    = new DummyComponentMapper(controller);
        edgePathMapper = new PathMapper(controller);
        requestMapper  = new UnicastRequestMapper(controller);
        pathListMapper = new PathListMapper(controller);
        PathListSystem pls = new PathListSystem(controller);
        RoutingSystem routingSystem = new RoutingSystem(controller);
	    ra = new CBFAlgorithm(controller);
	    ra.setProxy(new DummyEdgeProxy(controller));

        Graph graph = gs.createGraph();

        n = new Node[8];
        for(int i = 0; i <n.length; i++){
            n[i] = gs.createNode(graph);
        }
        e = new Edge[10];
        e[0] =  gs.createEdge(n[0], n[1]);
        e[1] =  gs.createEdge(n[1], n[2]);
        e[2] =  gs.createEdge(n[2], n[3]);
        e[3] =  gs.createEdge(n[3], n[0]);

        e[4] =  gs.createEdge(n[4], n[5]);
        e[5] =  gs.createEdge(n[5], n[6]);
        e[6] =  gs.createEdge(n[6], n[7]);
        e[7] =  gs.createEdge(n[7], n[4]);

        e[8] =  gs.createEdge(n[0], n[4]);
        e[9] =  gs.createEdge(n[6], n[2]);

        for(Edge edge: e)
            dummyMapper.attachComponent(edge, new DummyComponent());
    }

    @Test
    public void pathListAttachmentTest() {
        for(Edge edge: e){
            assertTrue("PathList is missing", this.pathListMapper.isIn(edge.getEntity()));
            assertTrue("PathList is missing", this.pathListMapper.isIn(edge.getSource().getGraph().getEntity()));
        }
    }

    @Test
    public void pathAttachmentTest() {
        Entity ent = controller.createEntity();

        try(MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent, new UnicastRequest(n[0], n[2]));
            this.dummyMapper.attachComponent(ent, new DummyComponent());
        }

        Path myPath = this.edgePathMapper.get(ent);
        Set<Edge> used = new HashSet<>();
        Collections.addAll(used, myPath.getPath());

        for(Edge edge: this.e) {
            if(used.contains(edge)) {
                PathList pl = this.pathListMapper.get(edge.getEntity());
                Assert.assertTrue("PathList should contain one path" , pl.getPathList().contains(myPath.getEntity()) && pl.getPathList().size() == 1);

                // Graph
                pl = this.pathListMapper.get(edge.getDestination().getGraph().getEntity());
                Assert.assertTrue("PathList should contain one path" , pl.getPathList().contains(myPath.getEntity()) && pl.getPathList().size() == 1);
            }
            else {
                PathList pl = this.pathListMapper.get(edge.getEntity());
                Assert.assertEquals("PathList should contain no path", 0, pl.getPathList().size());
            }
        }
    }
    @Test
    public void pathDetachmentTest() {
        Entity ent = controller.createEntity();

        try(MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent, new UnicastRequest(n[0], n[2]));
            this.dummyMapper.attachComponent(ent, new DummyComponent());
        }
        this.edgePathMapper.detachComponent(ent);
        for(Edge edge: this.e) {
            PathList pl = this.pathListMapper.get(edge.getEntity());
            Assert.assertEquals("PathList should contain no path", 0, pl.getPathList().size());
            // Graph
            pl = this.pathListMapper.get(edge.getDestination().getGraph().getEntity());
            Assert.assertEquals("PathList should contain no path", 0, pl.getPathList().size());
        }
    }

    @Test
    public void pathListDetachmentTest() {
        Entity ent = controller.createEntity();

        try(MapperSpace ms = controller.startMapperSpace()) {
            this.selectedRoutingAlgorithmMapper.attachComponent(ent, new SelectedRoutingAlgorithm(ra));
            this.requestMapper.attachComponent(ent, new UnicastRequest(n[0], n[2]));
            this.dummyMapper.attachComponent(ent, new DummyComponent());
        }
        Path myPath = this.edgePathMapper.get(ent);

		this.gs.deleteEdge(myPath.getPath()[0]);

        Assert.assertFalse(pathListMapper.isIn(myPath.getPath()[0].getEntity()));

		for(Edge edge : e) {
			if(edge != myPath.getPath()[0]) {
				assertTrue("PathList is missing", this.pathListMapper.isIn(edge.getEntity()));
                assertEquals("PathList should contain no path but contains " + this.pathListMapper.get(edge.getEntity()).getPathList().size(), 0, this.pathListMapper.get(edge.getEntity()).getPathList().size());

                // Graph
                PathList pl = this.pathListMapper.get(edge.getDestination().getGraph().getEntity());
                Assert.assertEquals("PathList should contain no path", 0, pl.getPathList().size());
			}
		}
    }
}
