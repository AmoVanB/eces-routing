package de.tum.ei.lkn.eces.routing.algorithms.mcsp.fb;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class FallbackAlgorithmTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);

		routingAlgorithmUnderTest = new FBAlgorithm(controller);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
