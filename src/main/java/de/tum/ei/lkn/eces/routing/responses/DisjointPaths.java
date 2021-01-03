package de.tum.ei.lkn.eces.routing.responses;

import com.google.common.collect.ImmutableSet;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Response of a DisjointRequest: set of disjoint paths.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DisjointPaths extends Response implements IndependentSetOfPaths {
	private List<Path> paths;

	public DisjointPaths(List<Path> paths) {
		this.paths = Collections.unmodifiableList(paths);
	}

	public Set<Path> getPaths() {
		return  ImmutableSet.copyOf(this.paths);
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		for(Path path : getPaths()) {
			string.append("- ").append(path).append("\n");
		}

		return string.toString();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = super.toJSONObject();

		int i = 0;
		for(Path path : this.getPaths()) {
			result.put(String.valueOf(i), path.getEntity().toJSONObject());
			i++;
		}

		return result;
	}
}
