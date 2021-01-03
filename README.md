# Routing

This project implements 30+ routing algorithms on top of the [graph](https://github.com/AmoVanB/eces-graph) library of the [ECES](https://github.com/AmoVanB/eces-core) framework.

## Usage

The project can be downloaded from maven central using:
```xml
<dependency>
  <groupId>de.tum.ei.lkn.eces</groupId>
  <artifactId>routing</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

The project implements a routing system that defines three components: requests (`Request.java`), responses (`Response.java`), and a selected routing algorithm object (`SelectedRoutingAlgorithm.java`).

When a new request component is created and attached to an entity where a selected routing algorithm object is also attached, the routing system automatically uses the algorithm referenced by the latter object to solve the request and then attaches the response to the same entity.

The snippet below shows how this can be done.

```java
Entity entity = controller.createEntity();
try (MapperSpace mapperSpace = controller.startMapperSpace()) {
    requestMapper.attachComponent(entity, new UnicastRequest(sourceNode, destinationNode));
    selectedRoutingAlgorithmMapper.attachComponent(entity, new SelectedRoutingAlgorithm(routingAlgorithmToUse));
}
```

At the closure of the mapper space, the routing system will have automatically attached a response to `entity`.

See [tests](src/test) for other simple examples.

See other ECES repositories using this routing library (e.g., the [tenant manager](https://github.com/AmoVanB/eces-tenant-manager)) for more detailed/advanced examples.
 
#### Types of Requests and Responses

We distinguish different type of requests (unicast requests, unicast requests with intermediate nodes, resilient request, etc.) and hence define different types of responses accordingly (path, set of paths, etc.). An "error" response is also included if the routing algorithm could not find a solution.

#### Do Not Route!

A do not route component (`DoNoRoute.java`) allows to prevent the routing system to solve a request.
If such a component is attached to the entity of a request, the routing system will not solve the request.

#### Delete a Request!

We introduce a delete request component (`DeleteRequest.java`).
If such a component is attached to an entity (any), the routing system automatically detaches the request attached to the entity specified by the delete request component.

### Structure of Routing Algorithms

As mentioned, routing algorithms take requests as input and return responses.
The request (`Request.java` or its subclasses) contains information on the source/destination/intermediate nodes/graph of the request.
The communication between a graph and an algorithm always happens through a proxy (`Proxy.java`).
Hence, before being used, an algorithm must always be "equipped" with a proxy.

### Proxies

 A Proxy is responsible for interfacing a routing algorithm with a graph model. The proxy is mostly able to:
  * compute the cost and constraints (i.e., metrics) values of an edge,
  * register and deregister a new flow in the graph (if resource reservation is to be implemented),
  * determine resources availability at an edge (if access control is to be implemented).

This information is used by the routing algorithm to find a solution to a given request. For example, Dijkstra (`DijkstraAlgorithm.java`) uses its proxy to get the cost of the edges it visits. 
Registration and deregistration is done through the proxy by the routing system after getting the response from the routing algorithm. 

 Three Proxy types are defined depending on what input the Proxy needs to compute cost, constraints and resources availability at an edge: edge proxies (`EdgeProxy.java`), previous edge proxies (`PreviousEdgeProxy.java`) and path proxies (`PathProxy.java`). An edge proxy requires only the current edge, a previous edge proxy requires the edge visited before the current edge and a path proxy requires the complete path traversed before reaching the current edge. (See *[Routing metrics depending on previous edges: The Mn taxonomy and its corresponding solutions" A Van Bemten,  JW Guck, CM Machuca, W Kellerer. 2018](https://arxiv.org/pdf/1805.11586;Routing).* for more information about this distinction.)

 Proxies are a very powerful tool to adapt the behavior of an algorithm without altering the graph structure.
 Just changing the proxy allows to completely change the metrics of all the edges.
 We for example define a `ShortestPathProxy.java` which simply returns 1 as cost for any edge.
 Another example are plumber proxies: these are proxies which wrap around a proxy and mix and match its cost and constraint values.
 For example, a plumber proxy can be used to transform the cost returned by a proxy into a constraint and a linear combination of the constraints returned by a proxy to a new cost function.  

 Besides just returning static values, a proxy can use any information to compute the metrics associated to an edge.
 For example, a proxy can check the list of paths already flowing through an edge or the rate of an edge and compute its metric values accordingly.

### Path List System

By listening to path attachment events, the path list system keeps track of all the paths that have been found per graph. 
It attaches a path list (`PathList.java`) to the entity of each graph.
 
## Algorithms and References

 Most algorithms algorithms implemented here were covered in the survey

 [Jochen W. Guck, Amaury Van Bemten, Martin Reisslein, and Wolfgang Kellerer. *"Unicast QoS routing algorithms for SDN: A comprehensive survey and performance evaluation."* IEEE Communications Surveys & Tutorials 20, no. 1 (2018): 388-415](https://mediatum.ub.tum.de/doc/1420144/file.pdf).

 The algorithms proposed in 

 - [Amaury Van Bemten, Jochen W. Guck, Carmen Mas Machuca, and Wolfgang Kellerer. *"Routing metrics depending on previous edges: The Mn taxonomy and its corresponding solutions."* In 2018 IEEE International Conference on Communications (ICC), pp. 1-7. IEEE, 2018](https://arxiv.org/pdf/1805.11586;Routing),
 - [Amaury Van Bemten, Jochen W. Guck, Petra Vizarreta, Carmen Mas Machuca, and Wolfgang Kellerer. *"LARAC-SN and Mole in the Hole: Enabling Routing through Service Function Chains."* In 2018 4th IEEE Conference on Network Softwarization and Workshops (NetSoft), pp. 298-302. IEEE, 2018](https://mediatum.ub.tum.de/doc/1437432/file.pdf), and
 - [Amaury Van Bemten, Jochen W. Guck, Carmen Mas Machuca, and Wolfgang Kellerer. *"Bounded Dijkstra (BD): Search Space Reduction for Expediting Shortest Path Subroutines."* arXiv preprint arXiv:1903.00436 (2019)](https://arxiv.org/pdf/1903.00436)

 are also implemented.

 These references are the best place for getting a clear understanding of the different algorithms.