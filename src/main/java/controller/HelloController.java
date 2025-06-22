package controller;

import controller.flightcontroller.FlightController;
import controller.ticketscontroller.TicketsController;
import domain.service.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ucr.flightroutemanagementsystem.HelloApplication;

import java.io.IOException;

public class HelloController {

    @FXML
    private BorderPane bp;
    @FXML
    private Text txtMessage;
    @FXML
    private AnchorPane ap;

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
    void Exit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void Home(ActionEvent event) {
        this.txtMessage.setText("Airport Operations System");
        this.bp.setCenter(ap);
    }


    @FXML
    public void simulaciónVuelosOcupaciónOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
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
            Scene scene = new Scene(root, 1410, 900);
            Stage stage = new Stage();
            stage.setTitle("Gestión de Vuelos");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void rutasEntreAeropuertosOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }
}