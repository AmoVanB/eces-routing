package de.tum.ei.lkn.eces.routing.algorithms.mcsp.mcpopt;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.mcp.emcp.EMCPAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class MCPOptimisationAlgorithmEMCPAlgorithmBSTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);

		RoutingAlgorithm algorithm = new MCPOptimisationAlgorithm(controller, new EMCPAlgorithm(controller));
		((MCPOptimisationAlgorithm)algorithm).enableBinarySearch();
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
	}

}