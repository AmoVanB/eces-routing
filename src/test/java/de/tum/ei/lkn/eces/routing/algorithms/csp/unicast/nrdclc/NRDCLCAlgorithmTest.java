package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.nrdclc;

import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.ShortestPathProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class NRDCLCAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		DijkstraAlgorithm da = new DijkstraAlgorithm(controller);
		da.setProxy(new ShortestPathProxy());
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest = new NRDCLCAlgorithm(controller, new DijkstraAlgorithm(controller));
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
