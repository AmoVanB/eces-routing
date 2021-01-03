package de.tum.ei.lkn.eces.routing.algorithms.mcsp.upqa;

/**
 * Elements where UPQA should store data.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public enum QueueMode {
	/*
	 * For each node like traditional Dijkstra.
	 */
	NODE,
	/*
	 * For each edge like edge-based Dijkstra described in "Routing metrics depending on previous edges: The Mn taxonomy
	 * and its corresponding solutions" A Van Bemten, JW Guck, CM Machuca, W Kellerer. 2018.
	 */
	EDGE,
	/*
	 * Keep track of each path, for Minfinity metrics.
	 */
	PATH,
	/*
	 * Automatically based on proxy type.
	 */
	AUTO,
	/*
	 * Like path but removes loops.
	 */
	PATH_LOOP_DETECTION
}
