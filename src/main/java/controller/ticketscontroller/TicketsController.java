package controller.ticketscontroller;

import domain.common.Airport;
import domain.common.Flight;
import domain.common.Passenger;
import domain.service.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class TicketsController {

    @FXML
    private ScrollPane flightScrollPane;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private TextField fromTf;

    @FXML
    private TextField toTf;

    private FlightService flightService;
    private PassengerService passengerService;
    private AirNetworkService airNetworkService;
    private AirplaneService airplaneService;
    private AirportService airportService;

    private Map<Flight, Label> occupancyLabels = new HashMap<>();
    private Map<Integer, String> airportCodeToName = new HashMap<>();
    private Map<String, Integer> airportNameToCode = new HashMap<>();

    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService, AirportService airportService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.airplaneService = airplaneService;
        this.airNetworkService = airNetworkService;
        this.airportService = airportService;

        cargarMapaAeropuertosActivos();

        //Ahora que los mapas tienen datos, crea la lista de nombres y configura autocomplete
        List<String> airportNames = new ArrayList<>(airportNameToCode.keySet());
        setupAutoComplete(fromTf, airportNames);
        setupAutoComplete(toTf, airportNames);
    }

    @FXML
    public void initialize() {

    }

    private void cargarMapaAeropuertosActivos() {
        try {
            ArrayList<Object> activos = airportService.getAirportsByStatus("Active");
            airportCodeToName.clear();
            airportNameToCode.clear();
            for (Object obj : activos) {
                if (obj instanceof Airport airport) {
                    airportCodeToName.put(airport.getCode(), airport.getName());
                    airportNameToCode.put(airport.getName(), airport.getCode());
                }
            }
        } catch (Exception e) {
            airportCodeToName.clear();
            airportNameToCode.clear();
        }
    }

    private String getAirportNameByCode(int code) {
        return airportCodeToName.getOrDefault(code, "Desconocido");
    }

    private Integer getAirportCodeByName(String name) {
        return airportNameToCode.get(name);
    }

    @FXML
    void searchFlightOnAction(ActionEvent event) {
        String fromName = fromTf.getText().trim();
        String toName = toTf.getText().trim();
        LocalDate fromDate = fromDatePicker.getValue();

        Integer fromCode = getAirportCodeByName(fromName);
        Integer toCode = getAirportCodeByName(toName);
        if (fromDate.isBefore(LocalDate.now())) {
            util.FXUtility.alertWarning( "Fecha inválida", "La fecha de salida no puede ser anterior al día de hoy.");
            return;
        }
        // Si alguno no se encuentra, muestra error
        if (fromCode == null || toCode == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleccione aeropuertos válidos usando la lista de sugerencias.");
            alert.showAndWait();
            return;
        }

        // Busca solo por códigos
        List<Flight> vuelosDisponibles = flightService.getAvailableFlights(
                String.valueOf(fromCode),
                String.valueOf(toCode),
                fromDate
        );

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

            String duracion = calcularDuracionVuelo(vuelo);
            String horaSalida = vuelo.getDepartureTime().toLocalTime().toString();
            String horaLlegada = vuelo.getAssignedRoute().getArrivalTime().toString();
            String origen = getAirportNameByCode(vuelo.getAssignedRoute().getOriginAirportCode());
            String destino = getAirportNameByCode(vuelo.getAssignedRoute().getDestinationAirportCode());
            String airline = vuelo.getAssignedRoute().getAirline();
            String avion = vuelo.getAssignedAirplane().getModel();
            String serial = vuelo.getAssignedAirplane().getSerialNumber();
            String precio = String.valueOf(vuelo.getAssignedRoute().getPrice());

            Label occupancyLabel = new Label("Occupancy: " + vuelo.getOccupancy() + "     Capacity: " + vuelo.getCapacity());
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
            for (Node node : info.getChildren()) {
                ((Label) node).setStyle("-fx-font-size: 15; -fx-text-fill: #333333;");
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

    private void setupAutoComplete(TextField textField, List<String> suggestions) {
        ContextMenu contextMenu = new ContextMenu();

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                contextMenu.hide();
                return;
            }
            List<String> filtered = suggestions.stream()
                    .filter(s -> s.toLowerCase().contains(newText.toLowerCase()))
                    .toList();

            if (filtered.isEmpty()) {
                contextMenu.hide();
            } else {
                contextMenu.getItems().clear();
                for (String s : filtered) {
                    MenuItem item = new MenuItem(s);
                    item.setOnAction(e -> {
                        textField.setText(s);
                        contextMenu.hide();
                    });
                    contextMenu.getItems().add(item);
                }
                contextMenu.show(textField, Side.BOTTOM, 0, 0);
            }
        });

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) contextMenu.hide();
        });
    }

    private void mostrarDialogoCompra(Flight vuelo) {
        Dialog<Integer> cantidadDialog = new Dialog<>();
        cantidadDialog.setTitle("Comprar Tiquete(s)");
        cantidadDialog.setHeaderText("Compra de tiquetes para el vuelo " + vuelo.getNumber());

        // Permite elegir más tiquetes que los cupos disponibles en este vuelo
        Spinner<Integer> spinnerCantidad = new Spinner<>();
        spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 20, 1)); // Puedes ajustar el 20 a un máximo razonable
        spinnerCantidad.setEditable(false);

        VBox fields = new VBox(10,
                new Label("Cantidad de tiquetes:"), spinnerCantidad
        );
        fields.setPadding(new Insets(10));
        cantidadDialog.getDialogPane().setContent(fields);
        cantidadDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Platform.runLater(spinnerCantidad::requestFocus);

        cantidadDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return spinnerCantidad.getValue();
            }
            return null;
        });

        cantidadDialog.showAndWait().ifPresent(cantidad -> {
            int pasajerosAsignados = 0;
            int contadorPasajero = 1;
            Flight vueloReferencia = vuelo; // Para buscar siguiente vuelo
            for (int i = 1; i <= cantidad; i++) {
                // --- Diálogo por cada pasajero ---
                Dialog<Passenger> pasajeroDialog = new Dialog<>();
                pasajeroDialog.setTitle("Datos del pasajero " + contadorPasajero);
                pasajeroDialog.setHeaderText("Ingrese los datos del pasajero #" + contadorPasajero);

                TextField idField = new TextField();
                idField.setPromptText("Cédula o ID");
                Label idError = new Label();
                idError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

                TextField nombreField = new TextField();
                nombreField.setPromptText("Nombre completo");
                Label nombreError = new Label();
                nombreError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

                TextField nacionalityField = new TextField();
                nacionalityField.setPromptText("Nacionalidad");
                Label nacionalidadError = new Label();
                nacionalidadError.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

                VBox pasajeroFields = new VBox(6,
                        new Label("Cédula/ID:"), idField, idError,
                        new Label("Nombre:"), nombreField, nombreError,
                        new Label("Nacionalidad:"), nacionalityField, nacionalidadError
                );
                pasajeroFields.setPadding(new Insets(10));
                pasajeroDialog.getDialogPane().setContent(pasajeroFields);
                pasajeroDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Node pasajeroOk = pasajeroDialog.getDialogPane().lookupButton(ButtonType.OK);
                pasajeroOk.setDisable(true);

                Runnable validate = () -> {
                    boolean valid = true;
                    idError.setText("");
                    nombreError.setText("");
                    nacionalidadError.setText("");

                    // Validar ID (no vacío y numérico)
                    String idText = idField.getText().trim();
                    if (idText.isEmpty()) {
                        idError.setText("La cédula/ID es obligatoria.");
                        valid = false;
                    } else {
                        try {
                            Integer.parseInt(idText);
                        } catch (NumberFormatException e) {
                            idError.setText("Debe ser un número.");
                            valid = false;
                        }
                    }
                    // Validar nombre
                    String nombreText = nombreField.getText().trim();
                    if (nombreText.isEmpty()) {
                        nombreError.setText("El nombre es obligatorio.");
                        valid = false;
                    } else if (nombreText.matches(".*\\d.*")) {
                        nombreError.setText("El nombre no puede contener números.");
                        valid = false;
                    }
                    // Validar nacionalidad
                    String nacionalidadText = nacionalityField.getText().trim();
                    if (nacionalidadText.isEmpty()) {
                        nacionalidadError.setText("La nacionalidad es obligatoria.");
                        valid = false;
                    } else if (nacionalidadText.matches(".*\\d.*")) {
                        nacionalidadError.setText("La nacionalidad no puede contener números.");
                        valid = false;
                    }
                    pasajeroOk.setDisable(!valid);
                };

                idField.textProperty().addListener((o, oldVal, newVal) -> validate.run());
                nombreField.textProperty().addListener((o, oldVal, newVal) -> validate.run());
                nacionalityField.textProperty().addListener((o, oldVal, newVal) -> validate.run());

                Platform.runLater(nombreField::requestFocus);

                pasajeroDialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                        ActionEvent.ACTION, event -> {
                            validate.run();
                            if (pasajeroOk.isDisable()) {
                                event.consume(); // No cierra el diálogo
                            }
                        });

                pasajeroDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK && !pasajeroOk.isDisable()) {
                        return new Passenger(
                                Integer.parseInt(idField.getText().trim()),
                                nombreField.getText().trim(),
                                nacionalityField.getText().trim()
                        );
                    }
                    return null;
                });

                Passenger pasajero = pasajeroDialog.showAndWait().orElse(null);
                if (pasajero == null) {
                    Alert cancelAlert = new Alert(Alert.AlertType.INFORMATION, "Compra cancelada.");
                    cancelAlert.showAndWait();
                    return;
                }
                try {
                    Passenger existente = passengerService.findPassengerById(pasajero.getId());
                    if (existente == null) {
                        passengerService.registerPassenger(pasajero);
                    } else {
                        pasajero = existente;
                    }

                    boolean asignado = false;
                    Flight vueloParaAsignar = vueloReferencia;
                    while (vueloParaAsignar != null && !asignado) {
                        try {
                            asignado = flightService.assignPassengerToFlight(vueloParaAsignar.getNumber(), pasajero);
                            if (!asignado) {
                                vueloParaAsignar = flightService.findNextAvailableFlight(vueloParaAsignar);
                            }
                        } catch (Exception e) {
                            // Si es vuelo lleno, seguir, si no mostrar error y salir
                            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                            if (msg.contains("lleno")) {
                                vueloParaAsignar = flightService.findNextAvailableFlight(vueloParaAsignar);
                            } else if (msg.contains("ya está asignado")) {
                                Alert err = new Alert(Alert.AlertType.ERROR, "El pasajero ya está asignado a este vuelo.");
                                err.showAndWait();
                                return;
                            } else {
                                Alert err = new Alert(Alert.AlertType.ERROR, "Error al asignar pasajero: " + e.getMessage());
                                err.showAndWait();
                                return;
                            }
                        }
                    }
                    if (!asignado) {
                        Alert err = new Alert(Alert.AlertType.ERROR, "No se pudo asignar el pasajero a ningún vuelo disponible con la misma ruta.");
                        err.showAndWait();
                        return;
                    }
                    if (vueloParaAsignar != vueloReferencia) {
                        Alert info = new Alert(Alert.AlertType.INFORMATION,
                                "El pasajero fue asignado al siguiente vuelo disponible: "
                                        + vueloParaAsignar.getNumber() + " (" + getAirportNameByCode(vueloParaAsignar.getAssignedRoute().getOriginAirportCode())
                                        + " → " + getAirportNameByCode(vueloParaAsignar.getAssignedRoute().getDestinationAirportCode()) + ")");
                        info.showAndWait();
                    }
                    vueloReferencia = vueloParaAsignar; // Para los siguientes pasajeros, seguir desde el último vuelo usado
                    pasajerosAsignados++;
                    contadorPasajero++;
                } catch (Exception e) {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Error al registrar pasajero: " + e.getMessage());
                    err.showAndWait();
                    return;
                }
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "Compra completada. " + pasajerosAsignados + " pasajero(s) registrados en el/los vuelo(s).");
            ok.showAndWait();

            Platform.runLater(() -> searchFlightOnAction(null));
        });
    }
    @FXML
    public void clearOnAction(){
        toTf.clear();
        fromTf.clear();
        fromDatePicker.setValue(null);
    }
}


