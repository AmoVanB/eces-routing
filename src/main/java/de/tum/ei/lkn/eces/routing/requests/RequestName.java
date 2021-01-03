package de.tum.ei.lkn.eces.routing.requests;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.routing.RoutingSystem;
import org.json.JSONObject;

/**
 * Component representing the name of any request.
 *
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = RoutingSystem.class)
public class RequestName extends Component implements Cloneable {
    private String name;

    public RequestName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", this.getName());
        return obj;
    }
}
