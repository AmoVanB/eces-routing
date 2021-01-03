package de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;

/**
 * Node of a new transformed graph.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class TransformedNode extends Node {
	/**
	 * Corresponding original node.
	 */
	private Node originalNode;

	public TransformedNode(Controller controller, TransformedGraph transformedGraph, Node originalNode) {
		super(transformedGraph);
		this.originalNode = originalNode;
		this.setEntity(controller.createEntity());
	}

	public Node getOriginalNode() {
		return originalNode;
	}

	// Override the following to have them accessible from this package (...)

	@Override
	protected void addOutgoingConnection(Edge edge) {
		outgoingConnections.add(edge);
	}

	@Override
	protected void addIncomingConnection(Edge edge) {
		incomingConnections.add(edge);
	}
}
