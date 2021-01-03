package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sfdclc;

import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

public class SFDCLCBDAlgorithmTest extends SFDCLCAlgorithmTest {
    @Before
    public final void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest = new SFDCLCAlgorithm(controller);
	    routingAlgorithmUnderTest.setProxy(proxy);
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new SFDCLCAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
    }
}
