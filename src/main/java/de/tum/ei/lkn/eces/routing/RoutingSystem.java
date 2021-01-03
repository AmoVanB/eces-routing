package de.tum.ei.lkn.eces.routing;

import de.tum.ei.lkn.eces.core.ComponentStatus;
import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.core.annotations.ComponentStateIs;
import de.tum.ei.lkn.eces.core.annotations.HasComponent;
import de.tum.ei.lkn.eces.core.annotations.HasNotComponent;
import de.tum.ei.lkn.eces.graph.mappers.GraphMapper;
import de.tum.ei.lkn.eces.graph.mappers.NodeMapper;
import de.tum.ei.lkn.eces.routing.algorithms.RoutingAlgorithm;
import de.tum.ei.lkn.eces.routing.mappers.*;
import de.tum.ei.lkn.eces.routing.requests.Request;
import de.tum.ei.lkn.eces.routing.responses.ErrorResponse;
import de.tum.ei.lkn.eces.routing.responses.IndependentSetOfPaths;
import de.tum.ei.lkn.eces.routing.responses.Path;
import de.tum.ei.lkn.eces.routing.responses.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * System automatically routing a Request when it is created.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 * @author Amir Varasteh
 */
public class RoutingSystem extends RootSystem {
    // List of handled requests
    protected Map<Long, Request> handledRequests = new HashMap<>();

    private ResponseMapper responseMapper = new ResponseMapper(controller);
    private GraphMapper graphMapper = new GraphMapper(controller);
    private UnicastRequestMapper requestMapper = new UnicastRequestMapper(controller);
    private PathListMapper pathListMapper = new PathListMapper(controller);
    private PathMapper pathMapper = new PathMapper(controller);
    private NodeMapper nodeMapper = new NodeMapper(controller);
    private SelectedRoutingAlgorithmMapper selectedRoutingAlgorithmMapper = new SelectedRoutingAlgorithmMapper(controller);
    private RequestNameMapper requestNameMapper = new RequestNameMapper(controller);
    private DeleteRequestMapper deleteRequestMapper = new DeleteRequestMapper(controller);

    /**
     * Creates a new RoutingSystem.
     * @param controller Controller responsible for the new System.
     */
    public RoutingSystem(Controller controller) {
        super(controller);
    }

    /**
     * When a new Request is created, tries to find a path for it.
     * The algorithm used is the one attached as SelectedRoutingAlgorithm.
     * If routing is successful, the Path is registered, otherwise an ErrorResponse is attached.
     * @param newFlowRequest The Request.
     */
    @ComponentStateIs(State = ComponentStatus.New)
    @HasNotComponent(component = DoNotRoute.class)
    public void findPath(Request newFlowRequest) {
        this.logger.info("Handling request " + newFlowRequest + ".");
        this.handledRequests.put(newFlowRequest.getEntity().getId(), newFlowRequest);
        // Get a read lock on the Graph.
        graphMapper.isIn(newFlowRequest.getGraph().getEntity());
        RoutingAlgorithm algorithm = this.findRoutingAlgorithm(newFlowRequest);
        this.logger.info(algorithm + " has been chosen to route " + newFlowRequest + ".");
        Response newFlowResponse = algorithm.solve(newFlowRequest);

        if (newFlowResponse != null && algorithm.getProxy().isValid(newFlowResponse, newFlowRequest)) {
            if(newFlowResponse instanceof IndependentSetOfPaths) {
                for(Path path : ((IndependentSetOfPaths) newFlowResponse).getPaths())
                    // Attach all paths to an empty entity
                    responseMapper.attachComponent(controller.createEntity(), path);
            }

            algorithm.getProxy().register(newFlowResponse, newFlowRequest);
            newFlowResponse.setProxy(algorithm.getProxy());
            // Attach response to request
            responseMapper.attachComponent(newFlowRequest, newFlowResponse);
            this.logger.info("Response " + newFlowResponse + " has been found (and registered) by " + algorithm + " for " + newFlowRequest + ".");
            return;
        }

        // No response found
        if(newFlowResponse != null && !algorithm.getProxy().isValid(newFlowResponse, newFlowRequest))
            this.logger.warn(algorithm + " found a path (" + newFlowResponse + ") for " + newFlowRequest + " but it is not valid.");
        else if (newFlowResponse == null)
            this.logger.warn(algorithm + " was not able to find a path for " + newFlowRequest + ".");

        responseMapper.attachComponent(newFlowRequest, new ErrorResponse(algorithm.getProxy()));
    }

    /**
     * When a DeleteRequest is added, delete the corresponding request.
     * @param deleteRequest The request to remove.
     */
    @ComponentStateIs(State = ComponentStatus.New)
    public void deleteRequest(DeleteRequest deleteRequest) {
        Request request = this.handledRequests.get(deleteRequest.getEntityToDelete());
        if(request != null) {
            requestMapper.detachComponent(request.getEntity());
            this.logger.info("Deleting request " + request + " upon direct request.");
        }

        deleteRequestMapper.detachComponent(deleteRequest);
    }

    /**
     * When a Request is removed, deregisters the Path associated to this
     * Request.
     * @param request The removed Request.
     */
    @ComponentStateIs(State = ComponentStatus.Destroyed)
    @HasComponent(component = Response.class)
    public void deletePath(Request request) {
        Response response = responseMapper.get(request.getEntity());
        response.getProxy().deregister(response, request);
        responseMapper.detachComponent(response);
        this.logger.info("Path " + response + " for " + request + " has been unregistered.");
        this.handledRequests.remove(request.getId());

    }

    @ComponentStateIs(State = ComponentStatus.Destroyed)
    @HasComponent(component = Request.class)
    public void deletePath(Response response) {
        this.handledRequests.remove(requestMapper.getOptimistic(response.getEntity()).getId());
        // Restoration can be implemented here
    }

    /**
     * Returns the attached algorithm planend for solving the request.
     * @param request Request to handle.
     * @return Algorithm responsible for handling the Request.
     */
    private RoutingAlgorithm findRoutingAlgorithm(Request request) {
        if(selectedRoutingAlgorithmMapper.isIn(request.getEntity()))
            return selectedRoutingAlgorithmMapper.get(request.getEntity()).getRoutingAlgorithm();
        else
            return null;
    }
}
