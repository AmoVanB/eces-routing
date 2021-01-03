package de.tum.ei.lkn.eces.routing.algorithms.sp.ksp;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A KSPAlgorithm (k-Shortest Paths) Algorithm is able to compute the first k
 * shortest paths for a request which only wants to minimize a given objective
 * without any constraint.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class KSPAlgorithm extends SPAlgorithm implements SolveUnicastRequest {
	/**
	 * Last Iterator created for each request.
	 * This allows to call next() after getPath() and obtain the second
	 * shortest path, avoiding to recompute the first one.
	 */
	private Map<UnicastRequest, Iterator<Path>> iterators;

	protected KSPAlgorithm(Controller controller) {
		super(controller);
		iterators = new HashMap<>();
	}

	@Override
	public boolean handle(Request request) {
		return this.getProxy().getNumberOfConstraints(request) == 0 && this.getProxy().handle(request, this.isForward());
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}

	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath(request);
	}

	protected Path computePath(UnicastRequest request) {
		// The shortest path is the first path of a new iterator.
		Iterator<Path> iterator = this.iterator(request);
		this.iterators.put(request, iterator);
		return iterator.next();
	}

	/**
	 * Returns the Iterator created by the last getPath() call for this
	 * Request.
	 * If no Iterator was created yet, one is created.
	 * Iterators created using the iterator() method are not considered
	 * by this function.
	 * @param request Request.
	 * @return Iterator.
	 */
	public Iterator<Path> getCurrentIterator(UnicastRequest request) {
		if(!iterators.containsKey(request))
			this.iterators.put(request, this.iterator(request));

		return this.iterators.get(request);
	}

	/**
	 * For a given Request, returns the number of Paths already returned
	 * by the Iterator associated to this request.
	 * If x is returned, this means that the next call to next() (if
	 * hasNext() is true) will return the (x+1)th shortest path.
	 * @param request Request.
	 * @return k.
	 */
	public abstract int getK(UnicastRequest request);

	public abstract Iterator<Path> iterator(UnicastRequest request);
}
