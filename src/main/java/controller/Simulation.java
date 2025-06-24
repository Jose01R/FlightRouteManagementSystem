package controller;

import domain.common.Airport;
import domain.common.Route;
import domain.graph.*;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.service.AirNetworkService;
import domain.service.AirportService;
import domain.service.FlightService;
import domain.service.AirplaneService;
import domain.service.PassengerService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import util.Utility;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Simulation {
    @FXML
    private Pane graph;
    @FXML
    private ImageView image;
    @FXML
    private Button btnShowTopAirportRoutes;

    private DirectedSinglyLinkedListGraph listGraph; // Nuestro grafo principal para dibujar las rutas

    // Mapas para gestionar los círculos de los vértices y las líneas/etiquetas de las aristas
    private Map<Object, Circle> vertexCircles;
    private final javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0); // Para el zoom
    private final Map<String, Line> edgeMap = new HashMap<>();
    private final Map<String, Text> edgeWeightLabels = new HashMap<>();

    // Servicios que utilizamos para obtener y gestionar los datos del sistema
    private AirportService airportService;
    private AirNetworkService airNetworkService;
    private FlightService flightService;
    private AirplaneService airplaneService;
    private PassengerService passengerService;

    // Nuevo: Guardar el estado original de los elementos para poder "desresaltar"
    private Map<String, Color> originalEdgeColors = new HashMap<>();
    private Map<String, Double> originalEdgeWidths = new HashMap<>();
    private Map<Object, Color> originalVertexColors = new HashMap<>();
    private Map<Object, Color> originalVertexStrokeColors = new HashMap<>();

    @FXML
    public void initialize() {
        setupMouseZoom(); // Configuramos el zoom con la rueda del ratón
        vertexCircles = new HashMap<>(); // Inicializamos el mapa de círculos para vértices
        graph.getTransforms().add(scaleTransform); // Añadimos la transformación de escala al panel del grafo
        image.getTransforms().add(scaleTransform); // Añadimos la transformación de escala al imagen del grafo

        listGraph = new DirectedSinglyLinkedListGraph(); // Creamos una nueva instancia de nuestro grafo
    }

    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService, AirportService airportService) {
        this.airNetworkService = airNetworkService;
        this.flightService = flightService;
        this.passengerService = passengerService;
        this.airplaneService = airplaneService;
        this.airportService = airportService;
        initializeData(); // Llamamos a la inicialización de datos una vez que los servicios están listos
    }

    private void initializeData() {
        try {
            // AirportService.loadAirports() ya se ejecuta en el constructor de AirportService
            // y carga los aeropuertos desde AirportsData. Por lo tanto, no necesitamos una llamada explícita aquí.

            // Generar datos iniciales si las listas están vacías (excepto aeropuertos, que ya cargamos)
            if (airplaneService.getAllAirplanes().isEmpty()) {
                System.out.println("Generando aviones iniciales...");
                airplaneService.generateInitialRandomAirplanes(10);
            }
            if (passengerService.getAllPassengers().isEmpty()) {
                System.out.println("Generando pasajeros iniciales...");
                passengerService.generateInitialRandomPassengers(50);
            }
            if (flightService.getFlightList().isEmpty()) {
                System.out.println("Generando vuelos iniciales...");
                flightService.generateFlightsRandom(25);
            }

            // Si no hay rutas cargadas, podemos crear algunas rutas 'base'
            // para que Dijkstra tenga una red sobre la cual calcular.
            if (airNetworkService.getAllRoutes().isEmpty()) {
                System.out.println("No hay rutas iniciales. Generando algunas aleatorias para que el grafo tenga aristas.");
                airNetworkService.generateInitialRandomRoutes(20);
            }

            // Al iniciar la aplicación, se dibuja el grafo con todas las rutas existentes.
            drawAllRoutesGraph();

        } catch (Exception e) {
            System.err.println("Error durante la inicialización del controlador: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización", "No se pudieron cargar o generar los datos iniciales: " + e.getMessage());
        }
    }

    /**
     * Dibuja el grafo principal mostrando todos los aeropuertos y las rutas existentes.
     */
    private void drawAllRoutesGraph() {
        // clearGraph(); // NO LLAMAR clearGraph() AQUÍ si queremos mantener el grafo base

        try {
            DoublyLinkedList allAirportsList = airportService.getAllAirports(); // Obtenemos todos los aeropuertos cargados
            List<Airport> allAirports = allAirportsList.toList().stream()
                    .filter(obj -> obj instanceof Airport)
                    .map(obj -> (Airport) obj)
                    .collect(Collectors.toList());

            if (allAirports.isEmpty()) {
                System.out.println("No hay aeropuertos para dibujar el grafo principal.");
                showAlert(Alert.AlertType.INFORMATION, "Sin Aeropuertos", "No hay aeropuertos cargados para mostrar la simulación.");
                return;
            }

            // Limpiamos los elementos visuales existentes y el grafo lógico si ya se había dibujado algo
            // Esto asegura que cada vez que se llama a drawAllRoutesGraph (por ejemplo, al reiniciar la app),
            // el grafo se dibuje desde cero.
            graph.getChildren().clear();
            vertexCircles.clear();
            edgeMap.clear();
            edgeWeightLabels.clear();
            listGraph.clear();
            originalEdgeColors.clear(); // Limpiamos también los estados originales
            originalEdgeWidths.clear();
            originalVertexColors.clear();
            originalVertexStrokeColors.clear();


            // Primero, añadimos todos los aeropuertos como vértices al grafo de dibujo.
            for (Airport airport : allAirports) {
                listGraph.addVertex(airport);
            }

            // Agrega todas las aristas existentes en el AirNetworkService a nuestro grafo de dibujo
            List<Route> allRoutes = airNetworkService.getAllRoutes();
            for (Route route : allRoutes) {
                Airport origin = airportService.getAirportByCode(route.getOriginAirportCode());
                Airport destination = airportService.getAirportByCode(route.getDestinationAirportCode());
                if (origin != null && destination != null) {
                    try {
                        // Asegúrate de que los vértices existan antes de añadir la arista
                        listGraph.addVertex(origin); // Solo para asegurarnos que estén si no se agregaron ya
                        listGraph.addVertex(destination);
                        listGraph.addEdgeWeight(origin, destination, route.getDistanceKm()); // Usar distancia para dibujar
                    } catch (GraphException e) {
                        System.err.println("Error al añadir arista para dibujar todas las conexiones: " + e.getMessage());
                    }
                }
            }

            // Finalmente, renderizamos el grafo en el panel de la interfaz gráfica
            renderGraphOnPane(listGraph, airportService);

        } catch (ListException | GraphException e) {
            System.err.println("Error al dibujar el grafo principal: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al Dibujar Grafo", "Ocurrió un error al intentar dibujar el grafo principal: " + e.getMessage());
        }
    }


    /**
     * Dibuja una ruta específica resaltando los aeropuertos y las aristas de esa ruta.
     * Este método ya no es necesario para la nueva funcionalidad de "resaltar".
     * Se mantiene por si acaso, pero no se usará en handleShowTopAirportRoutes.
     */
    private void drawSpecificPath(List<Integer> pathCodes, AirportService airportService, AirNetworkService airNetworkService, DirectedSinglyLinkedListGraph graphToRender) throws ListException, GraphException {
        // Este método ahora se usará para dibujar una ruta *aislada* si fuera necesario,
        // pero la lógica de handleShowTopAirportRoutes ha cambiado.
        clearGraph();

        // Primero, añadimos todos los aeropuertos de la ruta como vértices al grafo de dibujo.
        for (Integer airportCode : pathCodes) {
            Airport airport = airportService.getAirportByCode(airportCode);
            if (airport != null) {
                graphToRender.addVertex(airport);
            }
        }

        // Luego, añadimos las aristas (rutas) que forman el camino más corto
        for (int i = 0; i < pathCodes.size() - 1; i++) {
            int originCode = pathCodes.get(i);
            int destinationCode = pathCodes.get(i + 1);

            Airport origin = airportService.getAirportByCode(originCode);
            Airport destination = airportService.getAirportByCode(destinationCode);

            // Busca la ruta específica en el AirNetworkService para obtener su distancia/peso
            Route route = airNetworkService.getAllRoutes().stream()
                    .filter(r -> r.getOriginAirportCode() == originCode && r.getDestinationAirportCode() == destinationCode)
                    .findFirst()
                    .orElse(null);

            if (origin != null && destination != null && route != null) {
                graphToRender.addEdgeWeight(origin, destination, route.getDistanceKm()); // O cualquier otra propiedad de Route para el peso
            }
        }
        renderGraphOnPane(graphToRender, airportService); // Renderiza el grafo con la ruta específica
    }


    private void clearGraph() {
        graph.getChildren().clear(); // Limpiamos todos los elementos visuales del panel del grafo
        vertexCircles.clear(); // Limpiamos los mapas que guardan referencias a los elementos visuales
        edgeMap.clear();
        edgeWeightLabels.clear();
        listGraph.clear(); // Limpiamos la estructura de datos del grafo lógico
        originalEdgeColors.clear(); // Limpiamos también los estados originales
        originalEdgeWidths.clear();
        originalVertexColors.clear();
        originalVertexStrokeColors.clear();
    }

    // Método para renderizar el grafo en el panel de JavaFX
    private void renderGraphOnPane(DirectedSinglyLinkedListGraph graphToRender, AirportService airportService) throws GraphException, ListException {
        Object[] currentVertices = getGraphVertices(graphToRender); // Obtenemos todos los vértices del grafo a renderizar
        if (currentVertices.length == 0) return; // Si no hay vértices, salimos

        double paneWidth = graph.getWidth(); // Obtenemos las dimensiones del panel
        double paneHeight = graph.getHeight();

        // Aseguramos que las dimensiones del panel no sean cero, asignando valores por defecto si es necesario
        if (paneWidth == 0) paneWidth = 1423;
        if (paneHeight == 0) paneHeight = 589;

        Map<Object, double[]> vertexPositions = new HashMap<>(); // Almacenamos las posiciones (x, y) de cada vértice
        Random rand = new Random(); // Usamos un generador de números aleatorios para posiciones dinámicas

        // Definimos un área de padding para que los vértices no queden muy pegados a los bordes
        double minX = 50;
        double maxX = paneWidth - 50;
        double minY = 50;
        double maxY = paneHeight - 50;

        // Utilizamos un conjunto para llevar un registro de las posiciones ocupadas y evitar superposiciones
        Set<String> occupiedPositions = new HashSet<>();

        // Iteramos sobre todos los vértices para calcular sus posiciones y dibujarlos
        for (Object vertexObj : currentVertices) {
            Airport airport;
            if (vertexObj instanceof Airport) {
                airport = (Airport) vertexObj;
            } else if (vertexObj instanceof Integer) { // Si el vértice es un código, lo buscamos
                airport = airportService.getAirportByCode((Integer) vertexObj);
            } else {
                airport = null;
            }

            if (airport == null) {
                System.err.println("Advertencia: No se pudo encontrar el objeto Airport para el vértice: " + vertexObj);
                continue;
            }

            // Si el círculo ya existe, no lo volvemos a dibujar, solo actualizamos su estado si es necesario
            if (vertexCircles.containsKey(airport)) {
                continue; // El vértice ya está dibujado, no lo duplicamos.
            }

            double x, y;
            boolean positionFound = false;

            double safeRadius = 50; // Radio de seguridad para evitar superposiciones
            int attempts = 0;
            do {
                x = rand.nextDouble() * (maxX - minX) + minX;
                y = rand.nextDouble() * (maxY - minY) + minY;
                String posKey = String.format("%.0f,%.0f", x / safeRadius, y / safeRadius); // Clave para la posición aproximada
                if (!occupiedPositions.contains(posKey)) {
                    occupiedPositions.add(posKey);
                    positionFound = true;
                }
                attempts++;
            } while (!positionFound && attempts < 100); // Intentar hasta 100 veces para encontrar una posición no superpuesta
            if (!positionFound) {
                System.err.println("Advertencia: No se pudo encontrar una posición no superpuesta para " + airport.getCode() + " después de varios intentos. Usando una posición por defecto.");
                x = minX + (maxX - minX) / 2; // Posición de fallback
                y = minY + (maxY - minY) / 2;
            }


            vertexPositions.put(airport, new double[]{x, y}); // Guardamos la posición del vértice, usando el objeto Airport como clave

            // Dibujamos el círculo que representa el aeropuerto
            Circle circle = new Circle(x, y, 15, Color.WHITE);
            circle.setStroke(Color.LIGHTBLUE);
            circle.setStrokeWidth(2);
            circle.setId("airport_circle_" + airport.getCode());

            // Guardar estado original
            originalVertexColors.put(airport, (Color) circle.getFill());
            originalVertexStrokeColors.put(airport, (Color) circle.getStroke());


            // Creamos el texto con el nombre del aeropuerto
            Text text = new Text(airport.getName());
            text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            text.setFill(Color.WHITE);
            text.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            text.setLayoutX(x - text.getBoundsInLocal().getWidth() / 2);
            text.setLayoutY(y + circle.getRadius() + 15);

            graph.getChildren().addAll(circle, text);
            vertexCircles.put(airport, circle); // Guardamos la referencia al círculo en el mapa usando el objeto Airport

            // Añadir manejador de eventos al círculo para mostrar información al hacer clic
            circle.setOnMouseClicked(event -> {
                showAlert(Alert.AlertType.INFORMATION, "Información del Aeropuerto",
                        "Código: " + airport.getCode() + "\n" +
                                "Nombre: " + airport.getName() + "\n" +
                                "País: " + airport.getCountry() + "\n" +
                                "Estado: " + airport.getStatus());
            });
        }

        // Ahora, iteramos sobre los vértices del grafo para dibujar sus aristas
        SinglyLinkedList sll = graphToRender.getVertexList();
        for (int i = 1; i <= sll.size(); i++) {
            Node sourceNode = sll.getNode(i);
            if (sourceNode != null && sourceNode.getData() instanceof Vertex) {
                Vertex sourceVertex = (Vertex) sourceNode.getData();
                if (sourceVertex != null && !sourceVertex.edgesList.isEmpty()) {
                    SinglyLinkedList edgesOfSource = sourceVertex.edgesList;
                    for (int j = 1; j <= edgesOfSource.size(); j++) {
                        Node edgeNode = edgesOfSource.getNode(j);
                        if (edgeNode != null && edgeNode.getData() instanceof EdgeWeight) {
                            EdgeWeight edge = (EdgeWeight) edgeNode.getData();
                            // Dibuja la arista dirigida entre el origen y el destino con su peso
                            // Aseguramos que sourceVertex.data y edge.getEdge() sean objetos Airport para el mapa
                            drawDirectedEdge((Airport)sourceVertex.data, (Airport)edge.getEdge(), edge.getWeight(), vertexPositions);
                        } else {
                            System.err.println("Advertencia: Objeto no válido o nulo encontrado en edgesList en la posición " + j);
                        }
                    }
                }
            } else {
                System.err.println("Advertencia: Objeto no válido o nulo encontrado en SinglyLinkedList del grafo en la posición " + i);
            }
        }
    }


    @FXML
    private void handleShowTopAirportRoutes(ActionEvent event) {
        // ** NO LLAMAR clearGraph() AQUÍ ** para que no se borre el grafo principal
        // clearGraph();

        // Primero, restaurar todos los elementos del grafo a su estado original (des-resaltar)
        resetGraphHighlight();

        if (airNetworkService == null || airportService == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Servicio", "Los servicios de red aérea o aeropuertos no están inicializados.");
            return;
        }

        try {
            // Obtener los top 5 aeropuertos y sus conteos de rutas
            List<Airport> topAirports = airNetworkService.getTop5AirportsByRouteCount();
            Map<Integer, Long> routeCounts = airNetworkService.getRouteCountsByAirport();

            if (topAirports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Sin Datos", "No hay aeropuertos con rutas disponibles para mostrar.");
                return;
            }

            StringBuilder alertMessage = new StringBuilder();

            // Sección 1: Mostrar los Top 5 Aeropuertos por Conteo de Rutas
            alertMessage.append("Top 5 Aeropuertos por Conteo de Rutas:\n");
            for (Airport airport : topAirports) {
                long count = routeCounts.getOrDefault(airport.getCode(), 0L);
                alertMessage.append("- ").append(airport.getName())
                        .append(" (").append(airport.getCode()).append("): ")
                        .append(count).append(" routes.\n");

                // Resaltar los aeropuertos principales
                Circle airportCircle = vertexCircles.get(airport);
                if (airportCircle != null) {
                    airportCircle.setStroke(Color.GOLD);
                    airportCircle.setStrokeWidth(4);
                    airportCircle.setFill(Color.ORANGE);
                }
            }
            alertMessage.append("\n"); // Espacio antes de la siguiente sección

            // Sección 2: Mostrar Rutas Más Cortas entre los Aeropuertos Principales
            alertMessage.append("Rutas Más Cortas entre Aeropuertos Principales (por Distancia):\n\n");
            boolean foundAnyShortestRoute = false;

            if (topAirports.size() >= 2) {
                Airport primaryOrigin = topAirports.get(0); // Tomamos el primer aeropuerto como origen principal

                for (int i = 1; i < topAirports.size(); i++) {
                    Airport currentDestination = topAirports.get(i);
                    String costType = "distance"; // Puedes cambiar a "duration" o "price"

                    List<Integer> shortestPathCodes = airNetworkService.findShortestRoute(
                            primaryOrigin.getCode(), currentDestination.getCode(), costType);

                    if (shortestPathCodes != null && !shortestPathCodes.isEmpty()) {
                        foundAnyShortestRoute = true;
                        alertMessage.append("Ruta de ").append(primaryOrigin.getName())
                                .append(" a ").append(currentDestination.getName())
                                .append(":\n");

                        // Resaltar los aeropuertos y rutas en el grafo existente
                        for (int j = 0; j < shortestPathCodes.size(); j++) {
                            Airport pathAirport = airportService.getAirportByCode(shortestPathCodes.get(j));
                            if (pathAirport != null) {
                                // Resaltar el círculo del aeropuerto
                                Circle airportCircle = vertexCircles.get(pathAirport);
                                if (airportCircle != null) {
                                    airportCircle.setStroke(Color.RED);
                                    airportCircle.setStrokeWidth(3);
                                    airportCircle.setFill(Color.DARKRED);
                                } else {
                                    // Si el aeropuerto intermedio no existe en el grafo base, podrías añadirlo aquí,
                                    // pero eso complica el "des-resaltado". Lo ideal es que el grafo base ya contenga todos
                                    // los aeropuertos posibles. Por ahora, solo lo ignoramos si no está.
                                    System.err.println("Advertencia: Aeropuerto intermedio " + pathAirport.getName() + " no encontrado en el grafo base para resaltar.");
                                }

                                alertMessage.append(pathAirport.getName()).append(" (").append(pathAirport.getCode()).append(")");
                                if (j < shortestPathCodes.size() - 1) {
                                    alertMessage.append(" -> ");
                                    // Resaltar la arista
                                    Airport segmentOrigin = airportService.getAirportByCode(shortestPathCodes.get(j));
                                    Airport segmentDest = airportService.getAirportByCode(shortestPathCodes.get(j+1));

                                    String edgeKey = segmentOrigin.getCode() + "->" + segmentDest.getCode();
                                    Line edgeLine = edgeMap.get(edgeKey);
                                    if (edgeLine != null) {
                                        edgeLine.setStroke(Color.RED);
                                        edgeLine.setStrokeWidth(3);
                                    } else {
                                        // Esto es importante: si la ruta no existe en el grafo principal, no se puede resaltar.
                                        // Deberías asegurarte de que drawAllRoutesGraph() incluya todas las rutas relevantes.
                                        System.err.println("Advertencia: Ruta " + edgeKey + " no encontrada en el grafo base para resaltar.");
                                    }
                                }
                            }
                        }
                        alertMessage.append("\n\n");
                    } else {
                        alertMessage.append("No se encontró ruta de ").append(primaryOrigin.getName())
                                .append(" a ").append(currentDestination.getName()).append(".\n\n");
                    }
                }
            } else if (topAirports.size() == 1) {
                alertMessage.append("Solo se encontró un aeropuerto principal: ").append(topAirports.get(0).getName()).append(".\nNo hay suficientes aeropuertos para calcular rutas más cortas entre ellos.");
                // Si solo hay uno, ya está resaltado arriba.
                foundAnyShortestRoute = true;
            } else {
                alertMessage.append("No hay suficientes aeropuertos principales para calcular rutas.");
            }

            if (!foundAnyShortestRoute && topAirports.size() < 2) {
                showAlert(Alert.AlertType.INFORMATION, "Aeropuertos Principales", alertMessage.toString());
            } else if (!foundAnyShortestRoute) {
                showAlert(Alert.AlertType.INFORMATION, "Rutas de Aeropuertos Principales", alertMessage.toString() + "\nNo se encontraron rutas más cortas entre los aeropuertos más activos.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Rutas de Aeropuertos Principales", alertMessage.toString());
            }

            // No necesitamos renderGraphOnPane aquí, ya que estamos modificando los elementos existentes.

        } catch (Exception e) {
            System.err.println("Error al mostrar las rutas de los aeropuertos principales: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error al intentar mostrar las rutas: " + e.getMessage());
        }
    }

    /**
     * Restaura el color y grosor de todas las aristas y vértices a su estado original (no resaltado).
     */
    private void resetGraphHighlight() {
        // Restaurar colores y anchos de línea de las aristas
        for (Map.Entry<String, Line> entry : edgeMap.entrySet()) {
            String edgeKey = entry.getKey();
            Line line = entry.getValue();
            if (originalEdgeColors.containsKey(edgeKey)) {
                line.setStroke(originalEdgeColors.get(edgeKey));
            }
            if (originalEdgeWidths.containsKey(edgeKey)) {
                line.setStrokeWidth(originalEdgeWidths.get(edgeKey));
            }
            // También restaurar el color de las flechas asociadas si es posible
            // Esto podría ser más complejo si las flechas no tienen un ID único o no se mapean fácilmente.
            // Por simplicidad, podríamos redibujar las flechas si fuera necesario, o ignorar su reset.
            // Para un control más fino, podrías guardar referencias a los polígonos de las flechas también.
        }

        // Restaurar colores y bordes de los círculos de los vértices
        for (Map.Entry<Object, Circle> entry : vertexCircles.entrySet()) {
            Object airport = entry.getKey();
            Circle circle = entry.getValue();
            if (originalVertexColors.containsKey(airport)) {
                circle.setFill(originalVertexColors.get(airport));
            }
            if (originalVertexStrokeColors.containsKey(airport)) {
                circle.setStroke(originalVertexStrokeColors.get(airport));
            }
            circle.setStrokeWidth(2); // Asegurar que el ancho del borde vuelve a la normalidad
        }
    }

    // Dibuja una arista dirigida entre dos vértices con un peso
    private void drawDirectedEdge(Airport sourceAirport, Airport destAirport, Object weight, Map<Object, double[]> vertexPositions) {
        double[] sourcePos = vertexPositions.get(sourceAirport);
        double[] destPos = vertexPositions.get(destAirport);

        if (sourcePos == null || destPos == null) {
            System.err.println("Error: No se encontraron posiciones para el aeropuerto de origen (" + sourceAirport.getCode() + ") o destino (" + destAirport.getCode() + ").");
            return;
        }

        if (sourceAirport.equals(destAirport)) {
            drawSelfLoop(sourcePos[0], sourcePos[1], vertexCircles.get(sourceAirport).getRadius(), sourceAirport, weight);
            return;
        }

        double angle = Math.atan2(destPos[1] - sourcePos[1], destPos[0] - sourcePos[0]);

        double sourceNodeRadius = vertexCircles.get(sourceAirport) != null ? vertexCircles.get(sourceAirport).getRadius() : 15;
        double destNodeRadius = vertexCircles.get(destAirport) != null ? vertexCircles.get(destAirport).getRadius() : 15;

        double adjustedStartX = sourcePos[0] + sourceNodeRadius * Math.cos(angle);
        double adjustedStartY = sourcePos[1] + sourceNodeRadius * Math.sin(angle);
        double adjustedEndX = destPos[0] - destNodeRadius * Math.cos(angle);
        double adjustedEndY = destPos[1] - destNodeRadius * Math.sin(angle);

        Line line = new Line(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY);
        line.setStroke(Color.LIGHTBLUE);
        line.setStrokeWidth(1.5);

        String edgeKey = sourceAirport.getCode() + "->" + destAirport.getCode();
        // **NUEVO**: Guardar el estado original de la línea
        originalEdgeColors.put(edgeKey, (Color) line.getStroke());
        originalEdgeWidths.put(edgeKey, line.getStrokeWidth());


        if (edgeMap.containsKey(edgeKey)) {
            return; // Evita redibujar aristas si ya existen
        }
        edgeMap.put(edgeKey, line);

        line.setOnMouseEntered(e -> {
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);
        });

        line.setOnMouseExited(e -> {
            // Usa el color original o un color por defecto si no está en el mapa
            Color originalColor = originalEdgeColors.getOrDefault(edgeKey, Color.LIGHTBLUE);
            double originalWidth = originalEdgeWidths.getOrDefault(edgeKey, 1.5);
            line.setStroke(originalColor);
            line.setStrokeWidth(originalWidth);
        });

        graph.getChildren().add(line);
        addArrow(graph, adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY, (Color) line.getStroke());

        if (weight instanceof Double && (Double)weight != 0.0) {
            Text weightText = new Text(String.format("%.1f km", (Double)weight));
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            double midX = (adjustedStartX + adjustedEndX) / 2;
            double midY = (adjustedStartY + adjustedEndY) / 2;
            double offset = 15;
            double angleLine = Math.atan2(adjustedEndY - adjustedStartY, adjustedEndX - adjustedStartX);

            weightText.setLayoutX(midX + offset * Math.cos(angleLine + Math.PI / 2) - weightText.getBoundsInLocal().getWidth() / 2);
            weightText.setLayoutY(midY + offset * Math.sin(angleLine + Math.PI / 2) + weightText.getBoundsInLocal().getHeight() / 4);

            graph.getChildren().add(weightText);
            edgeWeightLabels.put(edgeKey, weightText);
        }
    }

    // Dibuja un bucle (self-loop) para aristas que conectan un vértice consigo mismo
    private void drawSelfLoop(double x, double y, double vertexRadius, Airport vertexAirport, Object weight) {
        double loopRadius = 25;
        double startAngle = 45;

        double arcCenterX = x + vertexRadius * Math.cos(Math.toRadians(startAngle + 90));
        double arcCenterY = y + vertexRadius * Math.sin(Math.toRadians(startAngle + 90));

        Arc arc = new Arc(arcCenterX, arcCenterY, loopRadius, loopRadius, startAngle, 270);
        arc.setType(ArcType.OPEN);
        arc.setStroke(Color.LIGHTBLUE);
        arc.setStrokeWidth(1.5);
        arc.setFill(Color.TRANSPARENT);

        String edgeKey = vertexAirport.getCode() + "->" + vertexAirport.getCode();
        // **NUEVO**: Guardar el estado original del arco
        originalEdgeColors.put(edgeKey, (Color) arc.getStroke());
        originalEdgeWidths.put(edgeKey, arc.getStrokeWidth());

        arc.setOnMouseEntered(e -> {
            arc.setStroke(Color.RED);
            arc.setStrokeWidth(3);
        });

        arc.setOnMouseExited(e -> {
            // Usa el color original o un color por defecto si no está en el mapa
            Color originalColor = originalEdgeColors.getOrDefault(edgeKey, Color.LIGHTBLUE);
            double originalWidth = originalEdgeWidths.getOrDefault(edgeKey, 1.5);
            arc.setStroke(originalColor);
            arc.setStrokeWidth(originalWidth);
        });

        graph.getChildren().add(arc);

        double endAngleRad = Math.toRadians(startAngle + arc.getLength());
        double arrowX = arcCenterX + loopRadius * Math.cos(endAngleRad);
        double arrowY = arcCenterY + loopRadius * Math.sin(endAngleRad);

        double arrowStartAngleRad = Math.toRadians(startAngle + arc.getLength() - 10);
        double arrowStartX = arcCenterX + loopRadius * Math.cos(arrowStartAngleRad);
        double arrowStartY = arcCenterY + loopRadius * Math.sin(arrowStartAngleRad);

        addArrow(graph, arrowStartX, arrowStartY, arrowX, arrowY, (Color) arc.getStroke());

        if (weight instanceof Double && (Double)weight != 0.0) {
            Text weightText = new Text(String.valueOf(String.format("%.1f km", (Double)weight)));
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            weightText.setLayoutX(arcCenterX + loopRadius + 5);
            weightText.setLayoutY(arcCenterY - loopRadius - 5);

            graph.getChildren().add(weightText);
            edgeWeightLabels.put(edgeKey, weightText);
        }
    }

    private void addArrow(Pane pane, double startX, double startY, double endX, double endY, Color color) {
        double arrowLength = 10;
        double arrowWidth = 13;

        double angle = Math.atan2(endY - startY, endX - startX);

        double x1 = endX - arrowLength * Math.cos(angle - Math.toRadians(arrowWidth));
        double y1 = endY - arrowLength * Math.sin(angle - Math.toRadians(arrowWidth));
        double x2 = endX - arrowLength * Math.cos(angle + Math.toRadians(arrowWidth));
        double y2 = endY - arrowLength * Math.sin(angle + Math.toRadians(arrowWidth));

        Polygon arrowHead = new Polygon(
                endX, endY,
                x1, y1,
                x2, y2
        );
        arrowHead.setFill(color);
        pane.getChildren().add(arrowHead);
    }

    private Object[] getGraphVertices(DirectedSinglyLinkedListGraph targetGraph) throws GraphException, ListException {
        try {
            SinglyLinkedList sll = targetGraph.getVertexList();
            Object[] vertices = new Object[sll.size()];
            for (int i = 1; i <= sll.size(); i++) {
                Node node = sll.getNode(i);
                if (node != null && node.getData() instanceof Vertex) {
                    Vertex v = (Vertex) node.getData();
                    if (v.data instanceof Airport) {
                        vertices[i - 1] = v.data;
                    } else {
                        System.err.println("Advertencia: El dato del vértice no es un Airport. Tipo: " + (v.data != null ? v.data.getClass().getName() : "null"));
                    }
                } else {
                    System.err.println("Advertencia: Objeto no válido o nulo encontrado en SinglyLinkedList del grafo en la posición " + i);
                }
            }
            return vertices;
        } catch (ListException e) {
            throw new GraphException("Error al recuperar vértices del grafo: " + e.getMessage());
        }
    }

    private void setupMouseZoom() {
        graph.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY() < 0 ? 1 / 1.1 : 1.1;
            double newScale = scaleTransform.getX() * zoomFactor;
            if (newScale < 0.2 || newScale > 5) return;
            scaleTransform.setX(newScale);
            scaleTransform.setY(newScale);
            scaleTransform.setPivotX(event.getX());
            scaleTransform.setPivotY(event.getY());
            event.consume();
        });
        image.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY() < 0 ? 1 / 1.1 : 1.1;
            double newScale = scaleTransform.getX() * zoomFactor;
            if (newScale < 0.2 || newScale > 5) return;
            scaleTransform.setX(newScale);
            scaleTransform.setY(newScale);
            scaleTransform.setPivotX(event.getX());
            scaleTransform.setPivotY(event.getY());
            event.consume();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}