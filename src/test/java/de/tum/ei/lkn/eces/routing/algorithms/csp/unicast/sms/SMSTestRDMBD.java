package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.sms;

import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import org.junit.Before;

/**
 * Test class for the DCR Algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SMSTestRDMBD extends SMSTestRDM {
	@Before
	public final void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1},
				new int[0]);

		routingAlgorithmUnderTest = new SMSAlgorithm(controller, PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING, Integer.MAX_VALUE);
		routingAlgorithmUnderTest.setProxy(proxy);
		((BDifiable) routingAlgorithmUnderTest).enableBD();

		referenceIdenticalAlgorithm = new SMSAlgorithm(controller, PreferredLinkMode.RESIDUAL_DELAY_MAXIMIZING, Integer.MAX_VALUE);
		referenceIdenticalAlgorithm.setProxy(proxy);
		((BDifiable) referenceIdenticalAlgorithm).disableBD();
	}
}
