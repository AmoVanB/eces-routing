package de.tum.ei.lkn.eces.routing.proxies.plumbers;

import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.exceptions.ProxyException;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.Proxy;
import de.tum.ei.lkn.eces.routing.proxies.ProxyTypes;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;

/**
 * A PlumberProxy is a Proxy able to mix the variables computed
 * by another underlying Proxy.
 *
 * - The cost returned by the PlumberProxy can be defined as a linear
 *   combination of the cost and constraints of the underlying Proxy.
 * - The constraints returned by the PlumberProxy can be chosen among
 *   the constraints from the underlying Proxy. The cost of the underlying
 *   Proxy cannot be chosen as a constraint because no bound would be
 *   defined for it.
 * - The parameters returned by the PlumberProxy correspond to the
 *   parameters of the underlying Proxy to which can be appended
 *   the cost and constraints of the underlying Proxy.
 *
 * IDs of the values returned by the underlying Proxy are defined as
 * follows: 0 corresponds to the cost, 1 to the first constraint, 2
 * to the second constraint, and so on. No ID is defined for the
 * parameters of the underlying Proxy.
 */
public final class EdgePlumberProxy extends EdgeProxy {
	/**
	 * Underlying EdgeProxy.
	 */
	private EdgeProxy proxy;

	/**
	 * IDs of the values returned by the underlying Proxy which make up
	 * the new cost.
	 */
	private int[] costIDs;

	/**
	 * Coefficients of the linear combination of the costIDs to make up
	 * the new cost.
	 */
	private double[] costMultipliers;

	/**
	 * IDs of the values returned by the underlying Proxy which are now
	 * constraints.
	 */
	private int[] constraintsIDs;

	/**
	 * IDs of the values returned by the underlying Proxy which have to
	 * be added to the parameters list.
	 */
	private int[] additionalParametersIDs;

	/**
	 * Creates a new EdgePlumberProxy.
	 * @param costIDs IDs of the values returned by the underlying Proxy which
	 *                make up the new cost.
	 * @param costMultipliers Coefficients of the linear combination of the
	 *                        costIDs to make up the new cost.
	 * @param constraintsIDs IDs of the values returned by the underlying Proxy
	 *                       which are now constraints.
	 * @param additionalParametersIDs IDs of the values returned by the
	 *                                underlying Proxy which have to be added
	 *                                to the parameters list.
	 * @throws ProxyException - If the costIDs and costMultipliers lengths do
	 *                          not match.
	 *                        - If one of the specified IDs is negative.
	 *                        - If one of the specified IDs for the constraint
	 *                          is 0 (the cost cannot become a constraint).
	 */
	public EdgePlumberProxy(int[] costIDs, double[] costMultipliers, int[] constraintsIDs, int[] additionalParametersIDs) {
		if(costIDs.length != costMultipliers.length)
			throw new ProxyException("When creating a PlumberProxy the array of coefficients for the new cost must be of the same size as the array of IDs which are considered in the linear combination.");

		// Checking the provided IDs.
		for(int costID : costIDs)
			if(costID < 0)
				throw new ProxyException("IDs given to the PlumberProxy must be non-negative.");

		for(int constraintsID : constraintsIDs) {
			if(constraintsID < 0)
				throw new ProxyException("IDs given to the PlumberProxy must be non-negative.");
			if(constraintsID == 0)
				throw new ProxyException("The cost of the underlying Proxy cannot be used as a constraint for the PlumberProxy.");
				/* Reason for this: the Plumber would not be able to compute the
				 * bound of the constraint. */
		}

		for(int additionalParametersID : additionalParametersIDs)
			if(additionalParametersID < 0)
				throw new ProxyException("IDs given to the PlumberProxy must be non-negative.");

		this.costMultipliers = costMultipliers;
		this.costIDs = costIDs;
		this.constraintsIDs = constraintsIDs;
		this.additionalParametersIDs = additionalParametersIDs;
	}

	/**
	 * Creates a new EdgePlumberProxy.
	 * @param proxy Underlying Proxy.
	 * @param costIDs IDs of the values returned by the underlying Proxy which
	 *                make up the new cost.
	 * @param costMultipliers Coefficients of the linear combination of the
	 *                        costIDs to make up the new cost.
	 * @param constraintsIDs IDs of the values returned by the underlying Proxy
	 *                       which are now constraints.
	 * @param additionalParametersIDs IDs of the values returned by the
	 *                                underlying Proxy which have to be added
	 *                                to the parameters list.
	 * @throws ProxyException - If the costIDs and costMultipliers lengths do
	 *                          not match.
	 *                        - If one of the specified IDs is negative.
	 *                        - If one of the specified IDs for the constraint
	 *                          is 0 (the cost cannot become a constraint).
	 */
	public EdgePlumberProxy(EdgeProxy proxy, int[] costIDs, double[] costMultipliers, int[] constraintsIDs, int[] additionalParametersIDs) {
		this(costIDs, costMultipliers, constraintsIDs, additionalParametersIDs);
		this.setProxy(proxy);
	}

	/**
	 * Sets the underlying Proxy of the EdgePlumberProxy.
	 * @param proxy New underlying Proxy.
	 */
	public void setProxy(EdgeProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Updates the coefficients for the linear combination of the costIDs.
	 * @param costMultipliers New coefficients for the linear combination of
	 *                        the costIDs.
	 * @throws ProxyException If the costMultipliers length does not match
	 *                        the number of values involved in the linear
	 *                        combination of the new cost.
	 */
	public void setCostMultipliers(double[] costMultipliers) {
		if(costIDs.length != costMultipliers.length)
			throw new ProxyException("The new array of coefficients for the new cost must be of the same size as the array of IDs which are considered in the linear combination.");
		this.costMultipliers = costMultipliers;
	}

	/**
	 * Gets the values of the parameters of the underlying Proxy from the
	 * values of the parameters of the PlumberProxy.
	 * @param parameters Parameters of the PlumberProxy.
	 * @param request Considered Request.
	 * @return Parameters for the underlying Proxy.
	 * @throws ProxyException if the number of parameters provided does not
	 *                        match the number of parameters of the Plumber
	 *                        Proxy.
	 */
	public double[] removePlumberParameters(double parameters[], Request request) {
		if(parameters == null)
			return null;

		/* The PlumberProxy just appends parameters to the parameters array of
		 * the underlying Proxy. We therefore simply have to truncate the
		 * array of parameters of the PlumberProxy. */
		int numUnderlyingParameters = proxy.getNumberOfParameters(request);
		if(parameters.length != numUnderlyingParameters + additionalParametersIDs.length)
			throw new ProxyException("Wrong number of parameters provided to PlumberProxy (" + parameters.length + " instead of " + numUnderlyingParameters + " + " + additionalParametersIDs.length + ").");
		double[] underlyingParameters = new double[numUnderlyingParameters];
		System.arraycopy(parameters, 0, underlyingParameters, 0, numUnderlyingParameters);
		return underlyingParameters;
	}

	/**
	 * Gets the ID of a parameter for the PlumberProxy.
	 * @param addedParametersId ID of the parameter among the additional
	 *                          parameters. 0 means first additional parameter,
	 *                          1 means second additional parameter, and so on.
	 * @param request Request for which ID translation must be done.
	 * @return The ID of the parameter for the PlumberProxy.
	 */
	public int getPlumberParameterId(int addedParametersId, UnicastRequest request) {
		return addedParametersId + proxy.getNumberOfParameters(request);
	}

	@Override
	public double[] getNewParameters(Edge edge, double[] oldParameters, Request request, boolean isForward) {
		// Getting underlying parameters, cost and constraints.
		int numUnderlyingParameters = proxy.getNumberOfParameters(request);
		double[] oldUnderlyingParameters = removePlumberParameters(oldParameters, request);
		double[] newUnderlyingParameters = proxy.getNewParameters(edge, oldUnderlyingParameters, request, isForward);
		if(newUnderlyingParameters == null)
			return null;
		double[] underlyingConstraints = proxy.getConstraintsValues(edge, request);

		// Copy the underlying parameters as the first new parameters.
		double[] newPlumberParameters = new double[numUnderlyingParameters + additionalParametersIDs.length];
		System.arraycopy(newUnderlyingParameters, 0, newPlumberParameters, 0, numUnderlyingParameters);

		// Adding the additional parameters.
		for(int i = 0; i < additionalParametersIDs.length; i++) {
			if(additionalParametersIDs[i] == 0)
				newPlumberParameters[numUnderlyingParameters + i] = proxy.getCost(edge, request);
			else
				newPlumberParameters[numUnderlyingParameters + i] = underlyingConstraints[additionalParametersIDs[i] - 1];

			/* Since the new parameters are constraints or the cost of the
			 * underlying proxy, they are additive parameters. */
			if(oldParameters != null)
				newPlumberParameters[numUnderlyingParameters + i] = newPlumberParameters[numUnderlyingParameters + i] + oldParameters[numUnderlyingParameters + i];
		}

		return newPlumberParameters;
	}

	@Override
	public boolean hasAccess(Edge edge, Request request) {
		return proxy.hasAccess(edge, request);
	}

	@Override
	public double getCost(Edge edge, Request request) {
		double[] underlyingConstraints = proxy.getConstraintsValues(edge, request);

		// Computing the linear combination.
		double plumberCost = 0;
		for(int i = 0; i < costIDs.length; i++) {
			if(costIDs[i] == 0)
				plumberCost += costMultipliers[0] * proxy.getCost(edge, request);
			else
				plumberCost += costMultipliers[i] * underlyingConstraints[costIDs[i] - 1];
		}

		return plumberCost;
	}

	@Override
	public double[] getConstraintsValues(Edge edge, Request request) {
		double[] underlyingConstraints = proxy.getConstraintsValues(edge, request);
		double[] plumberConstraints = new double[constraintsIDs.length];
		for(int i = 0; i < constraintsIDs.length; i++)
				plumberConstraints[i] = underlyingConstraints[constraintsIDs[i] - 1];
		return plumberConstraints;
	}

	@Override
	public double[] getConstraintsBounds(Request request) {
		double[] underlyingConstraintsBounds = proxy.getConstraintsBounds(request);
		double[] plumberConstraints = new double[constraintsIDs.length];
		for(int i = 0; i < constraintsIDs.length; i++)
				plumberConstraints[i] = underlyingConstraintsBounds[constraintsIDs[i] - 1];
		return plumberConstraints;
	}

	@Override
	public boolean register(Edge edge, Request request) {
		return ((EdgeProxy) proxy).register(edge, request);
	}

	@Override
	public boolean deregister( Edge edge, Request request) {
		return ((EdgeProxy) proxy).deregister(edge, request);
	}

	@Override
	public boolean handle(Request request, boolean isForward) {
		return proxy.handle(request, isForward);
	}

	@Override
	public int getNumberOfConstraints(Request request) {
		return constraintsIDs.length;
	}

	@Override
	public int getNumberOfParameters(Request request) {
		return proxy.getNumberOfParameters(request) + additionalParametersIDs.length;
	}

	@Override
	public double getGuessForCost(Node source, Node destination) {
		// Computing the linear combination.
		double plumberGuessForCost = 0;
		for(int i = 0; i < costIDs.length; i++) {
			if(costIDs[i] == 0)
				plumberGuessForCost += costMultipliers[0] * proxy.getGuessForCost(source, destination);
			else
				plumberGuessForCost += costMultipliers[i] * proxy.getGuessForConstraint(costIDs[i] - 1, source, destination);
		}

		return plumberGuessForCost;
	}

	@Override
	public double getGuessForConstraint(int index, Node source, Node destination) {
		return proxy.getGuessForConstraint(constraintsIDs[index] - 1, source, destination);
	}

	@Override
	public ProxyTypes getType() {
		return proxy.getType();
	}

	@Override
	public Proxy getProxy() {
		return this.proxy.getProxy();
	}

	@Override
	public EdgePlumberProxy clone() {
		EdgePlumberProxy clone = (EdgePlumberProxy) super.clone();
		clone.proxy = proxy.clone();
		return clone;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.proxy.toString() + "]";
	}
}
