package de.tum.ei.lkn.eces.routing.algorithms.mcsp.astarprune;

/**
 * Data stored at each Node during the A*Prune algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class AStarNodeData {
	/**
	 * Guess distances from current Node to the destination.
	 * Has one more element than 'constraintsToDestination' because it
	 * also contains the cost.
	 */
	private double[] admissibleDistancesToDestination;

	public AStarNodeData() {
		init();
	}

	public void init() {
		admissibleDistancesToDestination = null;
	}

	public double[] getAdmissibleDistancesToDestination() {
		return admissibleDistancesToDestination;
	}

	public void setAdmissibleDistancesToDestination(double[] admissibleDistancesToDestination) {
		this.admissibleDistancesToDestination = admissibleDistancesToDestination;
	}
}
