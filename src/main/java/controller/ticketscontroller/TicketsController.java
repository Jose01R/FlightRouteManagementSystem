package controller.ticketscontroller;
import domain.common.Flight;
import domain.common.Passenger;
import domain.service.AirNetworkService;
import domain.service.AirplaneService;
import domain.service.FlightService;
import domain.service.PassengerService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private AirplaneService airplaneService;
    private Map<Flight, Label> occupancyLabels = new HashMap<>();
    @FXML
    public void initialize(){

    }
    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.airplaneService=airplaneService;
        this.airNetworkService= airNetworkService;
    }
    @FXML
    void searchFlightOnAction(ActionEvent event) {
        String from = fromTf.getText().trim();
        String to = toTf.getText().trim();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Obtén la lista de vuelos válidos
        List<Flight> vuelosDisponibles = flightService.getAvailableFlights(from, to, fromDate, toDate, oneWayRadioButtom.isSelected());

        VBox vuelosBox = new VBox(15);
        vuelosBox.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");

        for (Flight vuelo : vuelosDisponibles) {
            BorderPane tarjetaVuelo = new BorderPane();
            tarjetaVuelo.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-padding: 18;" +
                            "-fx-border-color: #dedede;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, #cccccc, 6, 0.3, 0, 2);"
            );
            tarjetaVuelo.setPrefHeight(120);
            tarjetaVuelo.setMaxWidth(820);

            // DATOS REALES DEL VUELO Y RUTA
            String duracion = calcularDuracionVuelo(vuelo);
            String horaSalida = vuelo.getDepartureTime().toLocalTime().toString();
            String horaLlegada = vuelo.getAssignedRoute().getArrivalTime().toString();
            String origen = String.valueOf(vuelo.getAssignedRoute().getOriginAirportCode());
            String destino = String.valueOf(vuelo.getAssignedRoute().getDestinationAirportCode());
            String airline = vuelo.getAssignedRoute().getAirline();
            String avion = vuelo.getAssignedAirplane().getModel();
            String serial = vuelo.getAssignedAirplane().getSerialNumber();
            String precio = String.valueOf(vuelo.getAssignedRoute().getPrice());

            Label occupancyLabel = new Label("Occupancy: " + vuelo.getOccupancy()+"     Capacity: "+vuelo.getCapacity());
            occupancyLabels.put(vuelo, occupancyLabel);

            VBox info = new VBox(8);
            info.setPadding(new Insets(0, 0, 0, 10));
            info.getChildren().addAll(
                    new Label("🛫  "),
                    occupancyLabel,
                    new Label("Duración:  " + duracion),
                    new Label(horaSalida + " - " + horaLlegada),
                    new Label(origen + " → " + destino),
                    new Label("Aerolínea: " + airline + " | Avión: " + avion + " (" + serial + ")")
            );
            for (javafx.scene.Node node : info.getChildren()) {
                ((Label)node).setStyle("-fx-font-size: 15; -fx-text-fill: #333333;");
            }

            VBox precioBox = new VBox();
            Label precioLabel = new Label("₡" + precio);
            precioLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 19; -fx-text-fill: #e67e22;");
            Button seleccionar = new Button("Seleccionar");
            seleccionar.setStyle("-fx-background-color: #ff8000; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 7;");
            seleccionar.setOnAction(e -> mostrarDialogoCompra(vuelo));
            precioBox.getChildren().addAll(precioLabel, seleccionar);
            precioBox.setSpacing(10);
            precioBox.setPadding(new Insets(10, 30, 0, 10));

            tarjetaVuelo.setLeft(info);
            tarjetaVuelo.setRight(precioBox);
            BorderPane.setMargin(precioBox, new Insets(0, 10, 0, 0));
            BorderPane.setMargin(info, new Insets(0, 0, 0, 10));

            vuelosBox.getChildren().add(tarjetaVuelo);
        }

        flightScrollPane.setContent(vuelosBox);
    }
    public void actualizarOcupacionVuelo(Flight vuelo) {
        Label label = occupancyLabels.get(vuelo);
        if (label != null) {
            label.setText("Occupancy: " + vuelo.getOccupancy());
        }
    }
    private String calcularDuracionVuelo(Flight vuelo) {
        if (vuelo == null || vuelo.getAssignedRoute() == null) return "N/A";
        LocalTime horaSalida = vuelo.getDepartureTime().toLocalTime();
        LocalTime horaLlegada = vuelo.getAssignedRoute().getArrivalTime();
        if (horaLlegada == null || horaSalida == null) return "N/A";
        long minutos = java.time.Duration.between(horaSalida, horaLlegada).toMinutes();
        if (minutos < 0) minutos += 24 * 60;
        long horas = minutos / 60;
        long mins = minutos % 60;
        return String.format("%dh %02dmin", horas, mins);
    }

    private void mostrarDialogoCompra(Flight vuelo) {
        Dialog<Integer> cantidadDialog = new Dialog<>();
        cantidadDialog.setTitle("Comprar Tiquete(s)");
        cantidadDialog.setHeaderText("Compra de tiquetes para el vuelo " + vuelo.getNumber());

        int disponibles = vuelo.getCapacity() - vuelo.getOccupancy();
        Spinner<Integer> spinnerCantidad = new Spinner<>();
        spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, Math.max(1, disponibles), 1));
        spinnerCantidad.setEditable(false);

        VBox fields = new VBox(10,
                new Label("Cantidad de tiquetes:"), spinnerCantidad
        );
        fields.setPadding(new Insets(10));
        cantidadDialog.getDialogPane().setContent(fields);
        cantidadDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Deshabilitar OK si no hay tiquetes
        Node okButton = cantidadDialog.getDialogPane().lookupButton(ButtonType.OK);
        if (disponibles <= 0) okButton.setDisable(true);

        Platform.runLater(spinnerCantidad::requestFocus);

        cantidadDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return spinnerCantidad.getValue();
            }
            return null;
        });

        cantidadDialog.showAndWait().ifPresent(cantidad -> {
            for (int i = 1; i <= cantidad; i++) {
                // Diálogo para datos de cada pasajero
                Dialog<Passenger> pasajeroDialog = new Dialog<>();
                pasajeroDialog.setTitle("Datos del pasajero " + i);
                pasajeroDialog.setHeaderText("Ingrese los datos del pasajero #" + i);

                TextField nombreField = new TextField();
                nombreField.setPromptText("Nombre completo");
                TextField idField = new TextField();
                idField.setPromptText("Cédula o ID");
                TextField nacionalityField = new TextField();
                nacionalityField.setPromptText("Nacionalidad");

                VBox pasajeroFields = new VBox(10,
                        new Label("Cédula/ID:"), idField,
                        new Label("Nombre:"), nombreField,
                        new Label("Nacionalidad:"), nacionalityField
                );
                pasajeroFields.setPadding(new Insets(10));
                pasajeroDialog.getDialogPane().setContent(pasajeroFields);
                pasajeroDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Validación reactiva
                Node pasajeroOk = pasajeroDialog.getDialogPane().lookupButton(ButtonType.OK);
                pasajeroOk.setDisable(true);

                Runnable validate = () -> pasajeroOk.setDisable(
                        nombreField.getText().trim().isEmpty() ||
                                idField.getText().trim().isEmpty() ||
                                nacionalityField.getText().trim().isEmpty()
                );
                nombreField.textProperty().addListener((o, oldVal, newVal) -> validate.run());
                idField.textProperty().addListener((o, oldVal, newVal) -> validate.run());
                nacionalityField.textProperty().addListener((o, oldVal, newVal) -> validate.run());

                Platform.runLater(nombreField::requestFocus);

                pasajeroDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        try {
                            int id = Integer.parseInt(idField.getText().trim());
                            return new Passenger(id, nombreField.getText().trim(), nacionalityField.getText().trim());
                        } catch (NumberFormatException ex) {
                            Alert error = new Alert(Alert.AlertType.ERROR, "La cédula/ID debe ser un número.");
                            error.showAndWait();
                            return null;
                        }
                    }
                    return null;
                });

                // Si se cancela en algún diálogo, se aborta todo el proceso
                Passenger pasajero = pasajeroDialog.showAndWait().orElse(null);
                if (pasajero == null) {
                    Alert cancelAlert = new Alert(Alert.AlertType.INFORMATION, "Compra cancelada.");
                    cancelAlert.showAndWait();
                    return;
                }
                try {
                    // Buscar si ya existe el pasajero por ID
                    Passenger existente = passengerService.findPassengerById(pasajero.getId());
                    if (existente == null) {
                        // No existe, registra y guarda
                        passengerService.registerPassenger(pasajero);
                    } else {
                        // Si existe, tomar los datos del existente por si difieren
                        pasajero = existente;
                    }
                    // Asignar pasajero al vuelo
                    boolean asignado = flightService.assignPassengerToFlight(vuelo.getNumber(), pasajero);
                    if (!asignado) {
                        Alert err = new Alert(Alert.AlertType.ERROR, "No se pudo asignar el pasajero al vuelo.");
                        err.showAndWait();
                        return;
                    }
                } catch (Exception e) {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Error al asignar pasajero: " + e.getMessage());
                    err.showAndWait();
                    return;
                }
            }

            // Confirmación
            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Compra completada. " + cantidad + " pasajero(s) registrados en el vuelo.");
            ok.showAndWait();

            actualizarOcupacionVuelo(vuelo); // Si tienes método para refrescar la UI
        });
    }

}

