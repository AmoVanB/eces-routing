package de.tum.ei.lkn.eces.routing.mocks;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.routing.requests.Request;

public class GridPathProxy extends DummyPathProxy {

	public GridPathProxy(Controller controller) {
		super(controller);
	}

	@Override
	public boolean hasAccess(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		super.pathTest(path,edge,isForward);
		Edge pe = getFirstEdge(path);
		if(pe != null && pe.getId() % 2 == edge.getId() % 2)
			return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.getOptimistic(edge.getEntity()).count < 100;
		return dummyMapper.getOptimistic(edge.getEntity()).use && dummyMapper.getOptimistic(edge.getEntity()).count < 80;
	}

	@Override
	public double getCost(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		super.pathTest(path,edge,isForward);
		Edge pe = getFirstEdge(path);
		DummyComponent dc = dummyMapper.getOptimistic(edge.getEntity());
		if(pe != null && (pe.getId()+1) % 2 == edge.getId() % 2)
			return dc.cost * 2;
		return dc.cost;
	}

	@Override
	public double[] getConstraintsValues(Iterable<Edge> path, Edge edge, double[] oldParameters, Request request, boolean isForward) {
		super.pathTest(path,edge,isForward);
		Edge pe = getFirstEdge(path);
		DummyComponent data = dummyMapper.getOptimistic(edge.getEntity());
		double constr[] = new double[]{
				data.delay,
				data.loss,
				data.count,
				100};

		return constr;
	}

	private Edge getFirstEdge(Iterable<Edge> path){
		Edge edge = null;
		for(Edge z :path){
			edge = z;
		}
		return edge;
	}
}
