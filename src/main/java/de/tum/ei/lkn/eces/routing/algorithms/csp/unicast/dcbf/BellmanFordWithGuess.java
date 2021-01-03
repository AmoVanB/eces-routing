package de.tum.ei.lkn.eces.routing.algorithms.csp.unicast.dcbf;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempData;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempDataGuess;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempIterablePath;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.bellmanford.BellmanFordAlgorithm;
import de.tum.ei.lkn.eces.routing.requests.Request;

/**
 * Implementation of Bellman-Ford with an additional guess.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class BellmanFordWithGuess extends BellmanFordAlgorithm {
	public BellmanFordWithGuess(Controller controller, int k) {
		super(controller, k);
	}

	@Override
	protected boolean updateCosts(TempData nodeData, TempData nextNodeData, TempIterablePath iterator, Edge edge, double[] newParameters, Request request){
		double edgeCost = proxy.getCost(iterator, edge, newParameters, request, isForward());
		if(((TempDataGuess)nextNodeData).getSum() > nodeData.getCost() + edgeCost + ((DelayTestProxy)proxy).getCostToDestination()) { // costtodestination was compute by the hasaccess call before
			nextNodeData.setCost(nodeData.getCost() + edgeCost);
			nextNodeData.setPath(new TempIterablePath(nodeData.getPath(), edge));
			nextNodeData.setParameter(newParameters);
			((TempDataGuess)nextNodeData).setGuess(((DelayTestProxy)proxy).getCostToDestination());
			return true;
		}
		return false;
	}
}
