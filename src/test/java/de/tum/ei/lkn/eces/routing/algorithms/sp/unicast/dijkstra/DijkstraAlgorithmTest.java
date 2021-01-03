package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra;


import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ShortestPathAlgorithmTest;
import org.junit.Before;

public class DijkstraAlgorithmTest extends ShortestPathAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);
		routingAlgorithmUnderTest = new DijkstraAlgorithm(controller);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
