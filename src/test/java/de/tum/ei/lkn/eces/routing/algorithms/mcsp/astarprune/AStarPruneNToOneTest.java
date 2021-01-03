package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.util.MultipleConstrainedShortestPathAlgorithmTest;
import org.junit.Before;

public class AStarPruneNToOneTest extends MultipleConstrainedShortestPathAlgorithmTest {
	@Before
	public final void setupAlgorithm() throws Exception {
		super.setUp();
		proxy = new PathPlumberProxy(new int[]{0},
				new double[]{1},
				new int[]{1,2,3},
				new int[0]);
		NToOneAlgorithm[] allToDestinationAlgorithms = new NToOneAlgorithm[4];
		for(int i = 0; i < allToDestinationAlgorithms.length; i++) {
			allToDestinationAlgorithms[i] = new DijkstraAlgorithm(controller);
		}

		RoutingAlgorithm algorithm = new AStarPruneAlgorithm(controller, allToDestinationAlgorithms);
		algorithm.setProxy(proxy);
		this.routingAlgorithmUnderTest = algorithm;
	}
}
