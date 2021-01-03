package de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.BD;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.OneToNAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;
import org.apache.commons.lang3.NotImplementedException;

/**
 * 1959
 * "A Note on Two Problems in Connexion with Graphs"
 * E. W. Dijsktra
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DijkstraAlgorithm extends SPAlgorithm implements SolveUnicastRequest, OneToNAlgorithm, NToOneAlgorithm, BD {
	UniversalPriorityQueueAlgorithm upqa;

	public DijkstraAlgorithm(Controller controller) {
		super(controller);
		upqa = new UniversalPriorityQueueAlgorithm(controller, QueueMode.NODE, true);
	}

	/**
	 * Configure A* to relax on Nodes, that is to have Nodes in its
	 * priority queue.
	 */
	public void nodeRelaxationMode() {
		upqa.setQueueMode(QueueMode.NODE);
	}

	/**
	 * Configure A* to relax on Edges, that is to have Edges in its
	 * priority queue.
	 */
	public void edgeRelaxationMode() {
		upqa.setQueueMode(QueueMode.EDGE);
	}

	/**
	 * Set maximum proxy type handled by A*.
	 * @param type Maximum proxy type.
	 */
	public void setMaximumProxy(ProxyTypes type) {
		upqa.setMaximumProxy(type);
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
		upqa.setProxy(proxy);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		if(request instanceof UnicastRequest)
			return upqa.computePath((UnicastRequest) request);
		else
			throw new NotImplementedException("Dijkstra only supports unicast requests!");
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return upqa.computePath(request);
	}

	/**
	 * Finds a route starting with a specific path.
	 * @param request Request for which the route has to be found.
	 * @param initialPath Initial starting path.
	 * @return
	 */
	public Path solveNoChecks(Request request, Iterable<Edge> initialPath) {
		return upqa.computePath((UnicastRequest) request, initialPath);
	}

	@Override
	public boolean isForward() {
		return upqa.isForward();
	}

	@Override
	public boolean isOptimal() {
		return upqa.isOptimal();
	}

	@Override
	public boolean isComplete() {
		return upqa.isComplete();
	}

	@Override
	public boolean isValid() {
		return upqa.isValid();
	}

	@Override
	public void computePathsToAnyNodeFrom(Node source, Request request) {
		this.upqa.computePathsToAnyNodeFrom(source, request);
	}

	@Override
	public Path getPathFromNodeTo(Node destination) {
		return this.upqa.getPathFromNodeTo(destination);
	}

	@Override
	public void computePathsFromAnyNodeTo(Node destination, Request request) {
		this.upqa.computePathsFromAnyNodeTo(destination, request);
	}

	@Override
	public Path getPathToNodeFrom(Node source) {
		return this.upqa.getPathToNodeFrom(source);
	}

	public void computePathsFromAnyNodeTo(Node destination, Request request, double costBorder) {
		this.upqa.computePathsFromAnyNodeTo(destination, request, costBorder);
	}

	public void computePathsToAnyNodeFrom(Node source, Request request, double costBorder) {
		this.upqa.computePathsToAnyNodeFrom(source, request, costBorder);
	}

	@Override
	public Response solve(Request request, double costBorder) {
		return upqa.solve(request, costBorder);
	}

	@Override
	public void setCostBorder(double costBorder) {
		upqa.setCostBorder(costBorder);
	}

	@Override
	public void removeCostBorder() {
		upqa.removeCostBorder();
	}
}
