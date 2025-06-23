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

import data.PassengerData;
import data.FlightData;
import data.RouteData;

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

    @FXML
    public void initialize() {
        setupMouseZoom(); // Configuramos el zoom con la rueda del ratón
        vertexCircles = new HashMap<>(); // Inicializamos el mapa de círculos para vértices
        graph.getTransforms().add(scaleTransform); // Añadimos la transformación de escala al panel del grafo
        image.getTransforms().add(scaleTransform); // Añadimos la transformación de escala al imagen del grafo

        listGraph = new DirectedSinglyLinkedListGraph(); // Creamos una nueva instancia de nuestro grafo

    }
    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService, AirportService airportService) {
        this.airNetworkService=airNetworkService;
        this.flightService=flightService;
        this.passengerService=passengerService;
        this.airplaneService=airplaneService;
        this.airportService=airportService;
        initializeData();
    }
    private void initializeData() {
        try {

            // Generamos datos iniciales si detectamos que las listas están vacías
            // Esto es útil para pruebas o la primera ejecución de la aplicación
            if (airportService.getAllAirports().isEmpty()) {
                System.out.println("Generando aeropuertos iniciales...");
                airportService.generateInitialRandomAirports(20); // Generamos 20 aeropuertos aleatorios
            }
            if (airplaneService.getAllAirplanes().isEmpty()) {
                System.out.println("Generando aviones iniciales...");
                airplaneService.generateInitialRandomAirplanes(10); // Generamos 10 aviones aleatorios
            }
            if (airNetworkService.getAllRoutes().isEmpty()) {
                System.out.println("Generando rutas iniciales...");
                airNetworkService.generateInitialRandomRoutes(30); // Generamos 30 rutas aleatorias
            }
            if (passengerService.getAllPassengers().isEmpty()) {
                System.out.println("Generando pasajeros iniciales...");
                passengerService.generateInitialRandomPassengers(50); // Generamos 50 pasajeros aleatorios
            }
            if (flightService.getFlightList().isEmpty()) {
                System.out.println("Generando vuelos iniciales...");
                flightService.generateFlightsRandom(25); // Generamos 25 vuelos aleatorios
            }

            // Al iniciar la aplicación, dibujamos el grafo con todas las rutas existentes
            drawAllRoutesGraph();

        } catch (Exception e) {
            // Capturamos cualquier error durante la inicialización y mostramos una alerta
            System.err.println("Error durante la inicialización del controlador: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Inicialización", "No se pudieron cargar o generar los datos iniciales: " + e.getMessage());
        }
    }
    /**
     * Dibuja el grafo con todos los aeropuertos y rutas cargados desde los servicios.
     */
    private void drawAllRoutesGraph() {
        clearGraph(); // Limpiamos el grafo visual y los datos del grafo lógico antes de dibujar uno nuevo
        try {
            DoublyLinkedList allAirportsList = airportService.getAllAirports(); // Obtenemos la lista de todos los aeropuertos
            List<Route> allRoutes = airNetworkService.getAllRoutes(); // Obtenemos la lista de todas las rutas

            // Verificamos si hay suficientes datos para dibujar
            if (allAirportsList.isEmpty() || allRoutes.isEmpty()) {
                System.out.println("No hay suficientes aeropuertos o rutas para dibujar el grafo.");
                return;
            }

            // Recorremos la lista de aeropuertos para añadirlos como vértices a nuestro grafo de dibujo
            for (int i = 1; i <= allAirportsList.size(); i++) {
                Node node = allAirportsList.getNode(i);
                if (node != null && node.data instanceof Airport) {
                    Airport airport = (Airport) node.data;
                    listGraph.addVertex(airport.getCode()); // Añadimos el código del aeropuerto como vértice
                } else {
                    System.err.println("Advertencia: Objeto no válido o nulo encontrado en allAirportsList en la posición " + i);
                }
            }

            // Recorremos todas las rutas para añadirlas como aristas a nuestro grafo de dibujo
            for (Route route : allRoutes) {
                // Verificamos que ambos vértices (origen y destino) existan en el grafo antes de añadir la arista
                if (listGraph.containsVertex(route.getOriginAirportCode()) &&
                        listGraph.containsVertex(route.getDestinationAirportCode())) {
                    try {
                        // Añadimos la arista con el peso (distancia en km)
                        listGraph.addEdgeWeight(route.getOriginAirportCode(), route.getDestinationAirportCode(), route.getDistanceKm());
                    } catch (GraphException e) {
                        System.err.println("Error al agregar la arista para la ruta " + route.getRouteId() + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Advertencia: Se omite la ruta " + route.getRouteId() + " porque falta el vértice del aeropuerto de origen o destino.");
                }
            }

            // Finalmente, renderizamos el grafo en el panel de la interfaz gráfica
            renderGraphOnPane(listGraph, airportService);

        } catch (ListException | GraphException e) {
            System.err.println("Error al dibujar todas las rutas en el grafo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearGraph() {
        graph.getChildren().clear(); // Limpiamos todos los elementos visuales del panel del grafo
        vertexCircles.clear(); // Limpiamos los mapas que guardan referencias a los elementos visuales
        edgeMap.clear();
        edgeWeightLabels.clear();
        listGraph.clear(); // Limpiamos la estructura de datos del grafo lógico
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
        for (Object vertexCode : currentVertices) {
            int airportCode = (Integer) vertexCode;
            Airport airport = airportService.getAirportByCode(airportCode); // Obtenemos el objeto aeropuerto por su código

            double x, y;
            boolean positionFound = false;

            // Primero, intentamos asignar posiciones fijas para aeropuertos específicos para un layout más organizado
            if (airport != null) {
                switch (airport.getName()) {
                    case "Aeropuerto de Dortmund": x = 600; y = 170; positionFound = true; break;
                    case "Aeropuerto de Aalborg": x = 880; y = 185; positionFound = true; break;
                    case "Aeropuerto de La Coruña": x = 600; y = 210; positionFound = true; break;
                    case "Aeropuerto de Marsella-Provenza": x = 820; y = 295; positionFound = true; break;
                    case "Aeropuerto de Oporto": x = 755; y = 325; positionFound = true; break;
                    case "Aeropuerto de Zúrich": x = 870; y = 280; positionFound = true; break;
                    case "Aeropuerto de Milán-Malpensa": x = 890; y = 300; positionFound = true; break;
                    case "Aeropuerto de Bruselas": x = 840; y = 240; positionFound = true; break;
                    case "Aeropuerto de Estocolmo-Arlanda": x = 940; y = 145; positionFound = true; break;
                    case "Aeropuerto de Viena-Schwechat": x = 910; y = 270; positionFound = true; break;
                    case "Aeropuerto de Praga": x = 890; y = 255; positionFound = true; break;
                    case "Aeropuerto de Ámsterdam-Schiphol": x = 830; y = 225; positionFound = true; break;
                    case "Aeropuerto de Helsinki-Vantaa": x = 975; y = 110; positionFound = true; break;
                    case "Aeropuerto de Dublín": x = 720; y = 230; positionFound = true; break;
                    case "Aeropuerto de Oslo-Gardermoen": x = 930; y = 130; positionFound = true; break;
                    case "Aeropuerto de Bucarest-Henri Coandă": x = 980; y = 300; positionFound = true; break;
                    case "Aeropuerto de Budapest-Ferenc Liszt": x = 955; y = 285; positionFound = true; break;
                    case "Aeropuerto de Varsovia-Chopin": x = 960; y = 240; positionFound = true; break;
                    case "Aeropuerto de Copenhague-Kastrup": x = 910; y = 200; positionFound = true; break;
                    case "Aeropuerto de Sofía": x = 995; y = 320; positionFound = true; break;
                    default:
                        // Si no es un aeropuerto con posición fija, asignamos valores centinela para indicar que necesita posición dinámica
                        x = -1; y = -1;
                }
            } else {
                x = -1; y = -1;
            }

            // Para los aeropuertos que no tienen posición fija, intentamos encontrar una posición aleatoria no superpuesta
            if (!positionFound) {
                double safeRadius = 50; // Definimos un radio seguro para evitar que los círculos se superpongan demasiado
                int attempts = 0; // Contamos los intentos para evitar bucles infinitos
                do {
                    // Generamos coordenadas aleatorias dentro del área definida por el padding
                    x = rand.nextDouble() * (maxX - minX) + minX;
                    y = rand.nextDouble() * (maxY - minY) + minY;
                    // Creamos una "clave de posición" cuantizada para verificar la ocupación en una cuadrícula virtual
                    String posKey = String.format("%.0f,%.0f", x / safeRadius, y / safeRadius);
                    if (!occupiedPositions.contains(posKey)) {
                        occupiedPositions.add(posKey); // Marcamos la posición como ocupada
                        positionFound = true; // Indicamos que hemos encontrado una posición
                    }
                    attempts++;
                } while (!positionFound && attempts < 100); // Limitamos los intentos
                if (!positionFound) {
                    // Si después de muchos intentos no encontramos una posición, mostramos una advertencia
                    System.err.println("Advertencia: No se pudo encontrar una posición no superpuesta para " + vertexCode + " después de varios intentos.");
                }
            }

            vertexPositions.put(vertexCode, new double[]{x, y}); // Guardamos la posición del vértice

            // Dibujamos el círculo que representa el aeropuerto
            Circle circle = new Circle(x, y, 15, Color.WHITE);
            circle.setStroke(Color.LIGHTBLUE);
            circle.setStrokeWidth(2);
            circle.setId("airport_circle_" + airportCode);

            // Creamos el texto con el nombre del aeropuerto
            Text text = new Text(airport != null ? airport.getName() : String.valueOf(airportCode));
            text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            text.setFill(Color.WHITE);
            text.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;"); // Aumentamos ligeramente el tamaño de la fuente

            // Ajustamos la posición del texto para que quede centrado debajo del círculo
            text.setLayoutX(x - text.getBoundsInLocal().getWidth() / 2);
            text.setLayoutY(y + circle.getRadius() + 15); // Añadimos un desplazamiento para que no se superponga con el círculo

            graph.getChildren().addAll(circle, text); // Añadimos el círculo y el texto al panel del grafo
            vertexCircles.put(vertexCode, circle); // Guardamos la referencia al círculo en el mapa
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
                            // Dibujamos la arista dirigida entre el origen y el destino con su peso
                            drawDirectedEdge(sourceVertex.data, edge.getEdge(), edge.getWeight(), vertexPositions);
                        } else {
                            System.err.println("Advertencia: Objeto no válido o nulo encontrado en edgesList en la posición " + j);
                        }
                    }
                }
            } else {
                System.err.println("Advertencia: Objeto no válido o nulo encontrado en vertexListSLL en la posición " + i);
            }
        }
    }

    @FXML
    private void handleShowTopAirportRoutes(ActionEvent event) {
        clearGraph(); // Limpiamos el grafo actual para dibujar el nuevo subgrafo de rutas principales

        // Verificamos que los servicios estén inicializados
        if (airNetworkService == null || airportService == null) {
            showAlert(Alert.AlertType.ERROR, "Error de Servicio", "Los servicios de red aérea o aeropuertos no están inicializados.");
            return;
        }

        try {
            List<Airport> topAirports = airNetworkService.getTop5AirportsByRouteCount(); // Obtenemos los 5 aeropuertos con más rutas

            if (topAirports.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Sin Datos", "No hay aeropuertos con rutas disponibles para mostrar.");
                return;
            }

            // Creamos un nuevo grafo para mostrar solo las rutas de los aeropuertos principales
            DirectedSinglyLinkedListGraph topRoutesGraph = new DirectedSinglyLinkedListGraph();
            StringBuilder alertMessage = new StringBuilder("Rutas de los Aeropuertos Más Activos:\n\n");
            boolean foundRoutes = false;

            // Primero, añadimos todos los aeropuertos principales como vértices al nuevo grafo
            for (Airport airport : topAirports) {
                topRoutesGraph.addVertex(airport.getCode());
            }

            // Luego, iteramos sobre los aeropuertos principales para encontrar y añadir sus rutas al grafo
            for (Airport airport : topAirports) {
                List<Route> routesFromThisAirport = airNetworkService.getAllRoutes().stream()
                        .filter(route -> route.getOriginAirportCode() == airport.getCode())
                        .collect(Collectors.toList());

                if (!routesFromThisAirport.isEmpty()) {
                    alertMessage.append("Aeropuerto: ").append(airport.getName()).append(" (").append(airport.getCode()).append(")\n");
                    for (Route route : routesFromThisAirport) {
                        // Aseguramos que el aeropuerto de destino también esté en el grafo, incluso si no es uno de los "top"
                        if (!topRoutesGraph.containsVertex(route.getDestinationAirportCode())) {
                            topRoutesGraph.addVertex(route.getDestinationAirportCode());
                        }
                        topRoutesGraph.addEdgeWeight(route.getOriginAirportCode(), route.getDestinationAirportCode(), route.getDistanceKm());

                        Airport destinationAirport = airportService.getAirportByCode(route.getDestinationAirportCode());
                        alertMessage.append("  - Ruta ID: ").append(route.getRouteId())
                                .append(", Destino: ").append(destinationAirport != null ? destinationAirport.getName() : "Desconocido")
                                .append(", Distancia: ").append(String.format("%.2f", route.getDistanceKm())).append(" km\n");
                    }
                    alertMessage.append("\n");
                    foundRoutes = true;
                }
            }


            if (!foundRoutes) {
                showAlert(Alert.AlertType.INFORMATION, "Sin Rutas", "Los aeropuertos más activos no tienen rutas registradas.");
            } else {
                // Renderizamos el grafo con las rutas de los aeropuertos principales y mostramos la información en una alerta
                renderGraphOnPane(topRoutesGraph, airportService);
                showAlert(Alert.AlertType.INFORMATION, "Rutas de Aeropuertos Principales", alertMessage.toString());
            }

        } catch (Exception e) {
            System.err.println("Error al mostrar las rutas de los aeropuertos principales: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error al intentar mostrar las rutas: " + e.getMessage());
        }
    }

    // Dibuja una arista dirigida entre dos vértices con un peso
    private void drawDirectedEdge(Object sourceData, Object destData, Object weight, Map<Object, double[]> vertexPositions) {
        double[] sourcePos = vertexPositions.get(sourceData); // Obtenemos la posición del vértice origen
        double[] destPos = vertexPositions.get(destData);     // Obtenemos la posición del vértice destino

        if (sourcePos == null || destPos == null) {
            System.err.println("Error: No se encontraron posiciones para el vértice de origen o destino.");
            return;
        }

        // Si el origen y el destino son el mismo vértice, dibujamos un bucle (self-loop)
        if (sourceData.equals(destData)) {
            drawSelfLoop(sourcePos[0], sourcePos[1], vertexCircles.get(sourceData).getRadius(), sourceData, weight);
            return;
        }

        // Calculamos el ángulo entre los dos vértices para ajustar el inicio y fin de la línea
        double angle = Math.atan2(destPos[1] - sourcePos[1], destPos[0] - sourcePos[0]);

        // Obtenemos el radio real de los círculos de los vértices (o usamos un valor por defecto)
        double sourceNodeRadius = vertexCircles.get(sourceData) != null ? vertexCircles.get(sourceData).getRadius() : 15;
        double destNodeRadius = vertexCircles.get(destData) != null ? vertexCircles.get(destData).getRadius() : 15;

        // Ajustamos los puntos de inicio y fin de la línea para que comiencen/terminen en el borde de los círculos
        double adjustedStartX = sourcePos[0] + sourceNodeRadius * Math.cos(angle);
        double adjustedStartY = sourcePos[1] + sourceNodeRadius * Math.sin(angle);
        double adjustedEndX = destPos[0] - destNodeRadius * Math.cos(angle);
        double adjustedEndY = destPos[1] - destNodeRadius * Math.sin(angle);

        Line line = new Line(adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY);
        line.setStroke(Color.LIGHTBLUE);
        line.setStrokeWidth(1.5);

        String edgeKey = sourceData.toString() + "->" + destData.toString();
        // Evitamos dibujar líneas duplicadas si ya existen en el mapa
        if (edgeMap.containsKey(edgeKey)) {
            return;
        }
        edgeMap.put(edgeKey, line);

        // Añadimos efectos visuales al pasar el ratón sobre la línea
        line.setOnMouseEntered(e -> {
            line.setStroke(Color.RED);
            line.setStrokeWidth(3);
        });

        line.setOnMouseExited(e -> {
            line.setStroke(Color.LIGHTBLUE);
            line.setStrokeWidth(1.5);
        });

        graph.getChildren().add(line); // Añadimos la línea al panel del grafo
        addArrow(graph, adjustedStartX, adjustedStartY, adjustedEndX, adjustedEndY, (Color) line.getStroke()); // Añadimos una flecha para indicar dirección

        // Si hay un peso (distancia) y no es cero, lo dibujamos en la mitad de la línea
        if (weight instanceof Double && (Double)weight != 0.0) {
            double midX = (adjustedStartX + adjustedEndX) / 2;
            double midY = (adjustedStartY + adjustedEndY) / 2;
            Text weightText = new Text(String.format("%.1f km", (Double)weight)); // Formateamos el peso
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            double offset = 15; // Desplazamiento del texto respecto a la línea
            double angleLine = Math.atan2(adjustedEndY - adjustedStartY, adjustedEndX - adjustedStartX);

            // Posicionamos el texto ligeramente desplazado de la línea para evitar superposiciones
            weightText.setLayoutX(midX + offset * Math.cos(angleLine + Math.PI / 2) - weightText.getBoundsInLocal().getWidth() / 2);
            weightText.setLayoutY(midY + offset * Math.sin(angleLine + Math.PI / 2) + weightText.getBoundsInLocal().getHeight() / 4);

            graph.getChildren().add(weightText); // Añadimos el texto al panel
            edgeWeightLabels.put(edgeKey, weightText); // Guardamos la referencia al texto
        }
    }

    // Dibuja un bucle (self-loop) para aristas que conectan un vértice consigo mismo
    private void drawSelfLoop(double x, double y, double vertexRadius, Object vertexData, Object weight) {
        double loopRadius = 25; // Radio del bucle
        double startAngle = 45; // Ángulo de inicio del arco

        // Calculamos el centro del arco ligeramente desplazado del centro del aeropuerto
        double arcCenterX = x + vertexRadius * Math.cos(Math.toRadians(startAngle + 90));
        double arcCenterY = y + vertexRadius * Math.sin(Math.toRadians(startAngle + 90));

        Arc arc = new Arc(arcCenterX, arcCenterY, loopRadius, loopRadius, startAngle, 270);
        arc.setType(ArcType.OPEN);
        arc.setStroke(Color.LIGHTBLUE);
        arc.setStrokeWidth(1.5);
        arc.setFill(Color.TRANSPARENT);

        // Añadimos efectos visuales al pasar el ratón sobre el arco
        arc.setOnMouseEntered(e -> {
            arc.setStroke(Color.RED);
            arc.setStrokeWidth(3);
        });

        arc.setOnMouseExited(e -> {
            arc.setStroke(Color.LIGHTBLUE);
            arc.setStrokeWidth(1.5);
        });

        graph.getChildren().add(arc); // Añadimos el arco al panel

        // Calculamos la posición de la flecha en el extremo del bucle
        double endAngleRad = Math.toRadians(startAngle + arc.getLength());
        double arrowX = arcCenterX + loopRadius * Math.cos(endAngleRad);
        double arrowY = arcCenterY + loopRadius * Math.sin(endAngleRad);

        // Ajustamos el punto de inicio de la flecha para una apariencia más suave
        double arrowStartAngleRad = Math.toRadians(startAngle + arc.getLength() - 10);
        double arrowStartX = arcCenterX + loopRadius * Math.cos(arrowStartAngleRad);
        double arrowStartY = arcCenterY + loopRadius * Math.sin(arrowStartAngleRad);

        addArrow(graph, arrowStartX, arrowStartY, arrowX, arrowY, (Color) arc.getStroke()); // Añadimos la flecha

        // Si hay un peso, lo dibujamos junto al bucle
        if (weight instanceof Double && (Double)weight != 0.0) {
            Text weightText = new Text(String.valueOf(String.format("%.1f km", (Double)weight)));
            weightText.setFill(Color.DARKGREEN);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");

            // Posicionamos el texto para evitar superposiciones con el círculo del aeropuerto
            weightText.setLayoutX(arcCenterX + loopRadius + 5);
            weightText.setLayoutY(arcCenterY - loopRadius - 5);

            graph.getChildren().add(weightText); // Añadimos el texto al panel
            String edgeKey = vertexData.toString() + "->" + vertexData.toString();
            edgeWeightLabels.put(edgeKey, weightText); // Guardamos la referencia al texto
        }
    }

    // Dibuja una flecha al final de una línea para indicar la dirección de la arista
    private void addArrow(Pane pane, double startX, double startY, double endX, double endY, Color color) {
        double arrowLength = 10; // Longitud de la flecha
        double arrowWidth = 13;  // Ancho de la flecha (ángulo)

        double angle = Math.atan2(endY - startY, endX - startX); // Calculamos el ángulo de la línea

        // Calculamos los puntos para la cabeza de la flecha
        double x1 = endX - arrowLength * Math.cos(angle - Math.toRadians(arrowWidth));
        double y1 = endY - arrowLength * Math.sin(angle - Math.toRadians(arrowWidth));
        double x2 = endX - arrowLength * Math.cos(angle + Math.toRadians(arrowWidth));
        double y2 = endY - arrowLength * Math.sin(angle + Math.toRadians(arrowWidth));

        Polygon arrowHead = new Polygon(
                endX, endY, // La punta de la flecha
                x1, y1,     // Un lado
                x2, y2      // El otro lado
        );
        arrowHead.setFill(color); // Rellenamos la flecha con el color de la línea
        pane.getChildren().add(arrowHead); // Añadimos la flecha al panel
    }

    // Recuperamos todos los vértices de un grafo dado
    private Object[] getGraphVertices(DirectedSinglyLinkedListGraph targetGraph) throws GraphException, ListException {
        try {
            SinglyLinkedList sll = targetGraph.getVertexList(); // Obtenemos la lista de vértices
            Object[] vertices = new Object[sll.size()]; // Creamos un arreglo para los vértices
            for (int i = 1; i <= sll.size(); i++) {
                Node node = sll.getNode(i);
                if (node != null && node.getData() instanceof Vertex) {
                    Vertex v = (Vertex) node.getData();
                    vertices[i-1] = v.data; // Almacenamos los datos del vértice
                } else {
                    System.err.println("Advertencia: Objeto no válido o nulo encontrado en SinglyLinkedList del grafo en la posición " + i);
                }
            }
            return vertices;
        } catch (ListException e) {
            throw new GraphException("Error al recuperar vértices del grafo: " + e.getMessage());
        }
    }


    // Configuramos la funcionalidad de zoom con la rueda del ratón
    private void setupMouseZoom() {
        graph.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY() < 0 ? 1 / 1.1 : 1.1; // Calculamos el factor de zoom
            double newScale = scaleTransform.getX() * zoomFactor; // Calculamos la nueva escala
            // Limitamos el zoom para que no sea excesivo
            if (newScale < 0.2 || newScale > 5) return;
            scaleTransform.setX(newScale); // Aplicamos la nueva escala a X
            scaleTransform.setY(newScale); // Aplicamos la nueva escala a Y
            scaleTransform.setPivotX(event.getX()); // Establecemos el punto de pivote para el zoom (donde está el ratón)
            scaleTransform.setPivotY(event.getY());
            event.consume(); // Consumimos el evento para evitar que se propague
        });
        image.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY() < 0 ? 1 / 1.1 : 1.1; // Calculamos el factor de zoom
            double newScale = scaleTransform.getX() * zoomFactor; // Calculamos la nueva escala
            // Limitamos el zoom para que no sea excesivo
            if (newScale < 0.2 || newScale > 5) return;
            scaleTransform.setX(newScale); // Aplicamos la nueva escala a X
            scaleTransform.setY(newScale); // Aplicamos la nueva escala a Y
            scaleTransform.setPivotX(event.getX()); // Establecemos el punto de pivote para el zoom (donde está el ratón)
            scaleTransform.setPivotY(event.getY());
            event.consume(); // Consumimos el evento para evitar que se propague
        });
    }

    // Muestra una alerta con el tipo, título y mensaje especificados
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No mostramos un encabezado adicional
        alert.setContentText(message);
        alert.showAndWait(); // Mostramos la alerta y esperamos a que el usuario la cierre
    }
}