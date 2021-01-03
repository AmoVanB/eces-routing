package de.tum.ei.lkn.eces.routing.algorithms.sp.in;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.astar.AStarAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;
import org.apache.commons.lang3.NotImplementedException;

import java.util.LinkedList;

/**
 * Simple intermediate node algorithm: runs A* between intermediate nodes pairs.
 *
 * See: "LARAC-SN and Mole in the Hole: Enabling Routing through Service Function Chains". A Van Bemten, JW Guck,
 * P Vizarreta, CM Machuca, W Kellerer. NetSoft 2018.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SimpleINSPAlgorithm extends SPAlgorithm implements SolveUnicastRequest, SolveUnicastWithINRequest {
	/**
	 * Underlying algorithm used to compute the paths
	 * between the intermediate nodes.
	 */
	private AStarAlgorithm aStarAlgorithm;

	public SimpleINSPAlgorithm(Controller controller) {
		super(controller);
		aStarAlgorithm = new AStarAlgorithm(controller);
		aStarAlgorithm.setMaximumProxy(ProxyTypes.EDGE_PROXY);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		// If no intermediate nodes, normal solve.
		return aStarAlgorithm.solveNoChecks(request);
	}

	@Override
	public Response solveNoChecks(UnicastWithINRequest request) {
		// Simple if there are no intermediate nodes...
		if(request.getIntermediateNodes() == null || request.getIntermediateNodes().length == 0)
			return aStarAlgorithm.solveNoChecks(request);

		// So far nothing found
		Iterable<Edge> alreadyFoundPath = new LinkedList<>();

		// Last node reached... so far: the source
		Node lastNode = request.getSource();

		// Request used for the different parts of the path
		UnicastWithINRequest intermediateRequest = request.clone();
		intermediateRequest.setSource(request.getSource());
		intermediateRequest.setIntermediateNodes(new Node[0]);

		// Iterating between all the intermediate nodes pairs (incl. src/dst)
		for(int i = 0; i < request.getIntermediateNodes().length; i++) {
			Node intermediateNode = request.getIntermediateNodes()[i];

			// Nothing needed if we're already at the next node
			if(lastNode != intermediateNode) {
				intermediateRequest.setDestination(intermediateNode);
				alreadyFoundPath = aStarAlgorithm.solveNoChecks(intermediateRequest, alreadyFoundPath);

				// Stop if nothing found...
				if(alreadyFoundPath == null)
					return null;

				lastNode = intermediateNode;
			}
		}

		// Last iteration: from the last intermediate node to destination
		intermediateRequest.setDestination(request.getDestination());
		return aStarAlgorithm.solveNoChecks(intermediateRequest, alreadyFoundPath);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		if(request instanceof UnicastWithINRequest)
			return aStarAlgorithm.solveNoChecks((UnicastWithINRequest) request);
		else if(request instanceof UnicastRequest)
			return aStarAlgorithm.solveNoChecks((UnicastRequest) request);
		else
			throw new NotImplementedException("Dijkstra only supports unicast requests!");
	}

	@Override
	public boolean isForward() {
		return aStarAlgorithm.isForward();
	}

	@Override
	public boolean isOptimal() {
		return aStarAlgorithm.isOptimal();
	}

	@Override
	public boolean isComplete() {
		return aStarAlgorithm.isComplete();
	}

	@Override
	public boolean isValid() {
		return aStarAlgorithm.isValid();
	}

	@Override
	public void setProxy(EdgeProxy edgeProxy) {
		super.setProxy(edgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		setProxy();
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		setProxy();
	}

	private void setProxy() {
		aStarAlgorithm.setProxy(this.proxy);
	}
}
