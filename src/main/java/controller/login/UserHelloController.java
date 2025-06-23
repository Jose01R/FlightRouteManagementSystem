package controller.login;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controller.RoutesBetweenAirports;
import controller.flightcontroller.FlightController;
import controller.ticketscontroller.TicketsController;
import domain.common.Airport;
import domain.common.Passenger;
import domain.common.Route;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.service.*;
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
import java.time.LocalTime;
import java.util.List;

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
    public void reportesOnAction(ActionEvent actionEvent) throws DocumentException, IOException, IOException, ListException {
        List<Passenger> passengerList = this.passengerService.getAllPassengers();
        CircularDoublyLinkedList circularDoublyLinkedList = this.flightService.getFlightList();
        List<Route> routeList = this.airNetworkService.getAllRoutes();
        DoublyLinkedList doublyLinkedList = this.airportService.getAllAirports();

        // Crear documento y archivo PDF en carpeta data
        String fileName = "C:\\Repositorios\\Proyecto-Algoritmos y Estruc de Datos\\FlightRouteManagementSystem\\Estadistica.pdf";

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(fileName));
        doc.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Aeropuertos", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table = new PdfPTable(4);
        table.addCell("Code");
        table.addCell("Name");
        table.addCell("Country");
        table.addCell("Status");

        // Iterar sobre la lista de aeropuertos

        for (int i = 1; i <= doublyLinkedList.size(); i++) {
            Airport a = (Airport) doublyLinkedList.getNode(i).data;

            table.addCell(String.valueOf(a.getCode()));
            table.addCell(a.getName());
            table.addCell(a.getCountry());
            table.addCell(a.getStatus());
        }

        title = new Paragraph("Rutas más usadas", titleFont);
        doc.add(title);

        // Crear tabla con 4 columnas
        PdfPTable table = new PdfPTable(4);
        table.addCell("Code");
        table.addCell("Name");
        table.addCell("Country");
        table.addCell("Status");

        // Iterar sobre la lista de rutas más usadas

        for (int i = 0; i <= routeList.size(); i++) {
            String id = routeList.get(i).getRouteId();
            String airline = routeList.get(i).getAirline();
            double durationHours = routeList.get(i).getDurationHours();
            double distanceKm =  routeList.get(i).getDistanceKm();
            double price =  routeList.get(i).getPrice();
            LocalTime departureTime = routeList.get(i).getDepartureTime();
            LocalTime arriveTime = routeList.get(i).getArrivalTime();


            table.addCell(id);
            table.addCell(airline);
            table.addCell(String.valueOf(durationHours));
            table.addCell(String.valueOf(distanceKm));
            table.addCell(String.valueOf(price));
            table.addCell(String.valueOf(departureTime));
            table.addCell(String.valueOf(arriveTime));
        }
        
        doc.add(table);
        doc.close();

        System.out.println("PDF generado: " + new java.io.File(fileName).getAbsolutePath());
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