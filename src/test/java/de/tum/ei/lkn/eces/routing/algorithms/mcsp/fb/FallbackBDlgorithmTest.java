package de.tum.ei.lkn.eces.routing.algorithms.mcsp.fb;

import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

public class FallbackBDlgorithmTest extends FallbackAlgorithmTest {
	@Before
	public final void setupAlgorithm() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);

		routingAlgorithmUnderTest = new FBAlgorithm(controller);
		routingAlgorithmUnderTest.setProxy(proxy);
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new FBAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
	}
}
