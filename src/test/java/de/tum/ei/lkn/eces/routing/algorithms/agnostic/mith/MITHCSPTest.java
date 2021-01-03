package de.tum.ei.lkn.eces.routing.algorithms.agnostic.mith;

import de.tum.ei.lkn.eces.routing.algorithms.agnostic.in.mith.MITHAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.in.mhlarac.INLARACAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.larac.LARACAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.INAlgorithmTest;
import org.junit.Before;

public class MITHCSPTest extends INAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);

		routingAlgorithmUnderTest = new MITHAlgorithm(controller, new LARACAlgorithm(controller));
		routingAlgorithmUnderTest.setProxy(proxy);
		referenceINAlgorithm = new INLARACAlgorithm(controller);
		referenceINAlgorithm.setProxy(proxy);
	}
}
