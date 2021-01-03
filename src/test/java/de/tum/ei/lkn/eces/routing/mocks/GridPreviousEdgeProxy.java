package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.requests.Request;

public class GridPreviousEdgeProxy extends DummyPreviousEdgeProxy {

	public GridPreviousEdgeProxy(Controller controller) {
		super(controller);
	}

	@Override
	public boolean hasAccess(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		if((isForward && pe != null && pe.getId() % 2 == 0) ||
			(!isForward && pe != null && edge.getId() % 2 == 0)	)
			return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.get(edge.getEntity()).count < 100;
		return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.get(edge.getEntity()).count < 80;
	}

	@Override
	public double getCost(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		DummyComponent dc = dummyMapper.getOptimistic(edge.getEntity());
		if((isForward && pe != null && pe.getId() % 4 == 0) ||
				(!isForward && pe != null && edge.getId() % 4 == 0)	)
			return dc.cost * 2;
		return dc.cost;
	}

	@Override
	public double[] getConstraintsValues(Edge pe, Edge edge, Request request, boolean isForward) {
		pEdgeTest(pe,edge,isForward);
		DummyComponent data = dummyMapper.getOptimistic(edge.getEntity());
		double[] constr = new double[] {
				data.delay,
				data.loss,
				data.count,
				100};
		if((isForward && pe != null && pe.getId() % 4 == 1) ||
				(!isForward && pe != null && edge.getId() % 4 == 1)	)
			for(int i = 0; i < constr.length; i++)
				constr[i] = constr[i] * 2;
		return constr;
	}
}
