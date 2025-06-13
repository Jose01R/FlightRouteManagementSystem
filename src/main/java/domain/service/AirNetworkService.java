package domain.service;

import domain.common.Airport;
import domain.common.Route;
import domain.graph.DijkstraNode;
import domain.graph.DirectedSinglyLinkedListGraph;
import domain.graph.GraphException;
import domain.graph.Vertex;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.PriorityLinkedQueue;
import domain.linkedqueue.QueueException;
import util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.PriorityQueue;

import java.util.Comparator;
import java.util.Objects; //Para Objects .equals

public class AirNetworkService {
    private DirectedSinglyLinkedListGraph airportGraph;
    private Map<Integer, Airport> airports;

    public AirNetworkService() {
        this.airportGraph = new DirectedSinglyLinkedListGraph();
        this.airports = new HashMap<>();

        //Para cargar automáticamente todos los aeropuertos del JSON al iniciar el service
        // try {
        //     loadAllAirportsIntoGraph();
        // } catch (IOException | ListException | GraphException e) {
        //     System.err.println("Error al cargar aeropuertos iniciales en el grafo: " + e.getMessage());
        // }

    }

    /**
     * Carga todos los aeropuertos desde el archivo JSON al grafo
     * para inicializar el estado del grafo con datos persistentes
     * @throws IOException Si hay un error de lectura/escritura del archivo
     * @throws ListException Si hay un problema con las operaciones de la lista enlazada
     * @throws GraphException Si hay un problema al añadir vértices al grafo
     */
    public void loadAllAirportsIntoGraph() throws IOException, ListException, GraphException {
        //Obtenemos lista de aeropuertos desde el JSON
        DoublyLinkedList airportsJsonList = AirportsData.getElements();

        if (airportsJsonList != null && !airportsJsonList.isEmpty()) {
            domain.linkedlist.Node current = airportsJsonList.getFirstNode(); //obtenemos un node
            while (current != null) {
                if (current.data instanceof Airport) { //current.data es el airport
                    try {
                        addAirport((Airport) current.data); //Añadimos cada airport al grafo
                    } catch (GraphException e) {
                        //Evitamos duplicados si se carga más de una vez
                        if (!e.getMessage().contains("already exists")) {
                            throw e; //excepciones de grafo
                        }
                        System.out.println("Advertencia: Aeropuerto " + ((Airport) current.data).getCode() + " ya existe en el grafo. Ignorando.");
                    }
                }
                current = current.next;
            }
        }
    }

    /**
     * Agrega un aeropuerto al grafo
     * @param airport El objeto Airport a agregar
     * @throws GraphException Si el aeropuerto ya existe en el grafo o hay un problema interno
     * @throws ListException Si hay un problema con las operaciones de la lista enlazada (del grafo)
     */
    public void addAirport(Airport airport) throws GraphException, ListException {
        if (airport == null) {
            throw new IllegalArgumentException("Airport cannot be null.");
        }
        if (airports.containsKey(airport.getCode())) {
            throw new GraphException("Airport with code " + airport.getCode() + " already exists in the graph.");
        }
        airportGraph.addVertex(airport);
        airports.put(airport.getCode(), airport);
    }

    /**
     * Agrega una ruta entre dos aeropuertos en el grafo en memoria
     * Los aeropuertos de origen y destino deben existir en el AirportsData y se asegurará
     * que existan en el grafo antes de añadir la arista
     * @param originAirportCode El código del aeropuerto de origen
     * @param destinationAirportCode El código del aeropuerto de destino
     * @param route La información de la ruta (peso de la arista)
     * @throws GraphException Si alguno de los aeropuertos no existe en los datos o si la arista ya existe en el grafo
     * @throws IOException Si hay un problema al obtener los aeropuertos del archivo JSON (vía AirportsData)
     */
    public void addRoute(int originAirportCode, int destinationAirportCode, Route route)
            throws GraphException, ListException, IOException {
        if (route == null) {
            throw new IllegalArgumentException("Route details cannot be null.");
        }

        //Obtener los aeropuertos del AirportsData (que los lee del JSON)
        Airport originAirport = AirportsData.getAirportByCode(originAirportCode);
        Airport destinationAirport = AirportsData.getAirportByCode(destinationAirportCode);

        if (originAirport == null) {
            throw new GraphException("Origin airport with code " + originAirportCode + " not found in data (AirportsData).");
        }
        if (destinationAirport == null) {
            throw new GraphException("Destination airport with code " + destinationAirportCode + " not found in data (AirportsData).");
        }

        //Aeropuertos deben estar en el grafo antes de añadir la arista
        //Metodo addAirport() hace validacion si el aeropuerto existe
        try {
            addAirport(originAirport);
        } catch (GraphException e) {
            if (!e.getMessage().contains("already exists")) {
                throw e;
            }
        }
        try {
            addAirport(destinationAirport);
        } catch (GraphException e) {
            if (!e.getMessage().contains("already exists")) {
                throw e; //Relanza si no es el error de "already exists"
            }
        }


        //Agregamos la arista con el peso (Route)
        //Pueden ser mnutos, KM o precio
        Airport originInGraph = airports.get(originAirportCode);
        Airport destinationInGraph = airports.get(destinationAirportCode);

        if (originInGraph == null || destinationInGraph == null) {
            throw new GraphException("Internal error: Airport found in data but not in graph's in-memory map.");
        }

        airportGraph.addEdgeWeight(originInGraph, destinationInGraph, route);
    }

    /**
     * Elimina una ruta específica entre dos aeropuertos del grafo
     * @throws GraphException Si alguno de los aeropuertos no existe en el grafo o no hay ruta entre ellos
     * @throws IOException Si hay un problema al obtener los aeropuertos del JSON
     */
    public void removeRoute(int originAirportCode, int destinationAirportCode)
            throws GraphException, ListException, IOException {
        // Obtener los objetos Airport de la clase AirportsData (que los lee del JSON)
        Airport originAirport = AirportsData.getAirportByCode(originAirportCode);
        Airport destinationAirport = AirportsData.getAirportByCode(destinationAirportCode);

        if (originAirport == null || destinationAirport == null) {
            throw new GraphException("Origin or destination airport not found in data to remove route.");
        }

        // Asegurarse de usar las instancias de Airport que están en el grafo
        Airport originInGraph = airports.get(originAirportCode);
        Airport destinationInGraph = airports.get(destinationAirportCode);

        if (originInGraph == null || destinationInGraph == null) {
            throw new GraphException("Origin or destination airport not found in the graph's in-memory map to remove route.");
        }

        airportGraph.removeEdge(originInGraph, destinationInGraph);
    }

    /**
     * Elimina un aeropuerto (VERTEX) del grafo  y todas sus aristas asociadas
     * SOLO elimina el aeropuerto del grafo no del JSON
     *
     * @throws GraphException Si hay un problema al eliminar el vértice
     * @throws ListException Si hay un problema con las operaciones de la linked list (del grafo)
     */
    public void removeAirportFromGraph(int airportCode) throws GraphException, ListException {
        // Verificar si el aeropuerto existe en el mapa en memoria del servicio
        if (!airports.containsKey(airportCode)) {
            System.out.println("Advertencia: Aeropuerto con código " + airportCode + " no encontrado en el grafo en memoria para eliminar. Puede que ya haya sido eliminado o nunca se añadió.");
            return; // No hay nada que eliminar si no está en el grafo en memoria
        }

        //Obtener el Airport de la colección en memoria
        Airport airportToRemove = airports.get(airportCode);

        //Eliminar el vértice (aeropuerto) del grafo con sus aristas
        airportGraph.removeVertex(airportToRemove);

        //Suprimimos el aeropuerto del mapa de aeropuertos en memoria
        airports.remove(airportCode);

        System.out.println("Aeropuerto " + airportCode + " y sus rutas han sido eliminados del grafo en memoria.");
    }


    /**
     * Verifica si existe una ruta directa entre dos aeropuertos en el grafo
     *
     * @throws GraphException Si el grafo está vacío o hay un problema con los vertexes
     * @throws IOException Si hay un problema al obtener los aeropuertos del JSON
     */
    public boolean containsRoute(int originAirportCode, int destinationAirportCode)
            throws GraphException, ListException, IOException {
        // Obtener los objetos Airport de la clase AirportsData (que los lee del JSON)
        Airport originAirport = AirportsData.getAirportByCode(originAirportCode);
        Airport destinationAirport = AirportsData.getAirportByCode(destinationAirportCode);

        if (originAirport == null || destinationAirport == null) {
            return false; //Si no existen en los datos persistentes, no pueden tener una ruta.
        }

        // Asegurarse de usar las instancias de Airport que están en el grafo
        Airport originInGraph = airports.get(originAirportCode);
        Airport destinationInGraph = airports.get(destinationAirportCode);

        if (originInGraph == null || destinationInGraph == null) {
            return false; //Si no están en el grafo en memoria, no hay ruta en el grafo.
        }

        //true si existe una ruta directa
        return airportGraph.containsEdge(originInGraph, destinationInGraph);
    }

    /**
     * Método auxiliar: Obtener la misma instancia del objeto Airport
     * que está almacenada en el grafo, dado un objeto Airport de búsqueda
     */
    private Airport getAirportFromGraph(Airport searchAirport) {
        //La forma más eficiente de obtener el objeto del grafo es usar el mapa airports
        //que ya mantiene las instancias que están en el grafo
        if (searchAirport == null) return null;

        //Retrna un Airport del grafo o null caso contrario
        return airports.get(searchAirport.getCode());
    }


    /**
     *Encuentra la ruta más corta entre dos aeropuertos utilizando Dijkstra.
     *Puede ser por tiempo, distancia o precio, según costType
     */
    public List<Integer> findShortestRoute(int originAirportCode, int destinationAirportCode, String costType)
            throws GraphException, ListException, IOException {

        if (airportGraph.isEmpty()) {
            throw new GraphException("Airport network is empty. Please load airports and routes first.");
        }

        //Obtener los objetos Airport completos del archivo (JSON) para verificación inicial
        Airport origin = AirportsData.getAirportByCode(originAirportCode);
        Airport destination = AirportsData.getAirportByCode(destinationAirportCode);

        if (origin == null) {
            throw new GraphException("Origin airport with code " + originAirportCode + " not found in data (AirportsData).");
        }
        if (destination == null) {
            throw new GraphException("Destination airport with code " + destinationAirportCode + " not found in data (AirportsData).");
        }

        //Debemos usar las instancias de Airport que ESTAN en el grafo
        Airport actualOrigin = getAirportFromGraph(origin);
        Airport actualDestination = getAirportFromGraph(destination);

        if (actualOrigin == null) {
            throw new GraphException("Origin airport with code " + originAirportCode + " not found in the in-memory graph.");
        }
        if (actualDestination == null) {
            throw new GraphException("Destination airport with code " + destinationAirportCode + " not found in the in-memory graph.");
        }

        if (!costType.equalsIgnoreCase("duration") && !costType.equalsIgnoreCase("distance") && !costType.equalsIgnoreCase("price")) {
            throw new IllegalArgumentException("Invalid cost type. Must be 'duration', 'distance', or 'price'.");
        }

        Map<Airport, Double> minCosts = new HashMap<>();
        Map<Airport, Airport> predecessors = new HashMap<>(); //Para reconstruir la ruta
        //PriorityQueue para Dijkstra
        PriorityLinkedQueue pq = new PriorityLinkedQueue(); //Usando tu clase

        try {
            minCosts.put(actualOrigin, 0.0);
            //Cola de minima prioridad (donde queremos sacar el elemento con el menor costo primero) Integer.MAX_VALUE - costo
            //Un costo más pequeño resultará en una prioridad entera más grande (mayor valor)
            //Al usar int puede ser menos preciso
            int initialPriority = (int) (Integer.MAX_VALUE - 0.0); //Costo inicial 0
            pq.enQueue(new DijkstraNode(actualOrigin, 0.0, List.of(actualOrigin)), initialPriority);

            List<Airport> shortestPathAirports = null; //Almacenará la ruta de objetos Airport

            while (!pq.isEmpty()) {
                DijkstraNode current = (DijkstraNode) pq.deQueue();
                Airport currentAirport = current.getAirport();

                //Si ya procesamos un camino más corto a este aeropuerto, ignoramos esta iteración
                //Comparamos Airports

//                if (current.getCost() > minCosts.getOrDefault(currentAirport, Double.MAX_VALUE)) {
//                    continue;
//                }

                if (current.getCost() > minCosts.getOrDefault(currentAirport, Double.MAX_VALUE) && Utility.compare(current.getAirport(), currentAirport) != 0) {
                    continue;
                }

                //Si llegamos al destino, almacenamos la ruta y terminamos
                if (util.Utility.compare(currentAirport, actualDestination) == 0) {
                    shortestPathAirports = current.getPath();
                    break; //Ruta encontrada, salimos del bucle
                }

                //Obtener el Vertex correspondiente en el grafo para acceder a sus aristas
                Vertex currentGraphVertex = null;
                SinglyLinkedList vertexList = airportGraph.getVertexList();
                if (vertexList != null && !vertexList.isEmpty()) {
                    for (int i = 1; i <= vertexList.size(); i++) {
                        Node nodeFromList = vertexList.getNode(i);
                        if (nodeFromList != null && nodeFromList.data instanceof Vertex) {
                            Vertex potentialVertex = (Vertex) nodeFromList.data;
                            //Comparamos el Airport dentro del Vertex con currentAirport
                            if (Utility.compare(potentialVertex.data, currentAirport) == 0) {
                                currentGraphVertex = potentialVertex;
                                break;
                            }
                        }
                    }
                }

                if (currentGraphVertex == null || currentGraphVertex.edgesList == null || currentGraphVertex.edgesList.isEmpty()) {
                    continue; //No hay aristas salientes desde este aeropuerto o el vértice no fue encontrado
                }

                //Iterar sobre las aristas salientes (vecinos)
                for (int i = 1; i <= currentGraphVertex.edgesList.size(); i++) {
                    domain.graph.EdgeWeight edgeWeight = (domain.graph.EdgeWeight) currentGraphVertex.edgesList.getNode(i).data;
                    Airport neighborAirport = (Airport) edgeWeight.getEdge(); //El aeropuerto destino de esta arista
                    Route routeDetails = (Route) edgeWeight.getWeight(); //El peso de la arista

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
                    }

                    double newCost = current.getCost() + edgeCost;

                    //Si encontramos un camino más corto al vecino
                    if (newCost < minCosts.getOrDefault(neighborAirport, Double.MAX_VALUE)) {
                        minCosts.put(neighborAirport, newCost);
                        predecessors.put(neighborAirport, currentAirport); //Almacenamos el predecesor para reconstruir la ruta

                        List<Airport> newPath = new ArrayList<>(current.getPath());
                        newPath.add(neighborAirport); //Añade el vecino al camino actual
                        //Calcula la prioridad para el nuevo DijkstraNode
                        int newPriority = (int) (Integer.MAX_VALUE - newCost); // Mayor prioridad para menor costo
                        pq.enQueue(new DijkstraNode(neighborAirport, newCost, newPath), newPriority); // Enqueue con tu cola
                    }
                }
            }

            //Convertir la lista de objetos Airport a lista de códigos de aeropuerto
            if (shortestPathAirports != null) {
                List<Integer> resultCodes = new ArrayList<>();
                for (Airport airport : shortestPathAirports) {
                    resultCodes.add(airport.getCode());
                }
                return resultCodes;
            } else {
                return null; //No se encontró una ruta al destino
            }
        } catch (QueueException e) {
            throw new GraphException("Error processing shortest path with Priority Queue: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return airportGraph.toString();
    }

}