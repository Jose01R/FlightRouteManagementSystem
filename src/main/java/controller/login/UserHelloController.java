package controller.login;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controller.RoutesBetweenAirports;
import controller.ticketscontroller.TicketsController;
import data.RouteData;
import domain.common.Airport;
import domain.common.Flight;
import domain.common.Route;
import domain.graph.DirectedSinglyLinkedListGraph;
import domain.graph.GraphException;
import domain.linkedlist.ListException;
import domain.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ucr.flightroutemanagementsystem.HelloApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHelloController {

    @FXML
    private BorderPane bp;
    @FXML
    private Text txtMessage;
    @FXML
    private AnchorPane ap;

    private String name;
    private String rol;

    private PassengerService passengerService;
    private FlightService flightService;
    private AirplaneService airplaneService;
    private AirNetworkService airNetworkService;
    private AirportService airportService;

    private Map<Integer, String> airportCodeToName = new HashMap<>();

    public void recibirDatos(String name, String rol) {//recibe el nombre del usuario y su rol
        this.name = name;
        this.rol = rol;
        System.out.println("Nombre recibido: " + name);
        System.out.println("Rol recibido: " + rol);
    }

    private void loadPage(String page) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(page));
        try {
            this.bp.setCenter(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService, AirportService airportService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.airplaneService = airplaneService;
        this.airNetworkService = airNetworkService;
        this.airportService = airportService;

        airportCodeToName = new HashMap<>();
        try {
            for (Object obj : airportService.getAirportsByStatus("Active")) {
                if (obj instanceof Airport airport) {
                    airportCodeToName.put(airport.getCode(), airport.getName());
                }
            }
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void Exit(ActionEvent event) throws IOException {
        //System.exit(0);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loginFxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/logininterface/login.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load());

        stage.setTitle("Airport Operations System - Login");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    void Home(ActionEvent event) {
        this.txtMessage.setText("Name User: " + name + "\n Rol: " + rol);
        this.bp.setCenter(ap);
    }

    @FXML
    public void reportesOnAction(ActionEvent actionEvent) throws DocumentException, IOException, IOException, ListException, GraphException {
//        List<Passenger> passengerList = this.passengerService.getAllPassengers();
//        CircularDoublyLinkedList circularDoublyLinkedList = this.flightService.getFlightList();
//        List<Route> routeList = this.airNetworkService.getAllRoutes();
//        DoublyLinkedList doublyLinkedList = this.airportService.getAllAirports();

        this.airNetworkService = new AirNetworkService(airportService, new RouteData());

        List<Airport> topAirports = airNetworkService.getTop5AirportsByRouteCount(); // Obtenemos los 5 aeropuertos con más rutas

        // Crear documento y archivo PDF en carpeta data
        String fileName = "C:\\Repositorios\\Proyecto-Algoritmos y Estruc de Datos\\FlightRouteManagementSystem\\pdf\\Estadistica.pdf";

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Top 5 aeropuertos con más vuelos salientes", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table1 = new PdfPTable(4);
        table1.addCell("Code");
        table1.addCell("Name");
        table1.addCell("Country");
        table1.addCell("Status");

        // Iterar sobre la lista de aeropuertos
        System.out.println("Top 5 aeropuertos con más vuelos salientes");
        for (int i = 0; i < topAirports.size(); i++) {
            int id = topAirports.get(i).getCode();
            String name = topAirports.get(i).getName();
            String country = topAirports.get(i).getCountry();
            String status = topAirports.get(i).getStatus();

            table1.addCell(String.valueOf(id));
            table1.addCell(name);
            table1.addCell(country);
            table1.addCell(status);
        }
        doc.add(table1);


        title = new Paragraph("Rutas más utilizadas", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table2 = new PdfPTable(4);
        table2.addCell("Ruta de Origen");
        table2.addCell("ID Ruta");
        table2.addCell("Ruta de Destino");
        table2.addCell("Ruta de Distancia");

        // Iterar sobre la lista de rutas más usadas
        ObservableList<Flight> allFlights = flightService.getObservableFlights();
        //tomo los 5 aeropuertos top
        DirectedSinglyLinkedListGraph topRoutesGraph = new DirectedSinglyLinkedListGraph();
        System.out.println("La cantidad de rutas es de : "+ allFlights.size());
        for (Airport airport : topAirports) {
            topRoutesGraph.addVertex(airport.getCode());
        }

//        for (Flight f : allFlights) {
            // Luego, iteramos sobre los aeropuertos principales para encontrar y añadir sus rutas al grafo
            for (Airport airport : topAirports) {
                List<Route> routesFromThisAirport = airNetworkService.getAllRoutes().stream()
                        .filter(route -> route.getOriginAirportCode() == airport.getCode())
                        .collect(Collectors.toList());

                if (!routesFromThisAirport.isEmpty()) {
                    for (Route route : routesFromThisAirport) {
                        // Aseguramos que el aeropuerto de destino también esté en el grafo, incluso si no es uno de los "top"
                        if (!topRoutesGraph.containsVertex(route.getDestinationAirportCode())) {
                            topRoutesGraph.addVertex(route.getDestinationAirportCode());
                        }
                        topRoutesGraph.addEdgeWeight(route.getOriginAirportCode(), route.getDestinationAirportCode(), route.getDistanceKm());

                        Airport destinationAirport = airportService.getAirportByCode(route.getDestinationAirportCode());

                        table2.addCell(airport.getName() +" ("+airport.getCode()+")");
                        table2.addCell(route.getRouteId());
                        table2.addCell(destinationAirport.getName()+" ("+destinationAirport.getCode()+")");
                        table2.addCell(String.valueOf(route.getDistanceKm())+" km");
                    }
                }
            }

        //}

        doc.add(table2);

        title = new Paragraph("Pasajeros con más vuelos realizados", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table3 = new PdfPTable(3);
        table3.addCell("ID");
        table3.addCell("Name");
        table3.addCell("Nationality");
        //iterar en la lista y meter la tabla en el doc




        title = new Paragraph("Porcentaje de ocupación promedio por vuelo", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table4 = new PdfPTable(3);
        table4.addCell("ID");
        table4.addCell("Name");
        table4.addCell("Nationality");
        //iterar en la lista y meter la tabla en el doc

        doc.close();

        System.out.println("PDF generado: " + new java.io.File(fileName).getAbsolutePath());
    }

    private String getAirportNameByCode(int code) {
        return airportCodeToName.getOrDefault(code, "Desconocido");
    }

    @FXML
    public void simulaciónVuelosOcupaciónOnAction(ActionEvent actionEvent) {
        loadPage("simulation.fxml");
    }

    @FXML
    public void ticketsOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/ticketsinterface/tickets.fxml"));
            Parent root = loader.load();

            // Obtener el controlador real del FXML
            TicketsController ticketsController = loader.getController();

            // Pasar los servicios al controlador real
            ticketsController.setServices(this.passengerService, this.flightService, this.airplaneService, this.airNetworkService,this.airportService);

            // Mostrar la pantalla en el centro del BorderPane
            this.bp.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @FXML
//    public void aeropuertosOnAction(ActionEvent actionEvent) {
//        loadPage("airports.fxml");
//    }

//    @FXML
//    public void vuelosOnAction(ActionEvent actionEvent) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/flightinterface/flight.fxml"));
//            Parent root = loader.load();
//
//            // Obtener el controlador del archivo flight.fxml
//            FlightController flightController = loader.getController();
//
//            // Pasar los servicios
//            flightController.setServices(this.passengerService, this.flightService,this.airplaneService,this.airNetworkService,this.airportService);
//
//            // Crear y mostrar la nueva ventana
//            Scene scene = new Scene(root, 1410, 900);
//            Stage stage = new Stage();
//            stage.setTitle("Gestión de Vuelos");
//            stage.setScene(scene);
//            stage.show();
//
//            // Hacer que la ventana sea movible
//            final Delta dragDelta = new Delta();
//            root.setOnMousePressed(event -> {
//                dragDelta.x = event.getSceneX();
//                dragDelta.y = event.getSceneY();
//            });
//            root.setOnMouseDragged(event -> {
//                stage.setX(event.getScreenX() - dragDelta.x);
//                stage.setY(event.getScreenY() - dragDelta.y);
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    @FXML
    public void rutasEntreAeropuertosOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/routes_Between_Airports.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo flight.fxml
            RoutesBetweenAirports routesBetweenAirports = loader.getController();

            // Pasar los servicios
            routesBetweenAirports.setServices(this.passengerService, this.flightService,this.airplaneService,this.airNetworkService,this.airportService);

            // Crear y mostrar la nueva ventana
            Scene scene = new Scene(root, 1230, 700); //1410
            Stage stage = new Stage();
            stage.setTitle("Gestión de Rutas entre Aeropuertos");
            stage.setScene(scene);
            stage.show();

            // Hacer que la ventana sea movible
            final Delta dragDelta = new Delta();
            root.setOnMousePressed(event -> {
                dragDelta.x = event.getSceneX();
                dragDelta.y = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - dragDelta.x);
                stage.setY(event.getScreenY() - dragDelta.y);
            });

            routesBetweenAirports.close(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //loadPage("routes_Between_Airports.fxml");
    }

    private static class Delta {
        double x, y;
    }
}