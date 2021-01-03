package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac;

import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

public class LARACAlgorithmUPQApeModeTest extends LARACAlgorithmTest {

    @Before
    public final void setupAlgorithm() throws Exception {
	    super.setUp();
	    proxy = new PathPlumberProxy(new int[]{0},
			    new double[]{1},
			    new int[]{1},
			    new int[0]);
	    routingAlgorithmUnderTest =new LARACAlgorithm(controller,new UniversalPriorityQueueAlgorithm(controller, QueueMode.EDGE,true));
	    routingAlgorithmUnderTest.setProxy(proxy);
		referenceIdenticalAlgorithm = new LARACAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
    }
}
