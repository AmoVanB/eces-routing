package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;


import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ShortestPathAlgorithmTest;
import org.junit.Before;

public class uqpaReverseEdgeBasedDijkstraTest extends ShortestPathAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();

		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);
		routingAlgorithmUnderTest =new UniversalPriorityQueueAlgorithm(controller, QueueMode.EDGE,false);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
