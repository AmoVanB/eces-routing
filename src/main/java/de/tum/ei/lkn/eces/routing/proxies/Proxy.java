package de.tum.ei.lkn.eces.routing.proxies;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.proxies.wrappers.PathProxyWrapper;
import de.tum.ei.lkn.eces.routing.requests.DisjointRequest;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.requests.UnicastWithINRequest;
import de.tum.ei.lkn.eces.routing.responses.*;
import org.apache.commons.math3.util.MathArrays;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Proxy is responsible for interfacing a routing algorithm with a network
 * model.
 *
 * The proxy able to:
 * - register and deregister a new flow in the network,
 * - determine resources availability at an EDGE (or simply access to the EDGE),
 * - compute the cost and constraints values of an EDGE,
 * - compute the evolution of so-called parameters along a Path,
 * - optionally, compute a guess for the cost and constraints values to a NODE.
 *
 * Three Proxy types are defined depending on what input the Proxy needs to
 * compute cost, constraints and resources availability at an EDGE: EdgeProxy,
 * PreviousEdgeProxy and PathProxy. These are documented in ProxyTypes.java.
 * The list of parameters for the methods of the Proxy hence depend on the Proxy
 * type. Therefore, concerned methods are defined only in the corresponding
 * children classes.
 *
 * Note that if the algorithm using the Proxy is backward, then a
 * previousEdgeProxy needs what is actually the next EDGE and a PathProxy what
 * is actually the Path followed by the Request *after* the current EDGE.
 *
 * See: "Routing metrics depending on previous edges: The Mn taxonomy and its corresponding solutions" A Van Bemten,
 * JW Guck, CM Machuca, W Kellerer. 2018.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class Proxy implements Cloneable {
	/**
	 * Wrapping the Proxy in a PathProxy to allow to implement the
	 * checkValidity method only once.
	 */
	private PathProxy wrapper;

	public Proxy() {
		if(this instanceof EdgeProxy)
			wrapper = new PathProxyWrapper((EdgeProxy) this);
		else if(this instanceof PreviousEdgeProxy)
			wrapper = new PathProxyWrapper((PreviousEdgeProxy) this);
		else if(this instanceof PathProxy)
			wrapper = (PathProxy) this;
	}

	/**
	 * Checks if a Path is valid for a given Request. If true is returned,
	 * it means that the Path is a valid route satisfying the needs of the
	 * Request.
	 * @param response The Response.
	 * @param request The Request.
	 * @return true/false based on whether the Path is valid or not.
	 */
	public final boolean isValid(Response response, Request request) {
		return checkValidity(response, request, false) != null;
	}

	/**
	 * If path is valid for request, returns a new Path corresponding to the
	 * original one and with appropriate cost, constraints and parameters.
	 * If path is not valid, returns null.
	 * @param response Response to check.
	 * @param request Request for which the Path has to be checked.
	 * @param variablesCheck If true, verifying that the cost, constraints and
	 *                       parameters stored in the Path correspond to their
	 *                       actual values is part of the validity check. If
	 *                       false, values stored in the Path are not
	 *                       considered.
	 * @return A copy of the Path with appropriate parameters if it is valid,
	 *         null otherwise.
	 */
	public final Response checkValidity(Response response, Request request, boolean variablesCheck) {
		if(response instanceof Path && request instanceof UnicastWithINRequest){
			Iterator<Edge> iterator = ((Path) response).iterator();
			Edge edge = null;
			boolean sourceMode = true;
			for(Node inode: ((UnicastWithINRequest) request).getIntermediateNodes()){
				if(sourceMode) {
					if (inode == ((UnicastWithINRequest) request).getSource()) {
						if(edge == null)
							edge = iterator.next();
						if (edge.getSource() != inode)
							return null;
					} else {
						sourceMode = false;
					}
				}
				if(!sourceMode){
					if(edge != null && edge.getDestination() == inode)
						continue;
					while(true) {
						if (!iterator.hasNext())
							return null;
						edge = iterator.next();
						if(edge.getDestination() == inode)
							break;
					}
				}
			}
			return checkPathValidity((Path) response,(UnicastRequest) request, variablesCheck);
		}
		if(response instanceof Path && request instanceof UnicastRequest)
			return checkPathValidity((Path) response,(UnicastRequest) request, variablesCheck);
		if(response instanceof ResilientPath && request instanceof UnicastRequest){
			Path p1 = checkPathValidity(((ResilientPath) response).getPath1(),(UnicastRequest) request, variablesCheck);
			Path p2 = checkPathValidity(((ResilientPath) response).getPath2(),(UnicastRequest) request, variablesCheck);
			if(p1 != null && p2 != null)
				return new ResilientPath(p1, p2);
		}
		if(response instanceof DisjointPaths && request instanceof DisjointRequest) {
			List<Path> finalPaths = new LinkedList<>();
			int i = 0;
			for(Path p : ((DisjointPaths) response).getPaths())
				finalPaths.add(checkPathValidity(p, new UnicastRequest(((DisjointRequest) request).getSource(), ((DisjointRequest) request).getDestinations().get(i++)), variablesCheck));

			if(!finalPaths.contains(null))
				return new DisjointPaths(finalPaths);
		}
		return null;
	}

	/**
	 * If path is valid for request, returns a new Path corresponding to the
	 * original one and with appropriate cost, constraints and parameters.
	 * If path is not valid, returns null.
	 * @param path Path to check.
	 * @param request Request for which the Path has to be checked.
	 * @param variablesCheck If true, verifying that the cost, constraints and
	 *                       parameters stored in the Path correspond to their
	 *                       actual values is part of the validity check. If
	 *                       false, values stored in the Path are not
	 *                       considered.
	 * @return A copy of the Path with appropriate parameters if it is valid,
	 *         null otherwise.
	 */
	private final Path checkPathValidity(Path path, UnicastRequest request, boolean variablesCheck) {
		if(path == null)
			return null;

		// Iterator over the part of the Path already visited.
		LinkedList<Edge> headPathIterator = new LinkedList<>();
		double realCost = 0;
		double constraintsBounds[] = this.getConstraintsBounds(request);
		double realConstraintsValues[] = new double[this.getNumberOfConstraints(request)];
		double realParametersValues[] = null;

		Edge oldEdge = null;
		for(Edge edge : path) {
			if(oldEdge == null && edge.getSource() != request.getSource())
				return null; // The first EDGE does not start from the source node.
			if(oldEdge != null && oldEdge.getDestination() != edge.getSource())
				return null; // Edges are not connected.

			realParametersValues = this.wrapper.getNewParameters(headPathIterator, edge, realParametersValues, request, true);
			if(!this.wrapper.hasAccess(headPathIterator, edge, realParametersValues, request, true))
				return null; // Using an EDGE which it cannot.

			realCost += this.wrapper.getCost(headPathIterator, edge, realParametersValues, request, true);
			double newConstraints[] = this.wrapper.getConstraintsValues(headPathIterator, edge, realParametersValues, request, true);
			for(int i = 0; i < realConstraintsValues.length; i++)
				realConstraintsValues[i] += newConstraints[i];
			headPathIterator.addFirst(edge);
			oldEdge = edge;
		}

		if(oldEdge == null || oldEdge.getDestination() != request.getDestination())
			return null; // No Path or does not reach destination.

		for(int i = 0; i < realConstraintsValues.length; i++)
			if(Proxy.violatesBound(realConstraintsValues[i], constraintsBounds[i],10E-8))
				return null; // Constraints bounds violated.

		// Checking values stored in the initial Path were right.
		if(variablesCheck) {
			if(realCost != path.getCost())
				return null; // Incorrect cost.

			for(int i = 0; i < realConstraintsValues.length; i++)
				if(realConstraintsValues[i] != path.getConstraintsValues()[i])
					return null; // Incorrect constraint value.

			for(int i = 0; i < realParametersValues.length; i++)
				if(realParametersValues[i] != path.getParametersValues()[i])
					return null; // Incorrect parameter value.
		}

		return new Path(headPathIterator,true, realCost, realConstraintsValues, realParametersValues);
	}

	/**
	 * Creates a valid Path Object out of an array of Edges. Note that the
	 * EDGE array is not checked (e.g. against loops, unconnected Edges, etc.).
	 * @param path Array of Edges.
	 * @param request Request for which the Path object must be created.
	 * @param isForward If the EDGE array is forward or backward.
	 * @return A Path Object with appropriate cost, constraints values and
	 *         parameters.
	 */
	public final Path createPath(Edge[] path, UnicastRequest request, boolean isForward) {
		return this.createPath(Arrays.asList(path), request, isForward);
	}

	/**
	 * Creates a valid Path Object out of an iterable of Edges. Note that the
	 * EDGE iterable not checked (e.g. against loops, unconnected Edges, etc.).
	 * @param path The Edges.
	 * @param request Request for which the Path object must be created.
	 * @param isForward Whether the EDGE array is forward or backward.
	 * @return A Path Object with appropriate cost, constraints values and
	 *         parameters.
	 */
	public final Path createPath(Iterable<Edge> path, UnicastRequest request, boolean isForward) {
		if(path == null)
			return null;
		LinkedList<Edge> pathToFollow = new LinkedList<>();
		if(isForward){
			for (Edge edge : path){
				pathToFollow.add(edge);
			}
		}else{
			for (Edge edge : path){
				pathToFollow.addFirst(edge);
			}
		}
		LinkedList<Edge> headPathIterator = new LinkedList<>();
		double realCost = 0;
		double realConstraintsValues[] = new double[this.getNumberOfConstraints(request)];
		double realParametersValues[] = null;
		for(Edge edge : pathToFollow) {
			realParametersValues = this.wrapper.getNewParameters(headPathIterator, edge, realParametersValues, request, true);
			realCost += this.wrapper.getCost(headPathIterator, edge, realParametersValues, request, true);
			double newConstraints[] = this.wrapper.getConstraintsValues(headPathIterator, edge, realParametersValues, request, true);
			realConstraintsValues = MathArrays.ebeAdd(realConstraintsValues, newConstraints);
			headPathIterator.addFirst(edge);
		}

		return new Path(headPathIterator, true, realCost, realConstraintsValues, realParametersValues);
	}

	/**
	 * Registers a Path for a given Request. This usually reserves resources
	 * in the network for the Request.
	 * @param response Response to register.
	 * @param request Request for which the Path has to be registered.
	 * @return true/false based on success/failure.
	 */
	public boolean register(Response response, Request request){
		if(response instanceof Path){
			return register((Path)response, request);
		} else if(response instanceof IndependentSetOfPaths){
			return register((IndependentSetOfPaths)response, request);
		} else if(response instanceof ErrorResponse){
			return true;
		}
		throw new RoutingException("The response type " + response.getClass().getName() + " could not be registered");
	}

	/**
	 * Deregisters a Path for a given Request. This usually frees the
	 * resources reserved for the Path in the network.
	 * @param response Response to deregister.
	 * @param request Request for which the Path has to be unregistered.
	 * @return true/false based on success/failure.
	 */
	public boolean deregister(Response response, Request request){
		if(response instanceof Path){
			return deregister((Path)response, request);
		} else if(response instanceof IndependentSetOfPaths){
			return deregister((IndependentSetOfPaths)response, request);
		} else if(response instanceof ErrorResponse){
			return true;
		}
		throw new RoutingException("The response type " + response.getClass().getName() + " could not be deregistered");
	}

	/**
	 * Registers a Path for a given Request. This usually reserves resources
	 * in the network for the Request.
	 * @param path Path to register.
	 * @param request Request for which the Path has to be registered.
	 * @return true/false based on success/failure.
	 */
	public boolean register(Path path, Request request){
		return deRegister(path,request,true);
	}

	/**
	 * Deregisters a Path for a given Request. This usually frees the
	 * resources reserved for the Path in the network.
	 * @param path Path to deregister.
	 * @param request Request for which the Path has to be unregistered.
	 * @return true/false based on success/failure.
	 */
	public boolean deregister(Path path, Request request){
		return deRegister(path, request,false);
	}

	/**
	 * Registers a Path for a given Request. This usually reserves resources
	 * in the network for the Request.
	 * @param setOfPaths set of Paths to register.
	 * @param request Request for which the Path has to be registered.
	 * @return true/false based on success/failure.
	 */
	public boolean register(IndependentSetOfPaths setOfPaths, Request request) {
		return deRegister(setOfPaths,request,true);
	}

	/**
	 * Deregisters a Path for a given Request. This usually frees the
	 * resources reserved for the Path in the network.
	 * @param setOfPaths set of Paths to deregister.
	 * @param request Request for which the Path has to be unregistered.
	 * @return true/false based on success/failure.
	 */
	public boolean deregister(IndependentSetOfPaths setOfPaths, Request request){
		return deRegister(setOfPaths,request,false);
	}

	protected boolean deRegister(IndependentSetOfPaths setOfPaths, Request request, boolean register){
		boolean success = true;
		for(Path path : setOfPaths.getPaths()) {
			if(register){
				success = this.wrapper.register(path, request);
			}else{
				success = this.wrapper.deregister(path, request);
			}
		}
		return success;
	}

	protected boolean deRegister(Path path, Request request, boolean register){
		double realParametersValues[] = null;
		LinkedList<Edge> headPathIterator = new LinkedList<>();
		boolean success = true;
		for(Edge edge : path) {
			realParametersValues = this.wrapper.getNewParameters(headPathIterator, edge, realParametersValues, request, true);
			if(register){
				success = this.wrapper.register(headPathIterator,edge,realParametersValues,request);
			}else{
				success = this.wrapper.deregister(headPathIterator,edge,realParametersValues,request);
			}
			headPathIterator.addFirst(edge);
		}
		return success;
	}

	/**
	 * Checks if the Proxy is able/suitable to handle the routing of a given
	 * Request.
	 * @param request Request to route.
	 * @param isForward true if the requesting algorithm is forward, false
	 *                  otherwise.
	 * @return true/false based on whether or not the Proxy can handle this
	 *         Request.
	 */
	public abstract boolean handle(Request request, boolean isForward);

	/**
	 * Gets the size of the array returned by the `getConstraintsValues`
	 * (defined by children classes) and `getConstraintsBounds` methods
	 * for a given Request.
	 * @param request The Request.
	 * @return Number of constraints defined by the Proxy.
	 */
	public abstract int getNumberOfConstraints(Request request);

	/**
	 * Gets the size of the array returned by the `getNewParameters` method
	 * (defined by children classes) for a given Request.
	 * @param request The Request.
	 * @return Number of parameters defined by the Proxy.
	 */
	public abstract int getNumberOfParameters(Request request);

	/**
	 * Returns the E2E upper bounds on the constraints that are acceptable
	 * for a given Request.
	 * @param request The Request.
	 * @return The bounds.
	 */
	public abstract double[] getConstraintsBounds(Request request);

	/**
	 * Computes a guess for the least possible cost of a Path from a source to a
	 * destination.
	 * By definition, a guess G must be such that 0 <= G <= C where C is the
	 * actual least possibl cost of a Path from source to destination.
	 * @param source Source NODE.
	 * @param destination Destination NODE.
	 * @return Computed guess.
	 */
	public double getGuessForCost(Node source, Node destination) {
		return 0; // 0 is always a correct guess.
	}

	/**
	 * Computes a guess for the least possible value of a given constraint on a
	 * Path from a source to a destination.
	 * By definition, a guess G must be such that 0 <= G <= C where C is the
	 * actual least possible value of the constraint on a Path from source to
	 * destination.
	 * @param index Index of the desired constraint.
	 * @param source Source NODE.
	 * @param destination Destination NODE.
	 * @return Computed guess.
	 */
	public double getGuessForConstraint(int index, Node source, Node destination) {
		return 0; // 0 is always a valid guess.
	}

	/**
	 * Gets the type of the Proxy (EDGE, PreviousEdge or Path).
	 * The type might differ from the class of the object because PathProxies
	 * are used to wrap around other proxies in order to reduce code redundancy.
	 * For example, a PathProxy could be of EDGE type.
	 * @return Type of the Proxy (or wrapped Proxy).
	 */
	public abstract ProxyTypes getType();

	/**
	 * Gets the current Proxy.
	 * This might return an object different from current object if the current
	 * object is a wrapper or plumber around another Proxy.
	 * In this case, the wrapped/plumbed Proxy should be returned.
	 * @return Current Proxy.
	 */
	public Proxy getProxy() {
		return this;
	}

	@Override
	public Proxy clone() {
		try {
			return (Proxy) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Checks if values violate their bounds (numerical approximations are taken
	 * into account).
	 * @param values Array of values.
	 * @param bounds Array of bounds for the values.
	 * @return true if at least one value violates its bound, false otherwise.
	 */
	public static boolean violatesBound(double values[], double bounds[]) {
		return violatesBound(values, bounds, 10E-10);
	}

	public static boolean violatesBound(double values[], double bounds[], double accuracy) {
		for(int i = 0; i < values.length; i++)
			if(Proxy.violatesBound(values[i], bounds[i], accuracy))
				return true;
		return false;
	}

	/**
	 * Checks if a value violates its bound (numerical approximations are taken
	 * into account).
	 * @param value The value.
	 * @param bound Bound for the value.
	 * @return true if the value violates its bounds, false otherwise.
	 */
	public static boolean violatesBound(double value, double bound) {
		return violatesBound(value, bound, 10E-10);
	}

	public static boolean violatesBound(double value, double bound, double accuracy) {
		return !(value <= bound || Proxy.fuzzyEquals(value, bound, accuracy));
	}

	/**
	 * Checks if a value is equal to another one (numerical approximations are
	 * taken into account).
	 * @param value The value.
	 * @param correctValue The other value.
	 * @return true if the value is (roughly) equal to the other one, false
	 *         otherwise.
	 */
	public static boolean fuzzyEquals(double value, double correctValue) {
		return fuzzyEquals(value, correctValue, 10E-10);
	}

	/**
	 * Checks if a value is equal to another one (numerical approximations are
	 * taken into account).
	 * @param value The value.
	 * @param correctValue The other value.
	 * @return true if the value is (roughly) equal to the other one, false
	 *         otherwise.
	 */
	public static boolean fuzzyEquals(double value, double correctValue, double accuracy) {
		return (Math.abs(value - correctValue) < accuracy);
	}
}
