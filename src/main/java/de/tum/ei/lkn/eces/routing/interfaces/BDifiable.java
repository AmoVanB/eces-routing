package de.tum.ei.lkn.eces.routing.interfaces;

/**
 * An algorithm that can make use of BD.
 *
 * See: "Bounded Dijkstra (BD): Search Space Reduction for Expediting Shortest Path Subroutines" A Van Bemten, JW Guck,
 * CM Machuca, W Kellerer. 2019.
 */
public interface BDifiable {
	void enableBD();
	void disableBD();
}
