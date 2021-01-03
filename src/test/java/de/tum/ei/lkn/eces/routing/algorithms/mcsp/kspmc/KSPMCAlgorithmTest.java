package de.tum.ei.lkn.eces.routing.algorithms.mcsp.kspmc;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class KSPMCAlgorithmTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);


		RoutingAlgorithm algorithm = new KSPMCAlgorithm(controller);
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
	}
}
