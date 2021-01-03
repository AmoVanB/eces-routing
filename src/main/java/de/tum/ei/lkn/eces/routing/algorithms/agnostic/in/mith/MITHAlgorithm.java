package de.tum.ei.lkn.eces.routing.algorithms.agnostic.in.mith;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta.GraphTransformationAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta.TransformedEdge;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta.TransformedGraph;
import de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta.TransformedNode;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastWithINAndCandidatesRequest;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINAndCandidatesRequest;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.Response;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The MITH algorithm.
 *
 * 2018
 * "LARAC-SN and Mole in the Hole: Enabling Routing through Service Function Chains"
 * A Van Bemten, JW Guck, P Vizarreta, CM Machuca, W Kellerer.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MITHAlgorithm extends GraphTransformationAlgorithm implements SolveUnicastRequest, SolveUnicastWithINRequest, SolveUnicastWithINAndCandidatesRequest {
	/**
	 * Candidate matrix.
	 */
	private Node[][] intermediateNodesMatrix;

	/**
	 * Candidate matrix used previously.
	 */
	private Node[][] cachedIntermediateNodesMatrix;

	/**
	 * Previously generated graph.
	 */
	private TransformedGraph cachedGraph;

	public MITHAlgorithm(Controller controller, SolveUnicastRequest solver) {
		super(controller, solver);
	}

	@Override
	protected TransformedGraph getTransformedGraph(Graph graph) {
		// If candidates didn't change, then no need to recreate the graph.
		if(!Arrays.deepEquals(intermediateNodesMatrix, cachedIntermediateNodesMatrix)) {
			cachedGraph = createTransformedGraph(graph);
			cachedIntermediateNodesMatrix = intermediateNodesMatrix;
		}

		return cachedGraph;
	}

	private TransformedGraph createTransformedGraph(Graph graph) {
		TransformedGraph transformedGraph = new TransformedGraph(controller, graph);
		Map<Node, Node> previousLayer = createNewLayer(graph, transformedGraph);
		transformedGraph.setSourceTransformation(previousLayer);

		// New layer for each intermediate node.
		for(int i = 0; i < intermediateNodesMatrix.length; i++) {
			Map<Node, Node> newLayer = createNewLayer(graph, transformedGraph);
			// Traversing edge for all the candidates.
			for (Node node : intermediateNodesMatrix[i]) {
				transformedGraph.addEdge(new TransformedEdge(controller,
						(TransformedNode) previousLayer.get(node),
						(TransformedNode) newLayer.get(node)));
			}

			previousLayer = newLayer;
		}

		transformedGraph.setDestinationTransformation(previousLayer);
		return transformedGraph;
	}

	/**
	 * Creates a new layer in the transformed graph.
	 * @param originalGraph original graph.
	 * @param transformedGraph transformed graph.
	 * @return Map from original intermediateNodesMatrix to their new transformed
	 *         node in the new layer.
	 */
	private Map<Node, Node> createNewLayer(Graph originalGraph, TransformedGraph transformedGraph) {
		Map<Node, Node> originalToNewNodes = new HashMap<>();

		// Creating intermediateNodesMatrix.
		for(Node node : originalGraph.getNodes()) {
			TransformedNode transformedNode = new TransformedNode(controller, transformedGraph, node);
			transformedGraph.addNode(transformedNode);
			originalToNewNodes.put(node, transformedNode);
		}

		// Creating edges.
		for(Edge edge : originalGraph.getEdges()) {
			TransformedEdge transformedEdge = new TransformedEdge(
					controller,
					(TransformedNode) originalToNewNodes.get(edge.getSource()),
					(TransformedNode) originalToNewNodes.get(edge.getDestination()),
					edge);
			transformedGraph.addEdge(transformedEdge);
		}

		return originalToNewNodes;
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		intermediateNodesMatrix = new Node[0][0];
		return super.solveNoChecks(request);
	}

	@Override
	public Response solveNoChecks(UnicastWithINRequest request) {
		Node in[] = request.getIntermediateNodes();
		intermediateNodesMatrix = new Node[in.length][1];
		for(int i = 0; i < in.length; i++) {
			intermediateNodesMatrix[i][0] = in[i];
		}

		return super.solveNoChecks(request);
	}

	@Override
	public Response solveNoChecks(UnicastWithINAndCandidatesRequest request) {
		intermediateNodesMatrix = request.getCandidates();
		return super.solveNoChecks((UnicastRequest) request);
	}

	@Override
	protected Response solveNoChecks(Request request) {
		throw new NotImplementedException("Should use the specific solve methods");
	}

	@Override
	public boolean handle(Request request) {
		return false; // TODO not implemented yet
	}

	@Override
	public boolean isForward() {
		return ((RoutingAlgorithm) solver).isForward();
	}

	@Override
	public boolean isOptimal() {
		return ((RoutingAlgorithm) solver).isOptimal();
	}

	@Override
	public boolean isComplete() {
		return ((RoutingAlgorithm) solver).isComplete();
	}

	@Override
	public boolean isValid() {
		return ((RoutingAlgorithm) solver).isValid();
	}
}
