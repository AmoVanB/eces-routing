package de.tum.ei.lkn.eces.routing.pathlist;

import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.core.annotations.HasComponent;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.mappers.EdgeMapper;
import de.tum.ei.lkn.eces.graph.mappers.GraphMapper;
import de.tum.ei.lkn.eces.routing.mappers.PathListMapper;
import de.tum.ei.lkn.eces.routing.mappers.PathMapper;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.DisjointPaths;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.ResilientPath;

import java.util.*;

/**
 * System listing at an Edge's and a Graph's Entity, all the Paths going
 * through this Edge and Graph.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class PathListSystem extends RootSystem {
    // Mappers
	private PathListMapper pathListMapper = new PathListMapper(controller);
	private PathMapper pathMapper = new PathMapper(controller);
	private EdgeMapper edgeMapper = new EdgeMapper(controller);
	private GraphMapper graphMapper = new GraphMapper(controller);

	/**
	 * List of the edges used by the last embedding.
	 */
	private Map<Graph, Set<Edge>> lastEmbeddedEdges = new HashMap<>();

	/**
	 * Creates a new PathListSystem.
	 * @param controller Controller responsible for the PathListSystem.
	 */
	public PathListSystem(Controller controller){
		super(controller);
	}

	public Set<Edge> getLastEmbeddedEdges() {
		Set<Edge> result = new HashSet<>();
		for(Set<Edge> edges : lastEmbeddedEdges.values()) {
			result.addAll(edges);
		}
		return result;
	}

	/**
	 * Attaches an empty PathList on a newly created Edge.
	 * @param edge The newly created Edge.
	 */
	@ComponentStateIs(State = ComponentStatus.New)
	public void addListToEdge(Edge edge) {
		pathListMapper.attachComponent(edge, new PathList());
	}

    /**
     * Attaches an empty PathList on a newly created Graph.
     * @param graph The newly created Graph.
     */
    @ComponentStateIs(State = ComponentStatus.New)
    public void addListToGraph(Graph graph) {
        pathListMapper.attachComponent(graph, new PathList());
    }

	/**
	 * When an Edge is removed, deletes all the Paths going through this
	 * Edge. This includes removing these Paths from the other Edges they
	 * traverse.
	 * @param edge Edge that is removed.
	 */
	@ComponentStateIs(State = ComponentStatus.Destroyed)
	public void removeListFromEdge(Edge edge) {
		PathList pathList = pathListMapper.get(edge.getEntity());
		for(Entity myPath : pathList.getPathList())
			pathMapper.detachComponent(myPath); // Will also trigger "removePathFromList".
	}

	/**
	 * When a new Path is created, adds the Path to the PathList of all the
	 * Edges of this Path and on the corresponding Graph.
	 * @param path The newly created Path.
	 */
	@ComponentStateIs(State = ComponentStatus.New)
	@HasComponent(component = Request.class)
	public void addPathToList(Path path) {
	    // Add to edges
		for(Edge edge : path.getPath()) {
			PathList pathList = pathListMapper.get(edge.getEntity());
			pathListMapper.updateComponent(pathList, ()->pathList.addPath(path.getEntity()));
		}

		// Add to graph
        PathList pathListOnGraph = pathListMapper.get(path.getPath()[0].getSource().getGraph().getEntity());
        pathListMapper.updateComponent(pathListOnGraph, ()->pathListOnGraph.addPath(path.getEntity()));

		// Update last embedded path.
		Graph graph = path.getPath()[0].getSource().getGraph();
		if(!lastEmbeddedEdges.containsKey(graph))
			lastEmbeddedEdges.put(graph, new HashSet<>());
		lastEmbeddedEdges.get(graph).clear();
		Collections.addAll(lastEmbeddedEdges.get(graph), path.getPath());
	}

	/**
	 * When a Path is removed, removes this Path from the PathList of all the
	 * Edges of this Path (and from the Graph)
	 * @param path The removed Path.
	 */
	@ComponentStateIs(State = ComponentStatus.Destroyed)
	public void removePathFromList(Path path) {
		Path newPath = pathMapper.getOptimistic(path.getEntity());
		if(newPath == null) {
			for(Edge edge : path.getPath()) {
				PathList pathList = pathListMapper.get(edge.getEntity());
				pathListMapper.updateComponent(pathList, ()->pathList.removePath(path.getEntity()));

				// Remove the PathList object if the Edge has been removed.
				if(!edgeMapper.isIn(pathList.getEntity()))
					pathListMapper.detachComponent(pathList);
            }

            // Also from graph!
            PathList graphPathList = pathListMapper.get(path.getPath()[0].getSource().getGraph().getEntity());
			pathListMapper.updateComponent(graphPathList, ()->graphPathList.removePath(path.getEntity()));
			if(!graphMapper.isIn(graphPathList.getEntity()))
			    pathListMapper.detachComponent(graphPathList);
		} else {
		    // If there's already another path, let's first check for some existing edges to keep
			for(Edge edge : path.getPath()) {
				boolean keepThisEdge = false;
				for(Edge newEdge: newPath){
					if(newEdge.equals(edge)){
						keepThisEdge = true;
						break;
					}
				}

				if(keepThisEdge)
					continue;

				PathList pathList = pathListMapper.get(edge.getEntity());
				pathListMapper.updateComponent(pathList, ()->pathList.removePath(path.getEntity()));

				// Remove the PathList object if the Edge has been removed.
				if(!edgeMapper.isIn(pathList.getEntity()))
					pathListMapper.detachComponent(pathList);
			}

            // Also from graph!
            PathList graphPathList = pathListMapper.get(path.getPath()[0].getSource().getGraph().getEntity());
            pathListMapper.updateComponent(graphPathList, ()->graphPathList.removePath(path.getEntity()));
            if(!graphMapper.isIn(graphPathList.getEntity()))
                pathListMapper.detachComponent(graphPathList);
		}
	}

	@ComponentStateIs(State = ComponentStatus.New)
	@HasComponent(component = Request.class)
	public void addDisjointPaths(DisjointPaths paths) {
		for(Path path : paths.getPaths()) {
			for(Edge edge : path) {
				PathList pathList = pathListMapper.get(edge.getEntity());
				pathListMapper.updateComponent(pathList, ()->pathList.addPath(paths.getEntity()));
			}
		}

        // Add to graph
        PathList pathListOnGraph = pathListMapper.get(paths.getPaths().iterator().next().getPath()[0].getSource().getGraph().getEntity());
        pathListMapper.updateComponent(pathListOnGraph, ()->pathListOnGraph.addPath(paths.getEntity()));

		// Update last embedded path to include all the disjoint paths.
		Graph graph = paths.getPaths().iterator().next().getPath()[0].getSource().getGraph();
		if(!lastEmbeddedEdges.containsKey(graph))
			lastEmbeddedEdges.put(graph, new HashSet<>());
		lastEmbeddedEdges.get(graph).clear();
		for(Path path : paths.getPaths())
			Collections.addAll(lastEmbeddedEdges.get(graph), path.getPath());
	}

	@ComponentStateIs(State = ComponentStatus.Destroyed)
	public void removeDisjointPathsFromList(DisjointPaths paths) {
		for(Path path : paths.getPaths()) {
			for(Edge edge : path.getPath()) {
				PathList pathList = pathListMapper.get(edge.getEntity());
				pathListMapper.updateComponent(pathList, () -> pathList.removePath(paths.getEntity()));

				// Remove the PathList object if the Edge has been removed.
				if (!edgeMapper.isIn(pathList.getEntity()))
					pathListMapper.detachComponent(pathList);
			}
		}

        // Also from graph!
        PathList pathListOnGraph = pathListMapper.get(paths.getPaths().iterator().next().getPath()[0].getSource().getGraph().getEntity());
        pathListMapper.updateComponent(pathListOnGraph, ()->pathListOnGraph.removePath(paths.getEntity()));
        if(!graphMapper.isIn(pathListOnGraph.getEntity()))
            pathListMapper.detachComponent(pathListOnGraph);
	}

	@ComponentStateIs(State = ComponentStatus.New)
	@HasComponent(component = Request.class)
	public void addResilientPaths(ResilientPath paths) {
		for(Edge edge : paths.getPath1()) {
			PathList pathList = pathListMapper.get(edge.getEntity());
			pathListMapper.updateComponent(pathList, ()->pathList.addPath(paths.getEntity()));
		}

		for(Edge edge : paths.getPath2()) {
			PathList pathList = pathListMapper.get(edge.getEntity());
			pathListMapper.updateComponent(pathList, ()->pathList.addPath(paths.getEntity()));
		}

        // Add to graph
        PathList pathListOnGraph = pathListMapper.get(paths.getPath1().getPath()[0].getSource().getGraph().getEntity());
        pathListMapper.updateComponent(pathListOnGraph, ()->pathListOnGraph.addPath(paths.getEntity()));

		// Update last embedded path to include both paths
		Graph graph = paths.getPath1().getPath()[0].getSource().getGraph();
		if(!lastEmbeddedEdges.containsKey(graph))
			lastEmbeddedEdges.put(graph, new HashSet<>());
		lastEmbeddedEdges.get(graph).clear();
		Collections.addAll(lastEmbeddedEdges.get(graph), paths.getPath1().getPath());
		Collections.addAll(lastEmbeddedEdges.get(graph), paths.getPath2().getPath());
	}

	@ComponentStateIs(State = ComponentStatus.Destroyed)
	public void removeResilientPathsFromList(ResilientPath paths) {
		for(Edge edge : paths.getPath1()) {
			PathList pathList = pathListMapper.get(edge.getEntity());
			pathListMapper.updateComponent(pathList, ()->pathList.removePath(paths.getEntity()));

			// Remove the PathList object if the Edge has been removed.
			if(!edgeMapper.isIn(pathList.getEntity()))
				pathListMapper.detachComponent(pathList);
		}

		for(Edge edge : paths.getPath2()) {
			PathList pathList = pathListMapper.get(edge.getEntity());
			pathListMapper.updateComponent(pathList, ()->pathList.removePath(paths.getEntity()));

			// Remove the PathList object if the Edge has been removed.
			if(!edgeMapper.isIn(pathList.getEntity()))
				pathListMapper.detachComponent(pathList);
		}

        // Also from graph!
        PathList pathListOnGraph = pathListMapper.get(paths.getPath1().getPath()[0].getSource().getGraph().getEntity());
        pathListMapper.updateComponent(pathListOnGraph, ()->pathListOnGraph.removePath(paths.getEntity()));
        if(!graphMapper.isIn(pathListOnGraph.getEntity()))
            pathListMapper.detachComponent(pathListOnGraph);
	}
}
