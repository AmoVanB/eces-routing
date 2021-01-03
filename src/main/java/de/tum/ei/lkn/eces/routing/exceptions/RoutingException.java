package de.tum.ei.lkn.eces.routing.exceptions;

/**
 * Exception thrown when wrong Routing operations are performed.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class RoutingException extends RuntimeException {
	private static final long serialVersionUID = 3913533029680219692L;

	public RoutingException(String string) {
        super(string);
    }
}
