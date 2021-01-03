package de.tum.ei.lkn.eces.routing.distancevector;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceVectorSystemTest {
	@Test
	public void hopCountTest() {
		Controller controller = new Controller();
		GraphSystem graphSystem = new GraphSystem(controller);
		DistanceVectorSystem distanceVectorSystem = new DistanceVectorSystem(controller);

		Mapper<DistanceVector> distanceVectorMapper = new DistanceVectorMapper(controller);

		Graph graph = graphSystem.createGraph();
		Node[] nodes = new Node[10];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = graphSystem.createNode(graph);
		for(int i = 1; i < nodes.length; i++)
			graphSystem.createEdge(nodes[i-1], nodes[i]);

		distanceVectorSystem.update(graph);

		DistanceVector vector = distanceVectorMapper.get(nodes[nodes.length -1].getEntity());

		for(int i = 0; i < nodes.length; i++)
			assertEquals(vector.getDistance(nodes[i]), 9 - i, 0.0);
	}
}
