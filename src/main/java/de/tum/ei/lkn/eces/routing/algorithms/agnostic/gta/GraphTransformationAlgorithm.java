package de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.routing.algorithms.MetricTypes;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.AgnosticAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.PathProxy;
import de.tum.ei.lkn.eces.routing.proxies.PreviousEdgeProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.ResilientPath;
import de.tum.ei.lkn.eces.routing.responses.Response;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Abstract definition of an algorithm using a transformed
 * graph for solving a problem.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class GraphTransformationAlgorithm extends AgnosticAlgorithm implements SolveUnicastRequest {
	/**
	 * Underlying algorithm.
	 */
	protected SolveUnicastRequest solver;

	protected GraphTransformationAlgorithm(Controller controller, SolveUnicastRequest solver) {
		super(controller);
		this.solver = solver;
	}

	protected abstract TransformedGraph getTransformedGraph(Graph graph);

	public Response solveNoChecks(UnicastRequest request) {
		((GTAProxy) ((RoutingAlgorithm) solver).getProxy()).setOriginalRequest(request);
		return getResponseForOriginalGraph(
				this.solver.solveNoChecks(
							getRequestForTransformedGraph(
									getTransformedGraph(request.getGraph()),
									request)
						)
		);
	}

	/**
	 * Transforms a request for the transformed graph to a request for the
	 * original graph
	 * @param graph transformed graph.
	 * @param request request for the transformed graph.
	 * @return request for the original graph.
	 */
	protected UnicastRequest getRequestForTransformedGraph(TransformedGraph graph, UnicastRequest request) {
		UnicastRequest unicastRequest = request.clone();
		unicastRequest.setSource(graph.getSourceTransformationNode(unicastRequest.getSource()));
		unicastRequest.setDestination(graph.getDestinationTransformationNode(unicastRequest.getDestination()));
		return unicastRequest;
	}

	/**
	 * Transforms a response from the transformed graph to a response
	 * for the original graph.
	 * @param transformedResponse original response.
	 * @return Response for the original graph.
	 */
	protected Response getResponseForOriginalGraph(Response transformedResponse) {
		if(transformedResponse == null)
			return null;

		if(transformedResponse instanceof Path) {
			Path transformedPath = (Path) transformedResponse;
			return new Path(
					((GTAProxy) ((RoutingAlgorithm) this.solver).getProxy()).getPathInOriginalGraph(transformedPath),
					false,
					transformedPath.getCost(),
					transformedPath.getConstraintsValues(),
					transformedPath.getParametersValues()
			);
		} else if(transformedResponse instanceof ResilientPath) {
			ResilientPath resilientPath = (ResilientPath) transformedResponse;
			return new ResilientPath(
					(Path) getResponseForOriginalGraph(resilientPath.getPath1()),
					(Path) getResponseForOriginalGraph(resilientPath.getPath2())
			);
		}
		else
			throw new NotImplementedException("Transformation only implement for path and resilient path");
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

	protected void setProxy() {
		((RoutingAlgorithm) solver).setProxy(new GTAProxy(proxy));
	}

	@Override
	public MetricTypes getMetricsType() {
		return ((RoutingAlgorithm) solver).getMetricsType();
	}
}
