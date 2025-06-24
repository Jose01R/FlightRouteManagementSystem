package controller.login;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controller.RoutesBetweenAirports;
import controller.Simulation;
import controller.ticketscontroller.TicketsController;
import data.RouteData;
import domain.common.Airport;
import domain.common.Flight;
import domain.common.Passenger;
import domain.common.Route;
import domain.graph.DirectedSinglyLinkedListGraph;
import domain.graph.GraphException;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
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
import java.util.Comparator;
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
        this.txtMessage.setText("Name User: " + name + "\n Role: " + rol);
        this.bp.setCenter(ap);
    }

    @FXML
    public void reportesOnAction(ActionEvent actionEvent) throws DocumentException, IOException, ListException, GraphException {
        this.airNetworkService = new AirNetworkService(airportService, new RouteData());

        List<Airport> topAirports = airNetworkService.getTop5AirportsByRouteCount(); // Obtenemos los 5 aeropuertos con más rutas

        String fileName = "C:\\Users\\PC\\Documents\\INTELLIJ_IDEA_COMMUNITY\\PROYECTO\\FlightRouteManagementSystem\\pdf\\Estadistica.pdf";

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // Título de la sección
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Top 5 airports with the most outgoing flights", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE); // Espacio

        // Crear tabla con 4 columnas
        PdfPTable table1 = new PdfPTable(4);
        table1.setWidthPercentage(100); // Ancho de la tabla
        table1.setSpacingBefore(10f); // Espacio antes de la tabla
        table1.setSpacingAfter(10f); // Espacio después de la tabla
        table1.addCell("Code");
        table1.addCell("Name");
        table1.addCell("Country");
        table1.addCell("Status");

        // Iterar sobre la lista de aeropuertos
        System.out.println("Top 5 airports with the most outgoing flights");
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

        // --- Rutas más utilizadas ---
        doc.add(new Paragraph(" ")); // Add some space
        title = new Paragraph("Most used routes", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table2 = new PdfPTable(4);
        table2.setWidthPercentage(100);
        table2.setSpacingBefore(10f);
        table2.setSpacingAfter(10f);
        table2.addCell("Origin Route");
        table2.addCell("ID Route");
        table2.addCell("Destination Route");
        table2.addCell("Distance");

        ObservableList<Flight> allFlights = flightService.getObservableFlights();
        DirectedSinglyLinkedListGraph topRoutesGraph = new DirectedSinglyLinkedListGraph();
        System.out.println("The number of routes is: " + allFlights.size());

        // We already have topAirports from the previous section.
        for (Airport airport : topAirports) {
            topRoutesGraph.addVertex(airport.getCode());
        }

        for (Airport airport : topAirports) {
            List<Route> routesFromThisAirport = airNetworkService.getAllRoutes().stream()
                    .filter(route -> route.getOriginAirportCode() == airport.getCode())
                    .collect(Collectors.toList());

            if (!routesFromThisAirport.isEmpty()) {
                for (Route route : routesFromThisAirport) {
                    if (!topRoutesGraph.containsVertex(route.getDestinationAirportCode())) {
                        topRoutesGraph.addVertex(route.getDestinationAirportCode());
                    }
                    topRoutesGraph.addEdgeWeight(route.getOriginAirportCode(), route.getDestinationAirportCode(), route.getDistanceKm());

                    Airport destinationAirport = airportService.getAirportByCode(route.getDestinationAirportCode());

                    table2.addCell(airport.getName() + " (" + airport.getCode() + ")");
                    table2.addCell(route.getRouteId());
                    table2.addCell(destinationAirport.getName() + " (" + destinationAirport.getCode() + ")");
                    table2.addCell(String.valueOf(route.getDistanceKm()) + " km");
                }
            }
        }
        doc.add(table2);

        // --- Pasajeros con más vuelos realizados ---


        // --- Porcentaje de ocupación promedio por vuelo ---
        doc.add(new Paragraph(" ")); // Add some space
        title = new Paragraph("Average occupancy percentage per flight", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(Chunk.NEWLINE);

        PdfPTable table4 = new PdfPTable(4); // Flight Number, Capacity, Occupancy, Percentage
        table4.setWidthPercentage(100);
        table4.setSpacingBefore(10f);
        table4.setSpacingAfter(10f);
        table4.addCell("Flight Number");
        table4.addCell("Capacity");
        table4.addCell("Occupancy");
        table4.addCell("Occupancy %");

        double totalOccupancyPercentage = 0;
        int flightCountForAverage = 0;

        List<Flight> allSystemFlights = flightService.getObservableFlights();

        if (allSystemFlights.isEmpty()) {
            table4.addCell(new Phrase("N/A"));
            table4.addCell(new Phrase("N/A"));
            table4.addCell(new Phrase("N/A"));
            table4.addCell(new Phrase("No flights to calculate occupancy"));
        } else {
            for (Flight flight : allSystemFlights) {
                double occupancyPercentage = 0;
                if (flight.getCapacity() > 0) {
                    occupancyPercentage = ((double) flight.getOccupancy() / flight.getCapacity()) * 100;
                }

                table4.addCell(String.valueOf(flight.getNumber()));
                table4.addCell(String.valueOf(flight.getCapacity()));
                table4.addCell(String.valueOf(flight.getOccupancy()));
                table4.addCell(String.format("%.2f%%", occupancyPercentage)); //Formato a 2 decimales

                totalOccupancyPercentage += occupancyPercentage;
                flightCountForAverage++;
            }
        }
        doc.add(table4);

        //Añadir ocupación media general
        doc.add(new Paragraph(" ")); // Agregamos espaciado
        Paragraph averageOccupancyPara;
        if (flightCountForAverage > 0) {
            double overallAverage = totalOccupancyPercentage / flightCountForAverage;
            averageOccupancyPara = new Paragraph(
                    "Average Occupancy Across All Flights: " + String.format("%.2f%%", overallAverage),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
            );
        } else {
            averageOccupancyPara = new Paragraph(
                    "Average Occupancy Across All Flights: N/A (No flights available)",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
            );
        }
        averageOccupancyPara.setAlignment(Element.ALIGN_CENTER);
        doc.add(averageOccupancyPara);

        doc.close();

        util.FXUtility.alertInfo("SUCCESS", "PDF generated succesfully in your directory.");
        System.out.println("PDF generado: " + new java.io.File(fileName).getAbsolutePath());
    }

    private String getAirportNameByCode(int code) {
        return airportCodeToName.getOrDefault(code, "Unknown");
    }

    @FXML
    public void simulaciónVuelosOcupaciónOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/simulation.fxml"));
            Parent root = loader.load();

            //Obtener el controlador real del FXML
            Simulation simulation = loader.getController();

            //Pasar los servicios al controlador real
            simulation.setServices(this.passengerService, this.flightService, this.airplaneService, this.airNetworkService,this.airportService);

            // Mostrar la pantalla en el centro del BorderPane
            this.bp.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
        }

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
            stage.setTitle("Route Management between Airports");
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