package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.iak;

import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;

public class IAKBDTest extends IAKTest {
	@Override
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new IAKAlgorithm(controller);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new IAKAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
	}
}
