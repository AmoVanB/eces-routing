package de.tum.ei.lkn.eces.routing.exceptions;

/**
 * Exception thrown by an algorithm if it is not able to handle a Request.
 * This does not mean that no path has been found, but that the Algorithm is
 * not a suitable Algorithm for such a request (e.g. the algorithm is unaware
 * of constraints and a delay-constrained request is made).
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class UnableToHandleRequestException extends RoutingException {
	private static final long serialVersionUID = 2113533029680219692L;

	public UnableToHandleRequestException(String string) {
        super(string);
    }
}
