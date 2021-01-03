package de.tum.ei.lkn.eces.routing.distancevector;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.LocalMapper;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Node;

/**
 * Component that stores, for each Graph NODE, a distance to this NODE.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = DistanceVectorSystem.class)
public class DistanceVector extends Component {
	private LocalMapper<DistanceVectorData> nodeDataMapper;

	public DistanceVector(Controller controller){
		nodeDataMapper = controller.getLocalMapper(this, DistanceVectorDataLocal.class);
	}

	/**
	 * Gets the distance to a specific destination.
	 * @param destination Destination Node.
	 * @return Distance to the destination Node.
	 */
	public double getDistance(Node destination) {
		return nodeDataMapper.get(destination.getEntity()).getHopCount();
	}

	/**
	 * Sets the distance to a specific destination.
	 * @param destination Destination Node.
	 * @param value Distance to set.
	 */
	protected void setDistance(Node destination, double value) {
		nodeDataMapper.get(destination.getEntity()).setHopCount(value);
	}
}
