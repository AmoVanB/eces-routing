package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.graph.Node;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Component representing a disjoint routing request to several nodes.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DisjointRequest extends UnicastRequest {

	protected List<Node> destinations;
	public DisjointRequest(Node source, List<Node> destinations) {
		super(source, null);
		this.destinations = Collections.unmodifiableList(destinations);
	}

	public List<Node> getDestinations() {
		return destinations;
	}

	@Override
	public String toString() {
		return "(" + source + ")->[" + destinations + "]";
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		obj.put("entity", this.getEntity().getId());
		obj.put("source", source.getId());
		JSONArray destinations = new JSONArray();
		for(Node destination : this.getDestinations())
			destinations.put(destination.getId());
		obj.put("destinations", destinations);
		return obj;
	}
}
