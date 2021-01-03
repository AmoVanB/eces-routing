package de.tum.ei.lkn.eces.routing.responses;

import com.google.common.collect.ImmutableSet;
import org.json.JSONObject;

import java.util.Set;

/**
 * Response to a resilient request.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ResilientPath extends Response implements IndependentSetOfPaths {
	private Path path1;
	private Path path2;

	public ResilientPath(Path path1, Path path2) {
		this.path1 = path1;
		this.path2 = path2;
	}

	public Path getPath1() {
		return path1;
	}

	public Path getPath2() {
		return path2;
	}

	public String toString() {
		return "{" + path1.toString() + "x" + path2.toString() + "}";
	}

	public Set<Path> getPaths() {
		return ImmutableSet.of(path1, path2);
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = super.toJSONObject();
		result.put("0", path1.getEntity().toJSONObject());
		result.put("1", path2.getEntity().toJSONObject());
		return result;
	}
}
