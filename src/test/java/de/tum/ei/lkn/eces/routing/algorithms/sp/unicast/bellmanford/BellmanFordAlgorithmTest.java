package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford;


import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.easygraph.EasyGraphProxy;
import de.tum.ei.lkn.eces.routing.easygraph.Metrics;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.util.ShortestPathAlgorithmTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class BellmanFordAlgorithmTest extends ShortestPathAlgorithmTest {
    @Before
    public void setUp() {
        super.setUp();
        proxy = new PathPlumberProxy(new int[]{0},
                new double[]{1},
                new int[0],
                new int[0]);
        routingAlgorithmUnderTest =new BellmanFordAlgorithm(controller);
        routingAlgorithmUnderTest.setProxy(proxy);
    }

    @Test
    public final void fig2point1BhandariBookP27() {
        EasyGraphProxy dp = new EasyGraphProxy(controller, new double[]{15, 15, 15});
        proxy.setProxy(dp);
        assumeTrue("Algorithm cannot solve the edge problem optimal --> Skip test", routingAlgorithmUnderTest.isOptimal());

        Graph graph = graphSystem.createGraph();
        Node A  = graphSystem.createNode(graph);
        Node B  = graphSystem.createNode(graph);
        Node C  = graphSystem.createNode(graph);
        Node D  = graphSystem.createNode(graph);
        Node E  = graphSystem.createNode(graph);
        Node Z  = graphSystem.createNode(graph);

        // Creating Edges and assigning costs and delays
        Edge AB  = graphSystem.createEdge(A, B);
        metricsMapper.attachComponent(AB, new Metrics(5, new double[]{0, 0, 0}));
        Edge BA  = graphSystem.createEdge(B, A);
        metricsMapper.attachComponent(BA, new Metrics(5, new double[]{0, 0, 0}));
        Edge CA  = graphSystem.createEdge(C, A);
        metricsMapper.attachComponent(CA, new Metrics(-1, new double[]{0, 0, 0}));
        Edge AD  = graphSystem.createEdge(A, D);
        metricsMapper.attachComponent(AD, new Metrics(7, new double[]{0, 0, 0}));
        Edge DA  = graphSystem.createEdge(D, A);
        metricsMapper.attachComponent(DA, new Metrics(7, new double[]{0, 0, 0}));
        Edge CB  = graphSystem.createEdge(C, B);
        metricsMapper.attachComponent(CB, new Metrics(1, new double[]{0, 0, 0}));
        Edge BC  = graphSystem.createEdge(B, C);
        metricsMapper.attachComponent(BC, new Metrics(1, new double[]{0, 0, 0}));
        Edge ZB  = graphSystem.createEdge(Z, B);
        metricsMapper.attachComponent(ZB, new Metrics(8, new double[]{0, 0, 0}));
        Edge BZ  = graphSystem.createEdge(B, Z);
        metricsMapper.attachComponent(BZ, new Metrics(8, new double[]{0, 0, 0}));
        Edge EC  = graphSystem.createEdge(E, C);
        metricsMapper.attachComponent(EC, new Metrics(-6, new double[]{0, 0, 0}));
        Edge DZ  = graphSystem.createEdge(D, Z);
        metricsMapper.attachComponent(DZ, new Metrics(6, new double[]{0, 0, 0}));
        Edge ZD  = graphSystem.createEdge(Z, D);
        metricsMapper.attachComponent(ZD, new Metrics(6, new double[]{0, 0, 0}));
        Edge DE  = graphSystem.createEdge(D, E);
        metricsMapper.attachComponent(DE, new Metrics(2, new double[]{0, 0, 0}));
        Edge ED  = graphSystem.createEdge(E, D);
        metricsMapper.attachComponent(ED, new Metrics(2, new double[]{0, 0, 0}));
        Edge ZE  = graphSystem.createEdge(Z, E);
        metricsMapper.attachComponent(ZE, new Metrics(-2, new double[]{0, 0, 0}));

        Path result = (Path) routingAlgorithmUnderTest.solve(new UnicastRequest(A, Z));

        // Path should have a cost of 12.
        assertTrue(routingAlgorithmUnderTest + ": No path found", result != null);
        assertTrue("Path found has cost of " + result.getCost() + " but should have a cost of 12", result.getCost() == 12);

        // Path should be: ADECBZ
        assertTrue("Path found is of length " + result.getPath().length + " but should be of length 5", result.getPath().length == 5);
        assertTrue("Path should be ABCZ", result.getPath()[0] == AD && result.getPath()[1] == DE && result.getPath()[2] == EC && result.getPath()[3] == CB  && result.getPath()[4] == BZ);
    }
}
