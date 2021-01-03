package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.MCSPAlgorithm;
import de.tum.ei.lkn.eces.routing.algorithms.sp.SPAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.UnableToHandleRequestException;
import de.tum.ei.lkn.eces.routing.interfaces.NToOneAlgorithm;
import de.tum.ei.lkn.eces.routing.interfaces.SolveUnicastRequest;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.proxies.plumbers.PathPlumberProxy;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.PriorityQueue;

/**
 * The A*Prune algorithm.
 *
 * 2001
 * "A*Prune: An Algorithm for Finding K Shortest Paths Subject to Multiple
 * Constraints"
 * Gang Liu and K. G. Ramakrishnan.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class AStarPruneAlgorithm extends MCSPAlgorithm implements SolveUnicastRequest {
	/**
	 * LocalMapper handling the A*Prune AStarNodeData LocalComponents.
	 */
	private LocalMapper<AStarNodeData> AStarDataLocalMapper;

	/**
	 * Maximum number of metrics (1 (cost) + number of constraints) that can
	 * be handled by the Algorithm.
	 */
	private int maxNumberOfMetrics;

	/**
	 * Algorithms used to compute the least-cost paths from all nodes to the
	 * destination.
	 * Index 0 is used for the real cost, and indices > 0 are used with the
	 * individual constraints as cost.
	 */
	private NToOneAlgorithm[] allToDestinationGuessAlgorithms;

	/**
	 * Algorithms used to compute the least-cost paths from a node to the
	 * destination.
	 * Index 0 is used for the real cost, and indices > 0 are used with the
	 * individual constraints as cost.
	 */
	private SPAlgorithm[] SPGuessAlgorithms;

	/**
	 * Guess mode in use.
	 */
	private GuessModes mode;

	private boolean proxyInit = false;

	public AStarPruneAlgorithm(Controller controller, boolean uniformMode) {
		super(controller);
		this.AStarDataLocalMapper = controller.getLocalMapper(this, AStarNodeDataLocal.class);
		this.maxNumberOfMetrics = Integer.MAX_VALUE;
		if(uniformMode)
			this.mode = GuessModes.UNIFORM;
		else
			this.mode = GuessModes.PROXY;
	}

	public AStarPruneAlgorithm(Controller controller, NToOneAlgorithm[] allToDestinationGuessAlgorithms) {
		super(controller);
		this.AStarDataLocalMapper = controller.getLocalMapper(this, AStarNodeDataLocal.class);
		this.maxNumberOfMetrics = allToDestinationGuessAlgorithms.length;
		this.allToDestinationGuessAlgorithms = allToDestinationGuessAlgorithms;
		this.mode = GuessModes.N_TO_ONE;
	}

	public AStarPruneAlgorithm(Controller controller, SPAlgorithm[] SPGuessAlgorithms) {
		super(controller);
		this.AStarDataLocalMapper = controller.getLocalMapper(this, AStarNodeDataLocal.class);
		this.maxNumberOfMetrics = SPGuessAlgorithms.length;
		this.SPGuessAlgorithms = SPGuessAlgorithms;
		this.mode = GuessModes.ONE_TO_ONE;
	}

	@Override
	public void setProxy(EdgeProxy edgeProxy) {
		super.setProxy(edgeProxy);
		proxyInit = false;
	}

	@Override
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		super.setProxy(previousEdgeProxy);
		proxyInit = false;
	}

	@Override
	public void setProxy(PathProxy pathProxy) {
		super.setProxy(pathProxy);
		proxyInit = false;
	}

	private void setProxy(Request request) {
		if(!this.proxyInit) {
			this.maxNumberOfMetrics = proxy.getNumberOfConstraints(request) + 1;
			// Setting the PlumberProxies to the corresponding algorithms.
			for (int i = 0; i < maxNumberOfMetrics; i++) {
				int[] costIDs = new int[]{i};
				double[] costMultipliers = new double[]{1};
				int[] constraintsIDs = new int[0];
				int[] additionalParametersIDs = new int[0];
				if (mode == GuessModes.ONE_TO_ONE)
					this.SPGuessAlgorithms[i].setProxy(new PathPlumberProxy(proxy, costIDs, costMultipliers, constraintsIDs, additionalParametersIDs));
				else if (mode == GuessModes.N_TO_ONE)
					((SPAlgorithm) this.allToDestinationGuessAlgorithms[i]).setProxy(new PathPlumberProxy(proxy, costIDs, costMultipliers, constraintsIDs, additionalParametersIDs));
			}
			this.proxyInit = true;
		}
	}

	@Override
	public boolean handle(Request request) {
		//return this.getProxy().handle(request, this.isForward()) && this.getProxy().getNumberOfConstraints(request) > maxNumberOfMetrics - 1;
		return this.getProxy().handle(request, this.isForward());
	}

	@Override
	protected Response solveNoChecks(Request request) {
		return this.computePath((UnicastRequest) request);
	}
	@Override
	public Response solveNoChecks(UnicastRequest request) {
		return this.computePath( request);
	}
	public synchronized Path computePath(UnicastRequest request) {
		setProxy(request);
		int numberOfConstraints = proxy.getNumberOfConstraints(request);
		if(numberOfConstraints > (maxNumberOfMetrics - 1))
			throw new UnableToHandleRequestException("The request asks for " + numberOfConstraints + " constraints to be satisfied but the current instance of A*Prune can only handle " + (maxNumberOfMetrics - 1));

		/* 1. Pre-computing guesses if necessary. */
		if(mode == GuessModes.N_TO_ONE) {
			for(int i = 0; i < numberOfConstraints + 1; i++)
				allToDestinationGuessAlgorithms[i].computePathsFromAnyNodeTo(request.getDestination(), request);

			// Initializing data at each Node.
			for(Node node : request.getSource().getGraph().getNodes()) {
				double[] guesses = new double[numberOfConstraints + 1];
				for(int i = 0; i < numberOfConstraints + 1; i++) {
					if(allToDestinationGuessAlgorithms[i].getPathToNodeFrom(node) == null)
						guesses[i] = Double.MAX_VALUE;
					else
						guesses[i] = allToDestinationGuessAlgorithms[i].getPathToNodeFrom(node).getCost();
				}
				AStarNodeData nodeData = AStarDataLocalMapper.get(node.getEntity());
				nodeData.init();
				nodeData.setAdmissibleDistancesToDestination(guesses);
			}
		}
		else if(mode == GuessModes.UNIFORM) {
			// Initializing data at each Node.
			double[] guesses = new double[numberOfConstraints + 1];
			for(int i = 0; i < numberOfConstraints + 1; i++)
				guesses[i] = 0;
			for(Node node : request.getSource().getGraph().getNodes()) {
				AStarNodeData nodeData = AStarDataLocalMapper.get(node.getEntity());
				nodeData.init();
				nodeData.setAdmissibleDistancesToDestination(guesses);
			}
		}
		else {
			// Initializing data at each Node.
			for(Node node : request.getSource().getGraph().getNodes()) {
				AStarNodeData nodeData = AStarDataLocalMapper.get(node.getEntity());
				nodeData.init();
			}
		}

		/* 2. */
		// Initialize priority queue with a first empty head path.
		HeadPath initPath = new HeadPath(request.getSource());
		initPath.setCost(0);
		double[] constraintsValues = new double[numberOfConstraints];
		for(int i = 0; i < numberOfConstraints; i++)
			constraintsValues[i] = 0;
		initPath.setConstraintsValues(constraintsValues);

		// Initialize source.
		AStarNodeData nodeData = AStarDataLocalMapper.get(request.getSource().getEntity());
		// Compute guesses if mode requires so.
		if(mode == GuessModes.ONE_TO_ONE) {
			double[] distancesToDestination = new double[numberOfConstraints + 1];
			for(int i = 0; i < numberOfConstraints + 1; i++) {
				Path path = (Path) SPGuessAlgorithms[i].solve(request);
				if(path == null)
					distancesToDestination[i] = Double.MAX_VALUE;
				else
					distancesToDestination[i] = path.getCost();
			}
			nodeData.init();
			nodeData.setAdmissibleDistancesToDestination(distancesToDestination);
		}
		if (mode == GuessModes.PROXY) {
			double[] distancesToDestination = new double[numberOfConstraints + 1];
			for(int i = 0; i < numberOfConstraints + 1; i++) {
				if (i == 0)
					distancesToDestination[i] = proxy.getGuessForCost(request.getSource(), request.getDestination());
				else
					distancesToDestination[i] = proxy.getGuessForConstraint(i - 1, request.getSource(), request.getDestination());
			}
			nodeData.init();
			nodeData.setAdmissibleDistancesToDestination(distancesToDestination);
		}

		initPath.setCost(0);
		initPath.setProjectedCost(nodeData.getAdmissibleDistancesToDestination()[0]);
		initPath.setParametersValues(null);
		initPath.setId(0);

		PriorityQueue<HeadPath> pq = new PriorityQueue<>();
		pq.add(initPath);
		int id = 0;

		/* lines 3-28. */
		while(!pq.isEmpty()) {
			/* lines 4-6. */
			HeadPath headPath = pq.poll();

			/* lines 7-11. */
			if(headPath.getLastNode() == request.getDestination())
				return headPath.getPath();

			/* lines 13-27. */
			for(Edge outEdge : headPath.getLastNode().getOutgoingConnections()) {
				// Check access to the edge.
				HeadPathIterator iterator = new HeadPathIterator(headPath);
				double[] newParameters = proxy.getNewParameters(iterator, outEdge, headPath.getParametersValues(), request, this.isForward());
				if(!proxy.hasAccess(iterator, outEdge, newParameters, request, this.isForward()))
					continue;

				/* lines 15-16. */
				HeadPath expandedPath = new HeadPath(headPath, outEdge);
				expandedPath.setParametersValues(newParameters);
				expandedPath.setId(++id);

				/* lines 18-19. */
				if(expandedPath.containsLastNode())
					continue;

				/* line 17. */
				expandedPath.setCost(headPath.getCost() + proxy.getCost(iterator, outEdge, newParameters, request, this.isForward()));
				double[] edgeConstraintsValues = proxy.getConstraintsValues(iterator, outEdge, newParameters, request, this.isForward());
				double[] newConstraintsValues = new double[numberOfConstraints];
				for(int i = 0; i < numberOfConstraints; i++)
					newConstraintsValues[i] = headPath.getConstraintsValues()[i] + edgeConstraintsValues[i];
				expandedPath.setConstraintsValues(newConstraintsValues);

				/* lines 21-24. */
				boolean constraintViolated = false;
				AStarNodeData destinationNodeData = AStarDataLocalMapper.get(outEdge.getDestination().getEntity());
				// Compute guesses if mode requires so.
				if(destinationNodeData.getAdmissibleDistancesToDestination() == null) {
					UnicastRequest partRequest = request.clone();
					partRequest.setSource(outEdge.getDestination());
					if(mode == GuessModes.ONE_TO_ONE) {
						double[] distancesToDestination = new double[numberOfConstraints + 1];
						for(int i = 0; i < numberOfConstraints + 1; i++) {
							if(partRequest.getSource() == partRequest.getDestination())
								distancesToDestination[i] = 0;
							else {
								Path path = (Path) SPGuessAlgorithms[i].solve(partRequest);
								if(path == null)
									distancesToDestination[i] = Double.MAX_VALUE;
								else
									distancesToDestination[i] = path.getCost();
							}
						}
						destinationNodeData.init();
						destinationNodeData.setAdmissibleDistancesToDestination(distancesToDestination);
					}

					if(mode == GuessModes.PROXY) {
						double[] distancesToDestination = new double[numberOfConstraints + 1];
						for(int i = 0; i < numberOfConstraints + 1; i++) {
							if(outEdge.getDestination() == request.getDestination())
								distancesToDestination[i] = 0;
							else {
								if(i == 0)
									distancesToDestination[i] = proxy.getGuessForCost(outEdge.getDestination(), request.getDestination());
								else
									distancesToDestination[i] = proxy.getGuessForConstraint(i - 1, outEdge.getDestination(), request.getDestination());
							}
						}
						destinationNodeData.init();
						destinationNodeData.setAdmissibleDistancesToDestination(distancesToDestination);
					}
				}

				expandedPath.setProjectedCost(expandedPath.getCost() + destinationNodeData.getAdmissibleDistancesToDestination()[0]);
				double[] projectedConstraintsValues = new double[numberOfConstraints];
				for(int i = 0; i < numberOfConstraints; i++) {
					projectedConstraintsValues[i] = expandedPath.getConstraintsValues()[i] + destinationNodeData.getAdmissibleDistancesToDestination()[i + 1];
				}

				if(Proxy.violatesBound(projectedConstraintsValues, proxy.getConstraintsBounds(request)))
					continue; // Go to next Edge if one constraint is violated.

				/* lines 25-26. */
				pq.add(expandedPath);
			}
		}

		// Nothing found, return null.
		return null;
	}

	@Override
	public boolean isForward() {
		return true;
	}

	@Override
	public boolean isOptimal() {
		switch(mode){
			case N_TO_ONE:
			case ONE_TO_ONE:
				return proxy.getType() == ProxyTypes.EDGE_PROXY;
			case UNIFORM:
			case PROXY:
				return true;
		}
		return false;
	}

	@Override
	public boolean isComplete() {
		return isOptimal();
	}

	@Override
	public boolean isValid() {
		return true;
	}
}

/**
 * The four different ways in which A*Prune can compute guesses to the
 * destination.
 */
enum GuessModes {
	// Using an NToOneAlgorithm so that it has to be run only once per value.
	N_TO_ONE,
	/* Using an OneToOneAlgorithm. It has to be run for each source and value
	 * for which a guess has to be computed. */
	ONE_TO_ONE,
	// Always use 0 as guess.
	UNIFORM,
	// Ask the Proxy for the guess.
	PROXY
}
