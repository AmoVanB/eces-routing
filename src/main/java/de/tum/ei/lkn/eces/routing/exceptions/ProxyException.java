package de.tum.ei.lkn.eces.routing.exceptions;

/**
 * Exception thrown when something wrong happens with a Proxy.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class ProxyException extends RoutingException {
	private static final long serialVersionUID = 2113533029680219522L;

	public ProxyException(String string) {
        super(string);
    }
}
