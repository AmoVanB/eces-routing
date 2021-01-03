package de.tum.ei.lkn.eces.routing.algorithms.agnostic.gta;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;


/**
 * Edge of a new transformed graph.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class TransformedEdge extends Edge {
	/**
	 * Corresponding original edge.
	 */
	private Edge originalEdge;

	public TransformedEdge(Controller controller, TransformedNode source, TransformedNode destination, Edge originalEdge) {
		super(source, destination);
		source.addOutgoingConnection(this);
		destination.addIncomingConnection(this);
		this.originalEdge = originalEdge;
		this.setEntity(controller.createEntity());
	}

	public TransformedEdge(Controller controller,TransformedNode source, TransformedNode destination) {
		this(controller, source, destination,null);
	}

	public Edge getOriginalEdge() {
		return originalEdge;
	}
}
