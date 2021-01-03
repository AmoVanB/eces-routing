package de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transformed graph.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class TransformedGraph extends Graph {
	/**
	 * Corresponding original graph.
	 */
	private Graph originalGraph;

	/**
	 * Node mappings for source nodes.
	 */
	private Map<Node, Node> sourceTransformation = new HashMap<>();

	/**
	 * Node mappings for destination nodes.
	 */
	private Map<Node, Node> destinationTransformation = new HashMap<>();

	public TransformedGraph(Controller controller, Graph originalGraph) {
		this.originalGraph = originalGraph;
		this.setEntity(controller.createEntity());
	}

	public void setSourceTransformation(Map<Node, Node> sourceTransformation) {
		this.sourceTransformation = Collections.unmodifiableMap(new LinkedHashMap<>(sourceTransformation));
	}

	public void setDestinationTransformation(Map<Node, Node> destinationTransformation) {
		this.destinationTransformation = Collections.unmodifiableMap(new LinkedHashMap<>(destinationTransformation));
	}

	public Graph getOriginalGraph() {
		return originalGraph;
	}

	public Node getSourceTransformationNode(Node node) {
		return sourceTransformation.get(node);
	}

	public Node getDestinationTransformationNode(Node node) {
		return destinationTransformation.get(node);
	}

	// Override the following to have them public

	@Override
	public void addNode(Node node) {
		nodes.add(node);
	}

	@Override
	public void removeNode(Node node) {
		nodes.remove(node);
	}

	@Override
	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	@Override
	public void removeEdge(Edge edge) {
		edges.remove(edge);
	}
}
