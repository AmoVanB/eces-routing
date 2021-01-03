package de.tum.ei.lkn.eces.routing.distancevector;

import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.routing.algorithms.sp.unicast.dijkstra.DijkstraAlgorithm;
import de.tum.ei.lkn.eces.routing.proxies.EdgeProxy;
import de.tum.ei.lkn.eces.routing.proxies.ShortestPathProxy;
import de.tum.ei.lkn.eces.routing.requests.UnicastRequest;
import de.tum.ei.lkn.eces.routing.responses.Path;

/**
 * System attaching to each Node of a Graph a DistanceVector Component storing
 * the distance from this Node until all other Nodes. The distance is measured
 * in terms of cost.
 *
 * Distances must be manually updated with the update() method.
 * Cost is computed per-default using a ShortestPathProxy but any other proxy can be passed.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class DistanceVectorSystem extends RootSystem {
	/**
	 * Mapper handling the DistanceVector Components.
	 */
	private DistanceVectorMapper distanceVectorMapper;

	/**
	 * DijkstraAlgorithm used by the System.
	 */
	private DijkstraAlgorithm dijkstra;

	/**
	 * Creates a new DistanceVectorSystem.
	 * @param controller Controller responsible for the System.
	 * @param proxy Proxy to be used by the System to compute the costs.
	 */
	public DistanceVectorSystem(Controller controller, EdgeProxy proxy) {
		super(controller);
		distanceVectorMapper = new DistanceVectorMapper(controller);
		dijkstra = new DijkstraAlgorithm(controller);
		dijkstra.setProxy(proxy);
	}

	/**
	 * Creates a new DistanceVectorSystem. The System will use the hop count
	 * as a distance metric.
	 * @param controller Controller responsible for the System.
	 */
	public DistanceVectorSystem(Controller controller) {
		this(controller, new ShortestPathProxy());
	}

	/**
	 * When a new Node is created, adds a new empty list of distances to the
	 * Node's entity. We do not run the update method automatically because
	 * this is a heavy task. The user is supposed to do this himself when he
	 * wants up to date values. Also, adding a Node in a Network will not change
	 * anything in the distances.
	 * @param node The newly created Node.
	 */
	@ComponentStateIs(State = ComponentStatus.New)
	void addNode(Node node) {
		distanceVectorMapper.attachComponent(node, new DistanceVector(controller));
	}

	/**
	 * When a new Node is deleted, removes the DistanceVector Component from
	 * this NODE's Entity and remove this NODE in the DistanceVector
	 * Component of all other Nodes in the Network. We do not run the update
	 * method automatically because this is a heavy task. The user is supposed
	 * to do this himself when he wants up to date values.
	 * @param node The newly created Node.
	 */
	@ComponentStateIs(State = ComponentStatus.Destroyed)
	void removeNode(Node node) {
		distanceVectorMapper.detachComponent(node);
	}

	/**
	 * Updates the distances of the Nodes of a given Node's Graph.
	 * Dijkstra is used.
	 * @param node The Node whose Graph's Nodes must be updated.
	 */
	public void update(Node node) {
		update(node.getGraph());
	}

	/**
	 * Updates the distances of the Nodes of a given EDGE's Graph.
	 * Dijkstra is used.
	 * @param edge The EDGE whose Graph's Nodes must be updated.
	 */
	public void update(Edge edge) {
		update(edge.getSource().getGraph());
	}

	/**
	 * Updates the distances of the Nodes of a given Graph.
	 * Dijkstra is used.
	 * @param graph The Graph whose Nodes must be updated.
	 */
	public void update(Graph graph) {
		for(Node node : graph.getNodes()) {
			dijkstra.computePathsToAnyNodeFrom(node,new UnicastRequest(null, node));
			for(Node node2 : graph.getNodes()) {
				DistanceVector distanceVector = distanceVectorMapper.get(node2.getEntity());
				if(distanceVector == null) {
					distanceVector = new DistanceVector(controller);
					distanceVectorMapper.attachComponent(node2, distanceVector);
				}

				DistanceVector dv = distanceVector;
				distanceVectorMapper.updateComponent(dv, () -> {
					Path path = dijkstra.getPathFromNodeTo(node2);
					if(path == null)
						dv.setDistance(node, Double.MAX_VALUE);
					else
						dv.setDistance(node, path.getCost());
				});
			}
		}
	}
}
