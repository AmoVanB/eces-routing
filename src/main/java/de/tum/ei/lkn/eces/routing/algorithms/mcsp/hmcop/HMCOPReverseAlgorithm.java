package de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempData;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;

/**
 * The reverse algorithm of H_MCOP.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HMCOPReverseAlgorithm extends UniversalPriorityQueueAlgorithm {
	public HMCOPReverseAlgorithm(Controller controller) {
		super(controller, QueueMode.NODE, false, false);
	}
	public HMCOPReverseAlgorithm(Controller controller, ProxyTypes proxyType) {
		super(controller, QueueMode.NODE, false, false);
		switch (proxyType){
			case PREVIOUS_EDGE_PROXY:
				this.queueMode = QueueMode.EDGE;
			case PATH_PROXY:
				throw new RoutingException("H_MCOP dose never support a path proxy!");
		}
	}

	@Override
	protected double computeCost(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints){
		double sum = 0;
		for(int i = 0; i < newConstraints.length; i++)
			sum += newConstraints[i] / proxy.getConstraintsBounds(request)[i];

		return sum;
	}

	@Override
	public boolean handle(Request request) {
		return this.getProxy().handle(request, isForward());
	}
}


