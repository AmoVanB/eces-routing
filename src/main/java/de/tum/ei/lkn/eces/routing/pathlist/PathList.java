package de.tum.ei.lkn.eces.routing.pathlist;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.responses.Path;
import org.json.JSONObject;

import java.util.*;

/**
 * Component storing a list of Path.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = PathListSystem.class)
public class PathList extends Component implements Iterable<Entity> {
	/**
	 * The list of Paths.
	 */
	private Map<Entity, Date> list = new HashMap<>();

	/**
	 * Adds a Path to the list.
	 * @param path Path to add.
	 */
	public void addPath(Entity path) {
		list.put(path, new Date());
	}

	/**
	 * Removes a Path from the list.
	 * @param path Path to remove.
	 */
	public void removePath(Path path) {
		list.remove(path.getEntity());
	}

	/**
	 * Removes a Path from the list.
	 * @param path Path to remove.
	 */
	public void removePath(Entity path) {
		list.remove(path);
	}

	/**
	 * Returns the list of Paths currently stored.
	 * @return The list of Paths.
	 */
	public List<Entity> getPathList() {
		return new ArrayList<>(list.keySet());
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		obj.put("size", this.list.size());

		JSONObject flows = new JSONObject();
		for(Map.Entry<Entity, Date> pathEntry : this.list.entrySet())
			flows.put(Long.toString(pathEntry.getValue().getTime()), pathEntry.getKey().toJSONObject());
		obj.put("flows", flows);
		return obj;
	}

	@Override
	public Iterator<Entity> iterator() {
		return list.keySet().iterator();
	}
}
