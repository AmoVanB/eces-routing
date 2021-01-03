package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcbf;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class KDCBFAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest =new DCBFAlgorithm(controller,4);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
