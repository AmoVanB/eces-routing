package de.tum.ei.lkn.eces.routing.algorithms.sp.ksp.yen;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.KSPAlgorithmTest;
import org.junit.Before;

public class YenAlgorithmTest extends KSPAlgorithmTest {
	@Before
	public final void setupAlgorithm() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);
		routingAlgorithmUnderTest =new YenAlgorithm(controller);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
