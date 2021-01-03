package de.tum.ei.lkn.eces.routing.responses;

import java.util.Set;

/**
 * Interface for responses which are a set of paths.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public interface IndependentSetOfPaths {
	Set<Path> getPaths();
}
