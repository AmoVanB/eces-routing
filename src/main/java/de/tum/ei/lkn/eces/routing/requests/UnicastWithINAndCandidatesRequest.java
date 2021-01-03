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
public class UnicastWithINAndCandidatesRequest extends UnicastRequest{
	private Node[][] candidates;

	public UnicastWithINAndCandidatesRequest(Node source, Node[][] candidates, Node destination) {
		super(source, destination);
		this.candidates = candidates;
	}

	@Override
	public UnicastWithINAndCandidatesRequest clone()
	{
		return (UnicastWithINAndCandidatesRequest) super.clone();
	}

	@Override
	public int hashCode() {
		if(candidates == null || candidates.length == 0)
			return super.hashCode();
		int hash = super.hashCode();
		for(Node[] v : candidates)
			for(Node z : v)
				hash = hash * 31 + z.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object request) {
		if(!(request instanceof UnicastWithINAndCandidatesRequest))
			return false;
		if(!super.equals(request))
			return false;
		return Arrays.deepEquals(candidates, ((UnicastWithINAndCandidatesRequest) request).getCandidates());
	}

	public Node[][] getCandidates() {
		return candidates;
	}

	public void setCandidates(Node[][] matrix) {
		this.candidates = matrix;
	}
}
