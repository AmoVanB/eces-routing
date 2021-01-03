package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.graph.Node;

/**
 * Component representing a resilient routing request.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ResilientRequest extends UnicastRequest {

	public ResilientRequest(Node source, Node destination) {
		super(source, destination);
	}

	@Override
	public String toString() {
		return super.toString() + " (resilient)";
	}
}
