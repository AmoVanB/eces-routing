package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.meb;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class MEBAlgorithmTest extends ConstrainedShortestPathAlgorithmTest {

    @Before
    public final void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest =new MEBAlgorithm(controller);
	    routingAlgorithmUnderTest.setProxy(proxy);
    }
}
