package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcur;

import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

/**
 * Test class for the DCUR Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DCURwBDTest extends DCURTest {
	@Before
	public void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new DCURAlgorithm(controller);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new DCURAlgorithm(controller);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
	}
}
