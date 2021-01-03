package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.RoutingSystem;

import java.util.Arrays;

/**
 * Component representing a routing request.
 * It consists at least of a source and a destination Node but the class
 * can be derived to add additional information such as constraints
 * requirements and flow description.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class UnicastWithINRequest extends UnicastRequest{
	private Node[] intermediateNodes;

	public UnicastWithINRequest(Node source, Node[] intermediateNodes, Node destination) {
		super(source, destination);
		this.intermediateNodes =intermediateNodes;
	}

	@Override
	public UnicastWithINRequest clone()
	{
		return (UnicastWithINRequest) super.clone();
	}

	@Override
	public String toString() {
		if(intermediateNodes == null || intermediateNodes.length == 0)
			return super.toString();
		StringBuilder result = new StringBuilder("(" + source + ")->(");
		for(Node z: intermediateNodes)
			result.append(z).append(")->(");
		return result.toString() + destination + ")";
	}

	@Override
	public int hashCode() {
		if(intermediateNodes == null || intermediateNodes.length == 0)
			return super.hashCode();
		int hash = super.hashCode();
		for(Node z: intermediateNodes)
			hash = hash * 31 + z.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object request) {
		if(!(request instanceof UnicastWithINRequest))
			return false;
		if(!super.equals(request))
			return false;
		return Arrays.deepEquals(intermediateNodes, ((UnicastWithINRequest) request).getIntermediateNodes());
	}

	public Node[] getIntermediateNodes() {
		return intermediateNodes;
	}

	public void setIntermediateNodes(Node[] intermediateNodes) {
		this.intermediateNodes = intermediateNodes;
	}
}
