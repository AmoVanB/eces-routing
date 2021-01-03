package de.tum.ei.lkn.eces.routing.responses;

import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import org.json.JSONObject;

/**
 * Error response: returned when no response was found.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ErrorResponse extends Response {
	public ErrorResponse(Proxy proxy) {
		super();
		this.setProxy(proxy);
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = super.toJSONObject();
		result.put("ErrorResponse", result);
		return result;
	}
}
