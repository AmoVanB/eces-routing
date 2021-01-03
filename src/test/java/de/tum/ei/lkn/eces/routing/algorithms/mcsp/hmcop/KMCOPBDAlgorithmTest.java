package de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

public class KMCOPBDAlgorithmTest extends KMCOPAlgorithmTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);

		RoutingAlgorithm algorithm = new HMCOPAlgorithm(controller, Double.POSITIVE_INFINITY, 5);
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new HMCOPAlgorithm(controller, Double.POSITIVE_INFINITY, 5);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
	}
}
