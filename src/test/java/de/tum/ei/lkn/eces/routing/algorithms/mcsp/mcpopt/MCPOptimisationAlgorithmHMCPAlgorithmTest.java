package de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.hmcp.HMCPAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class MCPOptimisationAlgorithmHMCPAlgorithmTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);

		RoutingAlgorithm algorithm = new MCPOptimisationAlgorithm(controller, new HMCPAlgorithm(controller, false));
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
	}

}
