package de.tum.ei.lkn.eces.routing.algorithms.mcsp.hmcop;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.QueueMode;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.Record;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.TempData;
import de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa.UniversalPriorityQueueAlgorithm;
import de.tum.ei.lkn.eces.routing.exceptions.RoutingException;
import de.tum.ei.lkn.eces.routing.interfaces.BDifiable;
import de.tum.ei.lkn.eces.routing.proxies.*;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

import java.util.LinkedList;

/**
 * The H_MCOP algorithm.
 *
 * 2001
 * "Multi-constrained optimal path selection"
 * T. Korkmaz and M. Krunz.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class HMCOPAlgorithm extends UniversalPriorityQueueAlgorithm implements BDifiable {
	/**
	 * Whether BD is enabled or not.
	 */
	protected boolean BDFeature = false;

	protected HMCOPReverseAlgorithm preRunAlgorithm;
	private double lambda;

	public HMCOPAlgorithm(Controller controller, double lambda) {
		this(controller,lambda, true, ProxyTypes.EDGE_PROXY, 1);
	}

	public HMCOPAlgorithm(Controller controller, double lambda, int k) {
		this(controller,lambda, true, ProxyTypes.EDGE_PROXY, k);
	}

	public HMCOPAlgorithm(Controller controller, double lambda, boolean guessMode, ProxyTypes proxyType, int k) {
		super(controller, QueueMode.NODE, true, guessMode, k);
		switch (proxyType) {
			case PREVIOUS_EDGE_PROXY:
				this.queueMode = QueueMode.EDGE;
			case PATH_PROXY:
				throw new RoutingException("H_MCOP dose never support a path proxy!");
		}
		this.enableStopOnFirstKPath();
		preRunAlgorithm = new HMCOPReverseAlgorithm(controller, proxyType);
		this.lambda = lambda;
	}

	@Override
	protected TempData getNewTempData() {
		if(guessMode){
			return new HMCOPTempDataGuess();
		} else {
			return new HMCOPTempData();
		}
	}

	public Path computePath(UnicastRequest request) {
		if(BDFeature) {
			preRunAlgorithm.setCostBorder(proxy.getNumberOfConstraints(request));
			preRunAlgorithm.computePathsFromAnyNodeTo(request.getDestination(), request);
			preRunAlgorithm.removeCostBorder();
			if(preRunAlgorithm.getPathToNodeFrom(request.getSource()) == null)
				return null;
		}
		else {
			preRunAlgorithm.computePathsFromAnyNodeTo(request.getDestination(),request);
			if(preRunAlgorithm.getPathToNodeFrom(request.getSource()) == null ||
					Proxy.violatesBound(preRunAlgorithm.getPathToNodeFrom(request.getSource()).getCost(), proxy.getNumberOfConstraints(request)))
				return null;
		}

		Path result = computePath(request, new LinkedList<>());
		if(result == null || Proxy.violatesBound(result.getConstraintsValues(), proxy.getConstraintsBounds(request)))
			return null;
		return result;
	}

	private boolean checkAllConstraints(double[] constSoFar, double[] constToDest, double[] bounds){
		for(int i = 0; i < bounds.length; i++) {
			if(Proxy.violatesBound(constSoFar[i]+constToDest[i], bounds[i]))
				return false;
		}
		return true;
	}

	@Override
	protected TempData relaxNode(Edge nextEdge, TempData data, UnicastRequest request, double[] parameters, double[] newConstraints) {
		Record record = recordLocalMapper.get(nextEdge.getDestination().getEntity());
		int index = record.getMaxIDnotVisited();
		if(index == -1)
			return null;
		TempData tempData = record.getTempData(index);

		double[] bounds = proxy.getConstraintsBounds(request);
		Path pathToDest = preRunAlgorithm.getPathToNodeFrom(nextEdge.getDestination());
		double[] constToDest;
		if(pathToDest == null) {
			constToDest = new double[bounds.length];
			for(int i = 0; i < bounds.length; i++)
				constToDest[i] = Double.POSITIVE_INFINITY;

		}
		else
			constToDest = preRunAlgorithm.getPathToNodeFrom(nextEdge.getDestination()).getConstraintsValues();

		double g = 0;
		double newCost = computeCost(nextEdge, data, request, parameters, newConstraints);
		if(lambda == Double.POSITIVE_INFINITY) {
			for(int i = 0; i < bounds.length; i++)
				g = Math.max((newConstraints[i]+constToDest[i])/ bounds[i],g);
		}
		else {
			for(int i = 0; i < bounds.length; i++)
				g += Math.pow((newConstraints[i]+constToDest[i])/ bounds[i],lambda);
		}

		boolean newValid = checkAllConstraints(newConstraints, constToDest, bounds);

		if(tempData != null) {
			if((newCost < tempData.getCost() || newCost == Double.NaN) && newValid);
			else if((newCost > tempData.getCost() || newCost == Double.NaN) && tempData.isValid())
				return null;
			else if((guessMode && g < ((HMCOPTempDataGuess) tempData).getG()) ||
					(!guessMode && g < ((HMCOPTempData) tempData).getG()));
			else
				return null;
			tempData.setVisited(true);
		}

		tempData = this.getNewTempData();
		record.getArray()[index] = tempData;
		tempData.setCost(newCost);

		if(guessMode) {
			((HMCOPTempDataGuess) tempData).setG(g);
			((HMCOPTempDataGuess) tempData).setValid(newValid);
		} else {
			((HMCOPTempData) tempData).setG(g);
			((HMCOPTempData) tempData).setValid(newValid);
		}
		return tempData;
	}

	public boolean isValid(){
		return true;
	}

	public boolean isOptimal(){
		return false;
	}

	@Override
	public boolean isComplete() {
		return proxy.getType() == ProxyTypes.EDGE_PROXY;
	}

	@Override
	public boolean handle(Request request) {
		return this.getProxy().handle(request, isForward());
	}

	/**
	 * Sets the Algorithm in EdgeProxy mode.
	 * @param edgeProxy Proxy to use.
	 */
	public void setProxy(EdgeProxy edgeProxy) {
		this.preRunAlgorithm.setProxy(edgeProxy);
		super.setProxy(edgeProxy);
	}

	/**
	 * Sets the Algorithm in PreviousEdgeProxy mode.
	 * @param previousEdgeProxy Proxy to use.
	 */
	public void setProxy(PreviousEdgeProxy previousEdgeProxy) {
		this.preRunAlgorithm.setProxy(previousEdgeProxy);
		super.setProxy(previousEdgeProxy);
	}

	/**
	 * Sets the Algorithm in PathProxy mode.
	 * @param pathProxy Proxy to use.
	 */
	public void setProxy(PathProxy pathProxy) {
		this.preRunAlgorithm.setProxy(pathProxy);
		super.setProxy(pathProxy);
	}

	@Override
	public void enableBD() {
		this.BDFeature = true;
	}

	@Override
	public void disableBD() {
		this.BDFeature = false;
	}
}
