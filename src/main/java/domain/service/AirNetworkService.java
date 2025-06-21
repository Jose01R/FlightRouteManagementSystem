package domain.service;

import data.RouteData;
import domain.common.Airport;
import domain.common.Route;
import domain.graph.DijkstraNode;
import domain.graph.DirectedSinglyLinkedListGraph;
import domain.graph.GraphException;
import domain.graph.Vertex;
import domain.graph.EdgeWeight;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.PriorityLinkedQueue;
import domain.linkedqueue.QueueException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.Utility;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;

public class AirNetworkService {
    private DirectedSinglyLinkedListGraph airportGraph;
    private Map<String, Route> routesMap;
    private AirportService airportService;
    private RouteData routeData;
    private ObservableList<Route> observableRoutes;
    public AirNetworkService(AirportService airportService, RouteData routeData) {
        this.airportService = Objects.requireNonNull(airportService, "AirportService cannot be null");
        this.routeData = Objects.requireNonNull(routeData, "RouteData cannot be null");

        this.airportGraph = new DirectedSinglyLinkedListGraph();
        this.routesMap = new HashMap<>();
        this.observableRoutes = FXCollections.observableArrayList();
        try {
            generateInitialRandomRoutes(10);
        } catch (ListException e) {
            throw new RuntimeException(e);
        } catch (GraphException e) {
            throw new RuntimeException(e);
        }
        loadRoutesAndNetwork();
    }

    public ObservableList<Route> getObservableRoutes() {
        return observableRoutes;
    }

    private void loadRoutesAndNetwork() {
        try {
            airportGraph.clear();
            routesMap.clear();
            observableRoutes.clear();
            //Cargamos todos los aeropuertos y agregarmos al grafo como vértices
            List<Object> allAirportsObjects = airportService.getAllAirportsAsList();
            List<Airport> allAirports = new ArrayList<>();
            for (Object obj : allAirportsObjects) {
                if (obj instanceof Airport) {
                    allAirports.add((Airport) obj);
                } else {
                    System.err.println("Warning: Object in AirportService.getAllAirports is not an Airport. Type: " + (obj != null ? obj.getClass().getName() : "null"));
                }
            }

            System.out.println("Loading " + allAirports.size() + " airports into graph...");
            for (Airport airport : allAirports) {
                try {
                    airportGraph.addVertex(airport);


                } catch (GraphException e) {
                    System.err.println("Warning: Could not add airport to graph: " + airport.getCode() + " - " + e.getMessage());
                }
            }

            this.routesMap = routeData.loadRoutesToMap();
            System.out.println("Loading " + routesMap.size() + " routes into graph...");

            for (Route route : routesMap.values()) {
                Airport origin = airportService.getAirportByCode(route.getOriginAirportCode());
                Airport destination = airportService.getAirportByCode(route.getDestinationAirportCode());
                observableRoutes.setAll(routesMap.values());
                if (origin != null && destination != null) {
                    try {
                        airportGraph.addVertex(origin);
                        airportGraph.addVertex(destination);

                        airportGraph.addEdgeWeight(origin, destination, route);
                    } catch (GraphException e) {
                        System.err.println("Warning: Could not add edge for route " + route.getRouteId() + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Warning: Route " + route.getRouteId() + " has airports not found in AirportService. Not added to graph");
                }
            }
            System.out.println("Airport network and routes loaded successfully in AirNetworkService");
        } catch (IOException e) {
            System.err.println("Error reading route data file: " + e.getMessage());
            e.printStackTrace();
        } catch (ListException e) {
            System.err.println("Error loading airport network and routes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveRoutes() {
        try {
            routeData.saveRoutesFromMap(this.routesMap);
            System.out.println("Routes saved from AirNetworkService: " + routesMap.size());
        } catch (IOException e) {
            System.err.println("Error saving routes to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean addRoute(Route route) throws ListException, GraphException {
        Objects.requireNonNull(route, "Route details cannot be null");

        if (routesMap.containsKey(route.getRouteId())) {
            throw new ListException("La ruta con ID " + route.getRouteId() + " ya existe");
        }

        Airport originAirport = airportService.getAirportByCode(route.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(route.getDestinationAirportCode());

        if (originAirport == null) {
            throw new ListException("Aeropuerto de origen con código " + route.getOriginAirportCode() + " no encontrado en el AirportService");
        }
        if (destinationAirport == null) {
            throw new ListException("Aeropuerto de destino con código " + route.getDestinationAirportCode() + " no encontrado en el AirportService");
        }

        airportGraph.addVertex(originAirport);
        airportGraph.addVertex(destinationAirport);

        routesMap.put(route.getRouteId(), route);
        observableRoutes.add(route);

        airportGraph.addEdgeWeight(originAirport, destinationAirport, route);

        try {
            saveRoutes();
        } catch (Exception e) {
            //Revertir si falla el guardado
            routesMap.remove(route.getRouteId());
            try {
                airportGraph.removeEdge(originAirport, destinationAirport);
            } catch (GraphException rollbackEx) {
                System.err.println("CRITICAL: Failed to rollback addEdge after save error: " + rollbackEx.getMessage());
            }
            throw new GraphException("Failed to save route after addition: " + e.getMessage());
        }
        System.out.println("Ruta " + route.getRouteId() + " añadida a la red y persistida");
        return true;
    }

    public boolean deleteRoute(String routeId) throws ListException, GraphException {
        Route routeToDelete = routesMap.get(routeId);

        if (routeToDelete == null) {
            throw new ListException("Ruta con ID " + routeId + " no encontrada para eliminar");
        }

        Airport originAirport = airportService.getAirportByCode(routeToDelete.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(routeToDelete.getDestinationAirportCode());

        routesMap.remove(routeId);
        observableRoutes.remove(routeToDelete);

        if (originAirport != null && destinationAirport != null) {
            airportGraph.removeEdge(originAirport, destinationAirport);
        } else {
            System.err.println("Warning: Airports for deleted route " + routeId + " not found in AirportService. Could not precisely remove graph edge");
        }

        try {
            saveRoutes();
        } catch (Exception e) {
            //Revertir si falla el guardado
            routesMap.put(routeId, routeToDelete);
            if (originAirport != null && destinationAirport != null) {
                try {
                    //Re agregamos el edge, pasando la ruta directamente
                    airportGraph.addEdgeWeight(originAirport, destinationAirport, routeToDelete);
                } catch (GraphException rollbackEx) {
                    System.err.println("CRITICAL: Failed to rollback removeEdge after save error: " + rollbackEx.getMessage());
                }
            }
            throw new GraphException("Failed to save route changes after deletion: " + e.getMessage());
        }
        System.out.println("Ruta " + routeId + " eliminada de la red y persistida");
        return true;
    }

    public boolean updateRoute(Route updatedRoute) throws ListException, GraphException {
        Objects.requireNonNull(updatedRoute, "Updated route details cannot be null");

        if (!routesMap.containsKey(updatedRoute.getRouteId())) {
            throw new ListException("Ruta con ID " + updatedRoute.getRouteId() + " no encontrada para actualizar");
        }

        Route oldRoute = routesMap.get(updatedRoute.getRouteId());

        Airport originAirport = airportService.getAirportByCode(updatedRoute.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(updatedRoute.getDestinationAirportCode());

        if (originAirport == null) {
            throw new ListException("Aeropuerto de origen con código " + updatedRoute.getOriginAirportCode() + " no encontrado para actualizar la ruta");
        }
        if (destinationAirport == null) {
            throw new ListException("Aeropuerto de destino con código " + updatedRoute.getDestinationAirportCode() + " no encontrado para actualizar la ruta");
        }

        //Primero removemos el edge viejo
        airportGraph.removeEdge(airportService.getAirportByCode(oldRoute.getOriginAirportCode()),
                airportService.getAirportByCode(oldRoute.getDestinationAirportCode()));

        //Actualizar la ruta en el mapa
        routesMap.put(updatedRoute.getRouteId(), updatedRoute);
        int index = observableRoutes.indexOf(oldRoute);
        if (index >= 0) {
            observableRoutes.set(index, updatedRoute);
        }

        //Añade el nuevo edge actualizado
        airportGraph.addEdgeWeight(originAirport, destinationAirport, updatedRoute);

        try {
            saveRoutes();
        } catch (Exception e) {
            // Rollback changes if save fails
            routesMap.put(updatedRoute.getRouteId(), oldRoute);
            try {
                //Removemos el edge nuevo y agregamos el antiguo
                airportGraph.removeEdge(originAirport, destinationAirport);
                //Volvemos a agregar el edge antiguo, pasando la ruta anterior
                airportGraph.addEdgeWeight(airportService.getAirportByCode(oldRoute.getOriginAirportCode()),
                        airportService.getAirportByCode(oldRoute.getDestinationAirportCode()),
                        oldRoute);
            } catch (GraphException rollbackEx) {
                System.err.println("CRITICAL: Failed to rollback updateEdge after save error: " + rollbackEx.getMessage());
            }
            throw new GraphException("Failed to save route changes after update: " + e.getMessage());
        }
        System.out.println("Ruta " + updatedRoute.getRouteId() + " actualizada y persistida");
        return true;
    }

    public void generateInitialRandomRoutes(int count) throws ListException, GraphException {
        List<Object> allAirportsObjects = airportService.getAllAirportsAsList();
        List<Airport> allAirports = new ArrayList<>();
        for (Object obj : allAirportsObjects) {
            if (obj instanceof Airport) {
                allAirports.add((Airport) obj);
            } else {
                System.err.println("Warning: Object in AirportService.getAllAirports is not an Airport during random route generation. Type: " + (obj != null ? obj.getClass().getName() : "null"));
            }
        }

        if (allAirports.size() < 2) {
            throw new ListException("Necesitas al menos dos aeropuertos para generar rutas aleatorias");
        }

        int generatedCount = 0;
        while (generatedCount < count) {
            Airport origin = allAirports.get(Utility.random(allAirports.size()));
            Airport destination;
            do {
                destination = allAirports.get(Utility.random(allAirports.size()));
            } while (origin.equals(destination));

            String routeId = "R" + Utility.random(1000000);
            String airline = "Airline" + (char)('A' + Utility.random(26)) + (char)('A' + Utility.random(26));

            double distance = 500 + Utility.random(5000);
            double duration = Utility.randomMinMax(1, 10);
            double price = Utility.randomMinMax(50, 1000);

            LocalTime departureTime = LocalTime.of(Utility.random(24), Utility.random(60));
            LocalTime arrivalTime = departureTime.plusHours((long) duration).plusMinutes((long) ((duration - (long)duration) * 60));

            Route newRoute = new Route(routeId, origin.getCode(), destination.getCode(),
                    airline, duration, distance, price,
                    departureTime, arrivalTime);

            try {
                addRoute(newRoute);
                observableRoutes.add(newRoute);

                generatedCount++;
            } catch (ListException | GraphException e) {
                System.err.println("Warning: Error generating or adding random route " + routeId + ": " + e.getMessage());
            }
        }
        System.out.println(generatedCount + " rutas aleatorias generadas y guardadas.");
    }

    public boolean containsRoute(int originAirportCode, int destinationAirportCode) throws ListException {
        Airport originInGraph = airportService.getAirportByCode(originAirportCode);
        Airport destinationInGraph = airportService.getAirportByCode(destinationAirportCode);

        if (originInGraph == null || destinationInGraph == null) {
            return false;
        }

        try {
            return airportGraph.containsEdge(originInGraph, destinationInGraph);
        } catch (GraphException | ListException e) {
            System.err.println("Error checking route: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> findShortestRoute(int originAirportCode, int destinationAirportCode, String costType)
            throws GraphException, ListException {

        if (airportGraph.isEmpty()) {
            throw new GraphException("Airport network is empty. Please load airports and routes first.");
        }

        Airport origin = airportService.getAirportByCode(originAirportCode);
        Airport destination = airportService.getAirportByCode(destinationAirportCode);

        if (origin == null) {
            throw new GraphException("Origin airport with code " + originAirportCode + " not found in AirportService.");
        }
        if (destination == null) {
            throw new GraphException("Destination airport with code " + destinationAirportCode + " not found in AirportService.");
        }

        if (!airportGraph.containsVertex(origin)) {
            throw new GraphException("Origin airport with code " + originAirportCode + " not found in the in-memory graph.");
        }
        if (!airportGraph.containsVertex(destination)) {
            throw new GraphException("Destination airport with code " + destinationAirportCode + " not found in the in-memory graph.");
        }

        if (!costType.equalsIgnoreCase("duration") && !costType.equalsIgnoreCase("distance") && !costType.equalsIgnoreCase("price")) {
            throw new IllegalArgumentException("Invalid cost type. Must be 'duration', 'distance', or 'price'.");
        }

        Map<Airport, Double> minCosts = new HashMap<>();
        Map<Airport, Airport> predecessors = new HashMap<>();

        PriorityLinkedQueue pq = new PriorityLinkedQueue();

        try {
            minCosts.put(origin, 0.0);
            pq.enQueue(new DijkstraNode(origin, 0.0, new ArrayList<>(List.of(origin))), (int) 0.0);

            List<Airport> shortestPathAirports = null;

            while (!pq.isEmpty()) {
                DijkstraNode current = (DijkstraNode) pq.deQueue();
                Airport currentAirport = current.getAirport();

                //Si ya encontramos una ruta más corta al aeropuerto actual, saltamos
                if (current.getCost() > minCosts.getOrDefault(currentAirport, Double.MAX_VALUE) + 1e-9) {
                    continue;
                }

                if (Objects.equals(currentAirport, destination)) {
                    shortestPathAirports = current.getPath();
                    break;
                }

                Vertex currentGraphVertex = null;
                SinglyLinkedList vertexListSLL = airportGraph.getVertexList();
                if (vertexListSLL != null && !vertexListSLL.isEmpty()) {
                    for (int i = 1; i <= vertexListSLL.size(); i++) {
                        Node nodeFromList = vertexListSLL.getNode(i);
                        if (nodeFromList != null && nodeFromList.data instanceof Vertex) {
                            Vertex potentialVertex = (Vertex) nodeFromList.data;
                            if (Objects.equals(potentialVertex.data, currentAirport)) {
                                currentGraphVertex = potentialVertex;
                                break;
                            }
                        }
                    }
                }

                if (currentGraphVertex == null || currentGraphVertex.edgesList == null || currentGraphVertex.edgesList.isEmpty()) {
                    continue;
                }

                //Iterar sobre los edges salientes (vecinas)
                for (int i = 1; i <= currentGraphVertex.edgesList.size(); i++) {
                    Node edgeNode = currentGraphVertex.edgesList.getNode(i);
                    if (edgeNode == null || !(edgeNode.data instanceof EdgeWeight)) {
                        System.err.println("Warning: Non-EdgeWeight object found in edgesList for vertex " + currentAirport.getCode() + ". Data type: " + (edgeNode != null ? edgeNode.data.getClass().getName() : "null"));
                        continue;
                    }
                    EdgeWeight edgeWeight = (EdgeWeight) edgeNode.data;

                    Airport neighborAirport = (Airport) edgeWeight.getEdge(); //obtiene el aeropuerto de destino

                    Route routeDetails = (Route) edgeWeight.getWeight(); //devuelve Ruta directamente

                    if (neighborAirport == null || routeDetails == null) {
                        System.err.println("Warning: Malformed EdgeWeight object found for vertex " + currentAirport.getCode() + ". Missing neighbor airport or route details.");
                        continue;
                    }

                    double edgeCost = 0.0;
                    switch (costType.toLowerCase()) {
                        case "duration":
                            edgeCost = routeDetails.getDurationHours();
                            break;
                        case "distance":
                            edgeCost = routeDetails.getDistanceKm();
                            break;
                        case "price":
                            edgeCost = routeDetails.getPrice();
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid cost type in Dijkstra calculation.");
                    }

                    double newCost = current.getCost() + edgeCost;

                    if (newCost < minCosts.getOrDefault(neighborAirport, Double.MAX_VALUE)) {
                        minCosts.put(neighborAirport, newCost);
                        predecessors.put(neighborAirport, currentAirport);

                        List<Airport> newPath = new ArrayList<>(current.getPath());
                        newPath.add(neighborAirport);

                        pq.enQueue(new DijkstraNode(neighborAirport, newCost, newPath), (int) newCost);
                    }
                }
            }

            if (shortestPathAirports != null) {
                List<Integer> resultCodes = new ArrayList<>();
                for (Airport airport : shortestPathAirports) {
                    resultCodes.add(airport.getCode());
                }
                return resultCodes;
            } else {
                return null;
            }
        } catch (QueueException e) {
            throw new GraphException("Error processing shortest path with Priority Queue: " + e.getMessage());
        }
    }

    public void removeAirportFromGraph(int airportCode) throws GraphException, ListException {
        Airport airportToRemove = airportService.getAirportByCode(airportCode);

        if (airportToRemove == null) {
            throw new GraphException("Airport with code " + airportCode + " not found in AirportService, cannot remove from graph.");
        }
        if (!airportGraph.containsVertex(airportToRemove)) {
            System.out.println("Warning: Airport " + airportCode + " not found in the graph. No removal required from graph.");
            return;
        }

        System.out.println("Removing airport " + airportCode + " and its associated routes from the graph...");

        List<String> routeIdsToRemove = new ArrayList<>();
        for (Route route : new ArrayList<>(routesMap.values())) {
            if (route.getOriginAirportCode() == airportCode || route.getDestinationAirportCode() == airportCode) {
                routeIdsToRemove.add(route.getRouteId());
            }
        }

        for (String routeId : routeIdsToRemove) {
            Route routeRemovedFromMap = routesMap.get(routeId);
            if (routeRemovedFromMap != null) {
                routesMap.remove(routeId);

                Airport origin = airportService.getAirportByCode(routeRemovedFromMap.getOriginAirportCode());
                Airport destination = airportService.getAirportByCode(routeRemovedFromMap.getDestinationAirportCode());

                if (origin != null && destination != null) {
                    try {
                        airportGraph.removeEdge(origin, destination);
                    } catch (GraphException e) {
                        System.err.println("Error removing edge for route " + routeId + ": " + e.getMessage());
                    }
                }
            }
        }

        airportGraph.removeVertex(airportToRemove);
        System.out.println("Airport " + airportCode + " and its edges removed from the graph.");

        saveRoutes();
    }

    public Route getRouteById(String routeId) {
        return routesMap.get(routeId);
    }

    public List<Route> getAllRoutes() {
        return new ArrayList<>(routesMap.values());
    }

    @Override
    public String toString() {
        return airportGraph.toString();
    }
}