package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import org.json.JSONObject;

/**
 * Component representing a routing request.
 * It consists at least of a source and a destination Node but the class
 * can be derived to add additional information such as constraints
 * requirements and flow description.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class UnicastRequest extends Request {
	/**
	 * Source node.
	 */
	protected Node source;

	/**
	 * Destination node.
	 */
	protected Node destination;

	public UnicastRequest(Node source, Node destination) {
		super();
		this.source = source;
		this.destination = destination;	}

	/**
	 * Gets the source Node of the Request.
	 * @return source Node.
	 */
	public Node getSource() {
		return source;
	}

	/**
	 * Gets the destination Node of the Request.
	 * @return destination Node.
	 */
	public Node getDestination() {
		return destination;
	}

	/**
	 * Sets the source of the Request to a given Node.
	 * @param source New source of the Request.
	 */
	public void setSource(Node source) {
		this.source = source;
	}

	/**
	 * Sets the destination of the Request to a given Node.
	 * @param destination New source of the Request.
	 */
	public void setDestination(Node destination) {
		this.destination = destination;
	}

	@Override
	public UnicastRequest clone()
	{
		return (UnicastRequest) super.clone();
	}

	@Override
	public String toString() {
		return "(" + source + ")->(" + destination + ")";
	}

	@Override
	public int hashCode() {
		return this.source.hashCode() + 31 * this.destination.hashCode();
	}

	@Override
	public boolean equals(Object request) {
		return request instanceof UnicastRequest && source.equals((((UnicastRequest) request).getSource())) && destination.equals((((UnicastRequest) request).getDestination()));
	}

	@Override
	public Graph getGraph() {
		return source.getGraph();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		obj.put("entity", this.getEntity().getId());
		obj.put("source", source.getId());
		if(destination != null)
			obj.put("destination", destination.getId());
		return obj;
	}
}
