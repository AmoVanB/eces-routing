package de.tum.ei.lkn.eces.routing.algorithms.agnostic.mith;

import de.tum.ei.lkn.eces.routing.algorithms.agnostic.in.mith.MITHAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.in.SimpleINSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.INAlgorithmTest;
import org.junit.Before;

public class MITHSPTest extends INAlgorithmTest {
	@Before
	public void setUp() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[0],
				new int[0]);

		routingAlgorithmUnderTest = new MITHAlgorithm(controller, new AStarAlgorithm(controller));
		routingAlgorithmUnderTest.setProxy(proxy);
		referenceINAlgorithm = new SimpleINSPAlgorithm(controller);
		referenceINAlgorithm.setProxy(proxy);
	}
}
