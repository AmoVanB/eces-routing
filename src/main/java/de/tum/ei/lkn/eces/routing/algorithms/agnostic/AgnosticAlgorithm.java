package de.tum.ei.lkn.eces.routing.algorithms.agnostic;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;

/**
 * An agnostic algorithm does not know the problem it is solving but is simply
 * an algorithm modifying an existing sub-algorithm which solves a given problem.
 * The problem solved by the sub-algorithm does not matter to the agnostic algorithm.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class AgnosticAlgorithm extends RoutingAlgorithm {
	protected AgnosticAlgorithm(Controller controller) {
		super(controller);
	}
}
