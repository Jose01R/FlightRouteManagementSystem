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
import java.util.*;
import java.util.stream.Collectors;

public class AirNetworkService {
    private DirectedSinglyLinkedListGraph airportGraph; //Grafo representa red de aeropuertos y rutas
    private Map<String, Route> routesMap; //Mapa para almacenar rutas, usando el ID de ruta como key
    private AirportService airportService; //para interactuar con los datos de aeropuertos
    private RouteData routeData; //Capa de datos para persistenca de rutas
    private ObservableList<Route> observableRoutes; //Lista para la UI

    public AirNetworkService(AirportService airportService, RouteData routeData) {
        // Asegura que los servicos inyectados no sean nulos
        this.airportService = Objects.requireNonNull(airportService, "AirportService cannot be null");
        this.routeData = Objects.requireNonNull(routeData, "RouteData cannot be null");

        //Inicializamos estructuras de datos principales
        this.airportGraph = new DirectedSinglyLinkedListGraph();
        this.routesMap = new HashMap<>();
        this.observableRoutes = FXCollections.observableArrayList();
        try {
            //Genera rutas aleatorias iniciales
            generateInitialRandomRoutes(20);
        } catch (ListException | GraphException e) {
            throw new RuntimeException(e);
        }
        //Carga rutas persistentes y construye red del grafo
        loadRoutesAndNetwork();
    }

    public ObservableList<Route> getObservableRoutes() {
        //Devuelve la lista observable de rutas
        return observableRoutes;
    }

    private void loadRoutesAndNetwork() {
        try {
            //Limpia las estructuras antes de cargar nuevos datos para evitar duplicados
            airportGraph.clear();
            routesMap.clear();
            observableRoutes.clear();

            //Carga todos los aeropuertos desde AirportService y los añade como vertexes al grafo
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
                    airportGraph.addVertex(airport); //Añade cada aeropuerto como un nodo (vertex)
                } catch (GraphException e) {
                    System.err.println("Warning: Could not add airport to graph: " + airport.getCode() + " - " + e.getMessage());
                }
            }

            //Carga rutas desde RouteData
            this.routesMap = routeData.loadRoutesToMap();
            System.out.println("Loading " + routesMap.size() + " routes into graph...");

            //Recorre sobre las rutas cargadas y añade las ARISTAS al grafo
            for (Route route : routesMap.values()) {
                Airport origin = airportService.getAirportByCode(route.getOriginAirportCode());
                Airport destination = airportService.getAirportByCode(route.getDestinationAirportCode());
                observableRoutes.setAll(routesMap.values()); // Actualiza la lista observable.
                if (origin != null && destination != null) {
                    try {
                        //aeropuertos existen como vértices antes de añadir la arista
                        airportGraph.addVertex(origin);
                        airportGraph.addVertex(destination);
                        //Añade una arista dirigida entre origen y destino, usando Route como peso de la arista
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

        //Valida si ruta ya existe por ID
        if (routesMap.containsKey(route.getRouteId())) {
            throw new ListException("La ruta con ID " + route.getRouteId() + " ya existe");
        }

        //Obtiene los Airport de acuerdo a los códigos de origen- destino
        Airport originAirport = airportService.getAirportByCode(route.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(route.getDestinationAirportCode());

        //Valida que aeropuertos (origen y destino) existan
        if (originAirport == null) {
            throw new ListException("Aeropuerto de origen con código " + route.getOriginAirportCode() + " no encontrado en el AirportService");
        }
        if (destinationAirport == null) {
            throw new ListException("Aeropuerto de destino con código " + route.getDestinationAirportCode() + " no encontrado en el AirportService");
        }

        //Asegura que los aeropuertos sean vértices en el grafo
        airportGraph.addVertex(originAirport);
        airportGraph.addVertex(destinationAirport);

        //Añade la ruta al mapa y a la lista observable
        routesMap.put(route.getRouteId(), route);
        observableRoutes.add(route);

        //Añade la arista con peso al grafo, usando la ruta como peso
        airportGraph.addEdgeWeight(originAirport, destinationAirport, route);

        try {
            //guardar los cambios
            saveRoutes();
        } catch (Exception e) {
            //Si el guardado falla, se realiza un ROLLBACK:
            //Se elimina la ruta del mapa en memoria
            //Se intenta eliminar la arista del grafo para revertir cambio
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
        //Obtiene ruta a eliminar del map
        Route routeToDelete = routesMap.get(routeId);

        //Valida si ruta existe
        if (routeToDelete == null) {
            throw new ListException("Ruta con ID " + routeId + " no encontrada para eliminar");
        }

        //Obtiene los aeropuertos asociados a la ruta para la manipulación del grafo
        Airport originAirport = airportService.getAirportByCode(routeToDelete.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(routeToDelete.getDestinationAirportCode());

        //Elimina la ruta del mapa y de la lista observable
        routesMap.remove(routeId);
        observableRoutes.remove(routeToDelete);

        //Elimina la arista correspondiente del grafo
        if (originAirport != null && destinationAirport != null) {
            airportGraph.removeEdge(originAirport, destinationAirport);
        } else {
            System.err.println("Warning: Airports for deleted route " + routeId + " not found in AirportService. Could not precisely remove graph edge");
        }

        try {
            //guardar los cambios
            saveRoutes();
        } catch (Exception e) {
            //Si el guardado falla, se realiza un ROLLBACK:
            // Se reinserta la ruta en el mapa
            // Se intenta volver a añadir la arista al grafo
            routesMap.put(routeId, routeToDelete);
            if (originAirport != null && destinationAirport != null) {
                try {
                    airportGraph.addEdgeWeight(originAirport, destinationAirport, routeToDelete); // Re-agrega la arista.
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

        //Valida si la ruta a actualizar existe
        if (!routesMap.containsKey(updatedRoute.getRouteId())) {
            throw new ListException("Ruta con ID " + updatedRoute.getRouteId() + " no encontrada para actualizar");
        }

        //Guarda la ruta antigua para un posible rollback
        Route oldRoute = routesMap.get(updatedRoute.getRouteId());

        //Valida que los aeropuertos de la ruta actualizada existan
        Airport originAirport = airportService.getAirportByCode(updatedRoute.getOriginAirportCode());
        Airport destinationAirport = airportService.getAirportByCode(updatedRoute.getDestinationAirportCode());

        if (originAirport == null) {
            throw new ListException("Aeropuerto de origen con código " + updatedRoute.getOriginAirportCode() + " no encontrado para actualizar la ruta");
        }
        if (destinationAirport == null) {
            throw new ListException("Aeropuerto de destino con código " + updatedRoute.getDestinationAirportCode() + " no encontrado para actualizar la ruta");
        }

        //Remueve la arista antigua del grafo, ya que el peso (la ruta) o incluso los vértices podrían haber cambiado
        airportGraph.removeEdge(airportService.getAirportByCode(oldRoute.getOriginAirportCode()),
                airportService.getAirportByCode(oldRoute.getDestinationAirportCode()));

        //Actualiza la ruta en el mapa y en la lista observable
        routesMap.put(updatedRoute.getRouteId(), updatedRoute);
        int index = observableRoutes.indexOf(oldRoute);
        if (index >= 0) {
            observableRoutes.set(index, updatedRoute); //Reemplaza la ruta antigua con la actualizada
        }

        //Añade la nueva arista al grafo con la ruta actualizada como peso
        airportGraph.addEdgeWeight(originAirport, destinationAirport, updatedRoute);

        try {
            //guarda los cambios
            saveRoutes();
        } catch (Exception e) {
            //Si el guardado falla, se realiza un ROLLBACK:
            // Vuelve a colocar la ruta antigua en el mapa
            // Remueve la arista recién añadida y vuelve a agregar la arista antigua
            routesMap.put(updatedRoute.getRouteId(), oldRoute);
            try {
                airportGraph.removeEdge(originAirport, destinationAirport); //Remueve la arista con la ruta actualizada
                airportGraph.addEdgeWeight(airportService.getAirportByCode(oldRoute.getOriginAirportCode()),
                        airportService.getAirportByCode(oldRoute.getDestinationAirportCode()),
                        oldRoute); //Vuelve a agregar la arista con la ruta antigua
            } catch (GraphException rollbackEx) {
                System.err.println("CRITICAL: Failed to rollback updateEdge after save error: " + rollbackEx.getMessage());
            }
            throw new GraphException("Failed to save route changes after update: " + e.getMessage());
        }
        System.out.println("Ruta " + updatedRoute.getRouteId() + " actualizada y persistida");
        return true;
    }

    public void generateInitialRandomRoutes(int count) throws ListException, GraphException {
        //1. Obtiene todos los aeropuertos disponibles del AirportService
        List<Object> allAirportsObjects = airportService.getAllAirportsAsList();
        List<Airport> allAirports = new ArrayList<>();
        for (Object obj : allAirportsObjects) {
            if (obj instanceof Airport) {
                allAirports.add((Airport) obj);
            } else {
                System.err.println("Warning: Object in AirportService.getAllAirports is not an Airport during random route generation. Type: " + (obj != null ? obj.getClass().getName() : "null"));
            }
        }

        //2. Valida que haya suficientes aeropuertos para crear rutas (al menos dos)
        if (allAirports.size() < 2) {
            throw new ListException("Necesitas al menos dos aeropuertos para generar rutas aleatorias");
        }

        int generatedCount = 0;
        while (generatedCount < count) {
            //3. Selecciona aeropuertos de origen y destino aleatorios, asegurándose de que no sean el mismo
            Airport origin = allAirports.get(Utility.random(allAirports.size()));
            Airport destination;
            do {
                destination = allAirports.get(Utility.random(allAirports.size()));
            } while (origin.equals(destination));

            //4.Genera datos aleatorios para nueva ruta
            String routeId = "R" + Utility.random(1000000);
            String airline = "Airline" + (char)('A' + Utility.random(26)) + (char)('A' + Utility.random(26));
            double distance = 500 + Utility.random(5000);
            double duration = Utility.randomMinMax(1, 10);
            double price = Utility.randomMinMax(50, 1000);
            LocalTime departureTime = LocalTime.of(Utility.random(24), Utility.random(60));
            LocalTime arrivalTime = departureTime.plusHours((long) duration).plusMinutes((long) ((duration - (long)duration) * 60));

            // 5. Crea una nueva instancia de la ruta
            Route newRoute = new Route(routeId, origin.getCode(), destination.getCode(),
                    airline, duration, distance, price,
                    departureTime, arrivalTime);

            try {
                //6. Intenta añadir la ruta usando el método 'addRoute' del servicio, que maneja validaciones y persistencia
                addRoute(newRoute);
                observableRoutes.add(newRoute); //Añade a lista observable para la UI
                generatedCount++; //Incrementa el contador solo si la ruta se añadió
            } catch (ListException | GraphException e) {
                //Si ocurre un error (ej. ID de ruta duplicado generado aleatoriamente), se imprime una advertencia
                //El bucle 'while' asegura que se intente hasta alcanzar el 'count' de rutas únicas
                System.err.println("Warning: Error generating or adding random route " + routeId + ": " + e.getMessage());
            }
        }
        System.out.println(generatedCount + " rutas aleatorias generadas y guardadas.");
    }

    public boolean containsRoute(int originAirportCode, int destinationAirportCode) throws ListException {
        //1.Obtiene los Airport de acuerdo a los códigos
        Airport originInGraph = airportService.getAirportByCode(originAirportCode);
        Airport destinationInGraph = airportService.getAirportByCode(destinationAirportCode);

        //2. Si alguno de los aeropuertos no existe, no puede haber ruta
        if (originInGraph == null || destinationInGraph == null) {
            return false;
        }

        try {
            //3. Verifica si existe una arista directa (ruta) entre los dos aeropuertos
            return airportGraph.containsEdge(originInGraph, destinationInGraph);
        } catch (GraphException | ListException e) {
            System.err.println("Error checking route: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> findShortestRoute(int originAirportCode, int destinationAirportCode, String costType)
            throws GraphException, ListException {

        //Validacioness: grafo vaco, existencia de aeropuertos y tipo de costo válido
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

        //2. Inicializamos estructuras de datos para usar Dijkstra
        Map<Airport, Double> minCosts = new HashMap<>(); //Almacena el costo mínimo conocido para llegar a cada aeropuerto
        Map<Airport, Airport> predecessors = new HashMap<>(); //Almacena el aeropuerto anterior en la ruta más corta
        PriorityLinkedQueue pq = new PriorityLinkedQueue(); //Cola de prioridad para explorar los nodos de Dijkstra

        try {
            minCosts.put(origin, 0.0); //El costo al origen == 0
            pq.enQueue(new DijkstraNode(origin, 0.0, new ArrayList<>(List.of(origin))), (int) 0.0); //Añade el nodo de origen a la cola

            List<Airport> shortestPathAirports = null; //Almacenará la secuencia de aeropuertos de la ruta más corta

            //3. Bucle de Dijkstra
            while (!pq.isEmpty()) {
                DijkstraNode current = (DijkstraNode) pq.deQueue(); // Obtiene el nodo con el costo más bajo
                Airport currentAirport = current.getAirport();

                //Si ya se encontró un camino más corto al aeropuerto actual, se ignora esta exploración
                if (current.getCost() > minCosts.getOrDefault(currentAirport, Double.MAX_VALUE) + 1e-9) {
                    continue;
                }

                //Si el aeropuerto actual es el destino, se ha encontrado la ruta más corta.
                if (Objects.equals(currentAirport, destination)) {
                    shortestPathAirports = current.getPath();
                    break;
                }

                //Obtiene el vrtice correspondiente al aeropuerto actual para explorar sus aristas
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
                    continue; //Si no hay vetice o aristas salientes salta
                }

                //4. Recorremos las aristas salientes (rutas directas a aeropuertos vecinos)
                for (int i = 1; i <= currentGraphVertex.edgesList.size(); i++) {
                    Node edgeNode = currentGraphVertex.edgesList.getNode(i);
                    if (edgeNode == null || !(edgeNode.data instanceof EdgeWeight)) {
                        System.err.println("Warning: Non-EdgeWeight object found in edgesList for vertex " + currentAirport.getCode() + ". Data type: " + (edgeNode != null ? edgeNode.data.getClass().getName() : "null"));
                        continue;
                    }
                    EdgeWeight edgeWeight = (EdgeWeight) edgeNode.data;

                    Airport neighborAirport = (Airport) edgeWeight.getEdge(); //El aeropuerto al que lleva esta arista
                    Route routeDetails = (Route) edgeWeight.getWeight(); //Los detalles de la ruta (peso)

                    if (neighborAirport == null || routeDetails == null) {
                        System.err.println("Warning: Malformed EdgeWeight object found for vertex " + currentAirport.getCode() + ". Missing neighbor airport or route details.");
                        continue;
                    }

                    //Calcula el costo de esta arista según el 'costType' seleccionado
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

                    double newCost = current.getCost() + edgeCost; //Nuevo costo total para llegar al vecino

                    //Si se encuentra un camino más corto al 'neighborAirport'
                    if (newCost < minCosts.getOrDefault(neighborAirport, Double.MAX_VALUE)) {
                        minCosts.put(neighborAirport, newCost); // Actualiza el costo mínimo.
                        predecessors.put(neighborAirport, currentAirport); // Establece el predecesor.

                        List<Airport> newPath = new ArrayList<>(current.getPath()); //Construye nuevo camino
                        newPath.add(neighborAirport);

                        pq.enQueue(new DijkstraNode(neighborAirport, newCost, newPath), (int) newCost); //añade el vecino a la cola
                    }
                }
            }

            //Si se encontro la ruta más corta, la convierte a una lista de códigos de aeropuerto
            if (shortestPathAirports != null) {
                List<Integer> resultCodes = new ArrayList<>();
                for (Airport airport : shortestPathAirports) {
                    resultCodes.add(airport.getCode());
                }
                return resultCodes;
            } else {
                return null; //No encontró una ruta
            }
        } catch (QueueException e) {
            throw new GraphException("Error processing shortest path with Priority Queue: " + e.getMessage());
        }
    }

    /**
     * Finds and returns the top 5 airports with the highest number of associated routes
     * (either as origin or destination).
     * @return A list of the top 5 (or fewer if not enough) airports, sorted by route count descending.
     * Returns an empty list if no airports or routes exist.
     */
    public List<Airport> getTop5AirportsByRouteCount() throws ListException {
        Map<Airport, Integer> airportRouteCounts = new HashMap<>();

        // Initialize counts for all airports to 0
        List<Object> allAirportsObjects = airportService.getAllAirportsAsList();
        for (Object obj : allAirportsObjects) {
            if (obj instanceof Airport) {
                airportRouteCounts.put((Airport) obj, 0);
            }
        }

        // Iterate through all routes and increment counts for origin and destination airports
        for (Route route : routesMap.values()) {
            Airport origin = airportService.getAirportByCode(route.getOriginAirportCode());
            Airport destination = airportService.getAirportByCode(route.getDestinationAirportCode());

            if (origin != null) {
                airportRouteCounts.put(origin, airportRouteCounts.getOrDefault(origin, 0) + 1);
            }
            if (destination != null && !destination.equals(origin)) { // Avoid double-counting if origin == destination (though routes should prevent this)
                airportRouteCounts.put(destination, airportRouteCounts.getOrDefault(destination, 0) + 1);
            }
        }

        // Sort the map entries by count in descending order and limit to top 5
        List<Map.Entry<Airport, Integer>> sortedEntries = airportRouteCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<Airport> topAirports = new ArrayList<>();
        for (int i = 0; i < Math.min(5, sortedEntries.size()); i++) {
            topAirports.add(sortedEntries.get(i).getKey());
        }

        System.out.println("Top 5 Airports by Route Count:");
        for (Airport airport : topAirports) {
            System.out.println("- " + airport.getName() + " (" + airport.getCode() + "): " + airportRouteCounts.get(airport) + " routes.");
        }

        return topAirports;
    }

    public void removeAirportFromGraph(int airportCode) throws GraphException, ListException {
        //Obtiiene el aeropuerto a eliminar del servicio de aeropuertos
        Airport airportToRemove = airportService.getAirportByCode(airportCode);

        // 2. Valida la existencia del aeropuerto y si está en el grafo
        if (airportToRemove == null) {
            throw new GraphException("Airport with code " + airportCode + " not found in AirportService, cannot remove from graph.");
        }
        if (!airportGraph.containsVertex(airportToRemove)) {
            System.out.println("Warning: Airport " + airportCode + " not found in the graph. No removal required from graph.");
            return;
        }

        System.out.println("Removing airport " + airportCode + " and its associated routes from the graph...");

        //3. Identifica todas las rutas que tienen este aeropuerto como origen o destino
        List<String> routeIdsToRemove = new ArrayList<>();
        for (Route route : new ArrayList<>(routesMap.values())) {
            if (route.getOriginAirportCode() == airportCode || route.getDestinationAirportCode() == airportCode) {
                routeIdsToRemove.add(route.getRouteId());
            }
        }

        //4.Elimina las rutas identificadas del mapa y sus aristas del grafo
        for (String routeId : routeIdsToRemove) {
            Route routeRemovedFromMap = routesMap.get(routeId);
            if (routeRemovedFromMap != null) {
                routesMap.remove(routeId); //Elimina del mapa de rutas

                Airport origin = airportService.getAirportByCode(routeRemovedFromMap.getOriginAirportCode());
                Airport destination = airportService.getAirportByCode(routeRemovedFromMap.getDestinationAirportCode());

                if (origin != null && destination != null) {
                    try {
                        airportGraph.removeEdge(origin, destination); //Elimina la arista del grafo
                    } catch (GraphException e) {
                        System.err.println("Error removing edge for route " + routeId + ": " + e.getMessage());
                    }
                }
            }
        }

        //5.remueve el vértice del aeropuerto del grafo
        airportGraph.removeVertex(airportToRemove);
        System.out.println("Airport " + airportCode + " and its edges removed from the graph.");

        //6. Guarda los cambios
        saveRoutes();
    }

    public Route getRouteById(String routeId) {
        //Busca y devuelve una ruta por su ID desde el mapa de rutas
        return routesMap.get(routeId);
    }

    public List<Route> getAllRoutes() {
        //Devuelve una lista con todas las rutas del mapa
        return new ArrayList<>(routesMap.values());
    }

    @Override
    public String toString() {
        //Devuelve un string del grafo de aeropuertos
        return airportGraph.toString();
    }

}