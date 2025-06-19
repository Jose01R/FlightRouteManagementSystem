package controller.ticketscontroller;
import domain.service.AirNetworkService;
import domain.service.FlightService;
import domain.service.PassengerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class TicketsController {

    @FXML
    private ScrollPane flightScrollPane;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private TextField fromTf;

    @FXML
    private RadioButton oneWayRadioButtom;

    @FXML
    private RadioButton roundTripRadioButtom;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextField toTf;

    private FlightService flightService;
    private PassengerService passengerService;
    private AirNetworkService airNetworkService;
    @FXML
    void searchFlightOnAction(ActionEvent event) {
        // Ejemplo de creación dinámica
        VBox vuelosBox = new VBox(10);
        vuelosBox.setStyle("-fx-padding: 10;");

        // Aquí puedes iterar sobre una lista real de vuelos (Flight)
        for (int i = 0; i < 5; i++) {
            BorderPane tarjetaVuelo = new BorderPane();
            tarjetaVuelo.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: lightgray; -fx-background-radius: 8;");
            tarjetaVuelo.setPrefHeight(100);

            VBox info = new VBox(5);
            info.getChildren().addAll(
                    new Label("🛫 Sin escalas     4h 29min"),
                    new Label("10:46 AM - 4:15 PM"),
                    new Label("SFO → MEX"),
                    new Label("United Airlines")
            );

            VBox precioBox = new VBox();
            Label precio = new Label("US$539.52");
            precio.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            Button seleccionar = new Button("Selecciona");
            seleccionar.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold;");

            precioBox.getChildren().addAll(precio, seleccionar);
            precioBox.setSpacing(5);
            tarjetaVuelo.setLeft(info);
            tarjetaVuelo.setRight(precioBox);
            BorderPane.setMargin(precioBox, new Insets(0, 20, 0, 0));

            vuelosBox.getChildren().add(tarjetaVuelo);
        }

        flightScrollPane.setContent(vuelosBox);
    }


}

