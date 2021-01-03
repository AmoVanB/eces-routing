package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class AStarPruneTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);
		SPAlgorithm[] spAlgorithms = new SPAlgorithm[4];
		for(int i = 0; i < spAlgorithms.length; i++) {
			spAlgorithms[i] = new AStarAlgorithm(controller);
		}

		RoutingAlgorithm algorithm = new AStarPruneAlgorithm(controller, spAlgorithms);
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
	}
}
