package controller.login;

import controller.RoutesBetweenAirports;
import controller.flightcontroller.FlightController;
import controller.ticketscontroller.TicketsController;
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

import java.io.IOException;

public class AdminHelloController {

    @FXML
    private BorderPane bp;
    @FXML
    private Text txtMessage;
    @FXML
    private AnchorPane ap;

    private String name;
    private String rol;

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
    private PassengerService passengerService;
    private FlightService flightService;
    private AirplaneService airplaneService;
    private AirNetworkService airNetworkService;
    private AirportService airportService;

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
//        this.txtMessage.setText("Airport Operations System");
//        this.bp.setCenter(ap);
        this.txtMessage.setText("Name User: " + name + "\n Rol: " + rol);
        this.bp.setCenter(ap);
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

    @FXML
    public void aeropuertosOnAction(ActionEvent actionEvent) {
        loadPage("airports.fxml");
    }


    @FXML
    public void reportesOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }

    @FXML
    public void vuelosOnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/flightinterface/flight.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo flight.fxml
            FlightController flightController = loader.getController();

            // Pasar los servicios
            flightController.setServices(this.passengerService, this.flightService,this.airplaneService,this.airNetworkService,this.airportService);

            // Crear y mostrar la nueva ventana
            Scene scene = new Scene(root, 1410, 900); //1410
            Stage stage = new Stage();
            stage.setTitle("Gestión de Vuelos");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
    }

    private static class Delta {
        double x, y;
    }
}