package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.scrc;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class SCRCAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {

    @Before
    public void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest =new SCRCAlgorithm(controller);
	    routingAlgorithmUnderTest.setProxy(proxy);
    }
}
