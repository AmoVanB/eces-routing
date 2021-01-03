package de.tum.ei.lkn.eces.routing.algorithms.sp.in;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.INAlgorithmTest;
import org.junit.Before;

public class SimpleINAlgorithmTest extends INAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);
		routingAlgorithmUnderTest = new SimpleINSPAlgorithm(controller);
		routingAlgorithmUnderTest.setProxy(proxy);
		referenceINAlgorithm = new SimpleINSPAlgorithm(controller);
		referenceINAlgorithm.setProxy(proxy);
	}
}
