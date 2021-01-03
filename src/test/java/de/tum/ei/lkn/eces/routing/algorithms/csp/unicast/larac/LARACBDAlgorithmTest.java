package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac;

import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.ShortestPathProxy;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

public class LARACBDAlgorithmTest extends LARACAlgorithmTest {

    @Before
    public final void setupAlgorithm() throws Exception {
	    super.setUp();
	    DijkstraAlgorithm da = new DijkstraAlgorithm(controller);
	    da.setProxy(new ShortestPathProxy());
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest =new LARACAlgorithm(controller);
	    ((LARACAlgorithm)routingAlgorithmUnderTest).enableBD();
	    routingAlgorithmUnderTest.setProxy(proxy);

		referenceIdenticalAlgorithm = new LARACAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
    }
}
