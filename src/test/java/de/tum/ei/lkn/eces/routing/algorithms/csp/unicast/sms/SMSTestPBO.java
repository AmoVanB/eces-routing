package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sms;

import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.ConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

/**
 * Test class for the SMS Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */

public class SMSTestPBO extends ConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() throws Exception {
		super.setUp();
		routingAlgorithmUnderTest = new SMSAlgorithm(controller, PreferredLinkMode.PARTITION_BASED_ORDERING, Integer.MAX_VALUE);
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);
		routingAlgorithmUnderTest.setProxy(proxy);
	}
}
