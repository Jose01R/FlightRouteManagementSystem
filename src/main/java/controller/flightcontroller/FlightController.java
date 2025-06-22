package controller.flightcontroller;

import data.FlightData;
import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Airplane;
import domain.common.Flight;
import domain.common.Passenger;
import domain.common.Route;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import domain.service.AirNetworkService;
import domain.service.AirplaneService;
import domain.service.FlightService;
import domain.service.PassengerService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightController {

    @FXML
    private ComboBox<Flight> assignedFlightComboBox;

    @FXML
    private TextField capacityField;

    @FXML
    private DatePicker departureDatePicker;

    @FXML
    private TextField departureTimeField;



    @FXML
    private TableColumn<Passenger,String > flightColumn;

    @FXML
    private TextField flightNumberField;

    @FXML
    private TableView<Flight> flightTable;

    @FXML
    private TableColumn<Flight, Integer> flightTableNumberColumn;
    @FXML
    private TableColumn<Flight, String> flightTableOriginColumn;
    @FXML
    private TableColumn<Flight, String> flightTableDestinationColumn;
    @FXML
    private TableColumn<Flight, LocalDateTime> flightTableDepartureTimeColumn;
    @FXML
    private TableColumn<Flight, Integer> flightTableCapacityColumn;
    @FXML
    private TableColumn<Flight, Integer> flightTableOccupancyColumn;
    @FXML
    private TableColumn<Flight, String> flightTableStatusColumn;
    @FXML TableColumn<Flight,String>airPlaneIdColumn;
    @FXML
    private TableColumn<Passenger,Integer> idColumn;
    @FXML
    private TableColumn<Passenger, String> nameColumn;
    @FXML
    private TableColumn<Passenger, String> nationalityColumn;
    @FXML
    private TableColumn<Passenger, String> flightHistoryColumn;

    @FXML
    private TextField nationalityField;

    @FXML private ComboBox<Airplane>airplaneComboBox;
    @FXML private ComboBox<Route>routeComboBox;

    @FXML
    private TextField passengerIdField;

    @FXML
    private TextField passengerNameField;

    @FXML
    private TableView<Passenger> passengerTable;

    @FXML private TextField searchTextField;
    @FXML private TextField searchByOrigenTf;
    @FXML private TextField searchByArrivalTf;
    private AVL avlPassengers;
    private PassengerService passengerService;
    private PassengerData passengerData;
    private FlightData flightData;
    private FlightService flightService;
    private AirplaneService airplaneService;
    private AirNetworkService airNetworkService;
    private CircularDoublyLinkedList circularDoublyLinkedList;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        // Configuración de CellValueFactory para las columnas de la tabla de Pasajeros
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nationalityColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNationality()));


        // --- CAMBIOS AQUÍ: flightColumn ---
        flightColumn.setCellValueFactory(cell -> {
            Passenger p = cell.getValue();
            if (p != null && p.getFlightHistory() != null && !p.getFlightHistory().isEmpty()) {
                try {
                    Object lastFlightNumObject = p.getFlightHistory().getNode(p.getFlightHistory().size()).data;

                    if (lastFlightNumObject instanceof Integer) { // Verificar si es un Integer
                        Integer flightNumber = (Integer) lastFlightNumObject; // Casteo seguro a Integer

                        // ¡Buscar el objeto Flight real usando el número de vuelo!
                        Flight actualFlight = flightService.findFlightByNumber(flightNumber); // Necesitas este método en FlightService

                        if (actualFlight != null) {
                            return new SimpleStringProperty(String.valueOf(actualFlight.getNumber()));
                        } else {
                            System.err.println("Advertencia: Objeto Flight no encontrado para el número: " + flightNumber);
                            return new SimpleStringProperty("N/A (Vuelo no encontrado)");
                        }
                    } else {
                        System.err.println("Advertencia: Se encontró un objeto inesperado en el historial de vuelo (se esperaba Integer): " +
                                (lastFlightNumObject != null ? lastFlightNumObject.getClass().getSimpleName() : "null"));
                        return new SimpleStringProperty("N/A (Tipo incorrecto)");
                    }
                } catch (ListException e) { // Captura ListException de tu SinglyLinkedList
                    System.err.println("Error al obtener el último vuelo del historial (ListException): " + e.getMessage());
                    return new SimpleStringProperty("N/A (Error de lista)");
                } catch (Exception e) { // Captura otras excepciones generales
                    System.err.println("Error inesperado al obtener el último vuelo del historial: " + e.getMessage());
                    e.printStackTrace(); // Imprime el stack trace para depuración
                    return new SimpleStringProperty("N/A (Error)");
                }
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        flightTableStatusColumn.setCellValueFactory(cellData -> {
            boolean completed = cellData.getValue().isCompleted();
            String status = completed ? "Completado" : "Activo";
            return new ReadOnlyStringWrapper(status);
        });

        airPlaneIdColumn.setCellValueFactory(cellData -> {
            Airplane airplane = cellData.getValue().getAssignedAirplane();
            if (airplane != null) {
                String info = airplane.getSerialNumber() + " (" + airplane.getModel() + ")";
                return new SimpleStringProperty(info);
            } else {
                return new SimpleStringProperty("Sin avión");
            }
        });
        flightHistoryColumn.setCellValueFactory(cell -> {
            Passenger p = cell.getValue();
            if (p != null && p.getFlightHistory() != null && !p.getFlightHistory().isEmpty()) {
                StringBuilder historyBuilder = new StringBuilder();
                try {
                    for (int i = 1; i <= p.getFlightHistory().size(); i++) {
                        Object data = p.getFlightHistory().getNode(i).data;
                        if (data instanceof Integer) {
                            historyBuilder.append("#").append(data);
                            if (i < p.getFlightHistory().size()) {
                                historyBuilder.append(", ");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty("Error al cargar historial");
                }
                return new SimpleStringProperty(historyBuilder.toString());
            } else {
                return new SimpleStringProperty("Sin vuelos registrados");
            }
        });


        flightTableNumberColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNumber()).asObject());
        flightTableOriginColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrigin()));
        flightTableDestinationColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDestination()));
        flightTableDepartureTimeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDepartureTime()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        flightTableDepartureTimeColumn.setCellFactory(column -> new TableCell<Flight, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        flightTableCapacityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCapacity()).asObject());
        flightTableOccupancyColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getOccupancy()).asObject());


        assignedFlightComboBox.setConverter(new StringConverter<Flight>() {
            @Override
            public String toString(Flight flight) {

                return flight != null ? "Vuelo #" + flight.getNumber() + " (" + flight.getOrigin() + " -> " + flight.getDestination() + ")" : "";
            }
            @Override
            public Flight fromString(String string) {

                return null;
            }
        });
        airplaneComboBox.setConverter(new StringConverter<Airplane>() {
            @Override
            public String toString(Airplane airplane) {
                return (airplane != null) ? airplane.getSerialNumber() + " (" + airplane.getModel() + ")" : "";
            }

            @Override
            public Airplane fromString(String string) {
                return null; // No se necesita
            }
        });

        routeComboBox.setConverter(new StringConverter<Route>() {
            @Override
            public String toString(Route route) {
                return (route != null) ? route.getOriginAirportCode() + " → " + route.getDestinationAirportCode() : "";
            }

            @Override
            public Route fromString(String string) {
                return null;
            }
        });

        searchTextField.textProperty().addListener((obs, oldText, newText) -> {
            reorderTableViewByPassengerId(newText);
        });
        passengerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                passengerIdField.setText(String.valueOf(newSelection.getId()));
            }
        });
        searchByOrigenTf.textProperty().addListener((obs, oldText, newText) -> {
            reorderFlightTableView(searchByOrigenTf.getText(), searchByArrivalTf.getText());
        });

        searchByArrivalTf.textProperty().addListener((obs, oldText, newText) -> {
            reorderFlightTableView(searchByOrigenTf.getText(), searchByArrivalTf.getText());
        });
        flightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            assignedFlightComboBox.getSelectionModel().select(newSelection);
        });

    }

    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService) {
        this.passengerService = passengerService;
        this.flightService = flightService;
        this.airplaneService=airplaneService;
        this.airNetworkService= airNetworkService;
        passengerTable.setItems(this.passengerService.getObservablePassengers());
        assignedFlightComboBox.setItems(this.flightService.getObservableFlights());
        flightTable.setItems(this.flightService.getObservableFlights());
        routeComboBox.setItems(airNetworkService.getObservableRoutes());
        airplaneComboBox.setItems(airplaneService.getObservableAirplanes());

    }



    @FXML
    void handleRegisterPassenger(ActionEvent event) {
        try {
            int passengerId = Integer.parseInt(passengerIdField.getText());
            String passengerName = passengerNameField.getText();
            String nationality = nationalityField.getText();

            if (passengerName.isEmpty() || nationality.isEmpty() ) {
                showAlert(Alert.AlertType.ERROR, "Error de Entrada", "Todos los campos del pasajero son obligatorios.");
                return;
            }

            Flight assignedFlight = assignedFlightComboBox.getValue();
            if (assignedFlight == null) {
                showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, seleccione un vuelo para asignar al pasajero.");
                return;
            }

            Flight flightFound = flightService.findFlightByNumber(assignedFlight.getNumber());
            if (flightFound == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "El vuelo seleccionado no fue encontrado.");
                return;
            }

            if (flightFound.getOccupancy() >= flightFound.getCapacity()) {
                showAlert(Alert.AlertType.INFORMATION, "Vuelo Lleno", "El vuelo " + flightFound.getNumber() + " está lleno. El pasajero no puede ser asignado.");
                return;
            }

            Passenger passenger = passengerService.findPassengerById(passengerId);
            if (passenger == null) {
                passenger = new Passenger(passengerId, passengerName, nationality);
                passenger.setFlightHistory(new SinglyLinkedList());

                if (!passengerService.registerPassenger(passenger)) {
                    showAlert(Alert.AlertType.ERROR, "Pasajero Existente", "Ya existe un pasajero con el ID: " + passenger.getId());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Pasajero Existente", "Ya existe un pasajero con el ID: " + passenger.getId());
                return;
            }


            //Esta linea es por si se decide que se puedan editar los datos del pasajero
            //passengerService.updatePassenger(passenger);

            boolean assignedSuccessfully = flightService.assignPassengerToFlight(flightFound.getNumber(), passenger);


            if (assignedSuccessfully) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Pasajero registrado y asignado al vuelo " + flightFound.getNumber() + ".");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de Asignación", "No se pudo asignar el pasajero al vuelo.");
                return;
            }


            passengerIdField.clear();
            passengerNameField.clear();
            nationalityField.clear();
            assignedFlightComboBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Entrada", "ID del pasajero debe ser un número válido.");
        } catch (TreeException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Sistema", "Ocurrió un error al procesar la solicitud: " + e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void handleRegisterFlight(ActionEvent event) {
        try {
            int flightNumber = Integer.parseInt(flightNumberField.getText());
            Route selectedRoute = routeComboBox.getValue();
            Airplane selectedAirplane = airplaneComboBox.getValue();
            int capacity = Integer.parseInt(capacityField.getText());

            LocalDate selectedDate = departureDatePicker.getValue();
            String timeString = departureTimeField.getText();

            if (selectedRoute==null || selectedAirplane==null|| timeString.isEmpty() || selectedDate == null) {
                showAlert(Alert.AlertType.ERROR, "Error de Entrada", "Todos los campos de vuelo son obligatorios.");
                return;
            }
            if (capacity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error de Entrada", "La capacidad debe ser un número positivo.");
                return;
            }

            LocalTime selectedTime;
            try {
                selectedTime = LocalTime.parse(timeString);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Formato de Hora Inválido", "Por favor, ingrese la hora en formato HH:mm (ej. 14:30) o HH:mm:ss.");
                return;
            }

            LocalDateTime departureDateTime = LocalDateTime.of(selectedDate, selectedTime);
            Flight flight = new Flight(flightNumber, departureDateTime, capacity,selectedAirplane, selectedRoute);
            flightService.createFlight(flight);


            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Vuelo " + flightNumber + " registrado correctamente.");


            flightNumberField.clear();
            capacityField.clear();
            departureDatePicker.setValue(null);
            departureTimeField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Entrada", "Número de vuelo y capacidad deben ser números válidos.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error al registrar el vuelo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleAssignedFlightToExistingPassenger(ActionEvent event) {

        String passengerIdText = passengerIdField.getText();
        if (passengerIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, ingrese el ID del pasajero en el campo 'ID' para asignarle un vuelo.");
            return;
        }

        int passengerId;
        try {
            passengerId = Integer.parseInt(passengerIdText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Formato", "El ID del pasajero debe ser un número válido.");
            return;
        }

        Flight assignedFlight = assignedFlightComboBox.getValue();
        if (assignedFlight == null) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Por favor, seleccione un vuelo para asignar al pasajero.");
            return;
        }

        try {

            Passenger foundPassenger = passengerService.findPassengerById(passengerId);
            if (foundPassenger == null) {
                showAlert(Alert.AlertType.ERROR, "Pasajero No Encontrado", "No se encontró un pasajero con el ID: " + passengerId + ". Por favor, regístrelo primero.");
                return;
            }


            boolean flightAlreadyInHistory = false;
            if (foundPassenger.getFlightHistory() != null) {
                try {
                    for (int i = 1; i <= foundPassenger.getFlightHistory().size(); i++) {
                        Object obj = foundPassenger.getFlightHistory().getNode(i).data;
                        if (obj instanceof Flight && ((Flight) obj).getNumber() == assignedFlight.getNumber()) {
                            flightAlreadyInHistory = true;
                            break;
                        }
                    }
                } catch (ListException e) {
                    System.err.println("Error al verificar historial de vuelos para pasajero existente: " + e.getMessage());
                }
            }

            Flight flightInSystem = flightService.findFlightByNumber(assignedFlight.getNumber());
            if (flightInSystem == null) {
                showAlert(Alert.AlertType.ERROR, "Error Interno", "El vuelo seleccionado no fue encontrado en el sistema.");
                return;
            }

            if (flightInSystem.getOccupancy() >= flightInSystem.getCapacity()) {
                showAlert(Alert.AlertType.INFORMATION, "Vuelo Lleno", "El vuelo " + flightInSystem.getNumber() + " está lleno. El pasajero no puede ser asignado.");
                return;
            }


            if (!flightAlreadyInHistory) {

                if (foundPassenger.getFlightHistory() == null) {
                    foundPassenger.setFlightHistory(new SinglyLinkedList());
                }
                foundPassenger.getFlightHistory().add(flightInSystem);
                passengerService.updatePassenger(foundPassenger);


                boolean assignedSuccessfully = flightService.assignPassengerToFlight(flightInSystem.getNumber(), foundPassenger);

                if (assignedSuccessfully) {
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Vuelo " + flightInSystem.getNumber() + " asignado a pasajero ID: " + foundPassenger.getId() + " correctamente.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error de Asignación", "No se pudo asignar el pasajero al vuelo. (Error interno)");
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Vuelo ya Asignado", "El pasajero ID: " + foundPassenger.getId() + " ya tiene el vuelo " + flightInSystem.getNumber() + " en su historial.");
            }


        } catch (TreeException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Sistema (Árbol AVL)", "Error al buscar o actualizar pasajero: " + e.getMessage());
            e.printStackTrace();
        } catch (ListException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Sistema (Lista Enlazada)", "Error al asignar vuelo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error al asignar el vuelo al pasajero existente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void reorderTableViewByPassengerId(String input) {
        ObservableList<Passenger> filteredList = FXCollections.observableArrayList();

        if (input == null || input.isEmpty()) {
            passengerTable.setItems(passengerService.getObservablePassengers());
            return;
        }

        for (Passenger p : passengerService.getObservablePassengers()) {
            if (String.valueOf(p.getId()).startsWith(input)) {
                filteredList.add(p); // Coincidencias parciales primero
            }
        }

        for (Passenger p : passengerService.getObservablePassengers()) {
            if (!String.valueOf(p.getId()).startsWith(input)) {
                filteredList.add(p); // El resto después
            }
        }

        passengerTable.setItems(filteredList);
        passengerTable.refresh(); // Refresca la vista por si ya estaba seleccionado
    }
    private void reorderFlightTableView(String originInput, String destinationInput) {
        ObservableList<Flight> filteredList = FXCollections.observableArrayList();
        ObservableList<Flight> allFlights = flightService.getObservableFlights();

        // Convertir entradas a string para comparar y buscar (pueden ser números o texto)
        String origin = originInput != null ? originInput.trim().toLowerCase() : "";
        String destination = destinationInput != null ? destinationInput.trim().toLowerCase() : "";

        // Coincidencias preferidas primero
        for (Flight f : allFlights) {
            Route route = f.getAssignedRoute();
            String routeOrigin = route != null ? String.valueOf(route.getOriginAirportCode()).toLowerCase() : "";
            String routeDestination = route != null ? String.valueOf(route.getDestinationAirportCode()).toLowerCase() : "";

            boolean matchesOrigin = routeOrigin.startsWith(origin);
            boolean matchesDestination = routeDestination.startsWith(destination);

            if ((origin.isEmpty() || matchesOrigin) && (destination.isEmpty() || matchesDestination)) {
                filteredList.add(f);
            }
        }

        // Luego el resto
        for (Flight f : allFlights) {
            Route route = f.getAssignedRoute();
            String routeOrigin = route != null ? String.valueOf(route.getOriginAirportCode()).toLowerCase() : "";
            String routeDestination = route != null ? String.valueOf(route.getDestinationAirportCode()).toLowerCase() : "";

            boolean matchesOrigin = routeOrigin.startsWith(origin);
            boolean matchesDestination = routeDestination.startsWith(destination);

            if (!((origin.isEmpty() || matchesOrigin) && (destination.isEmpty() || matchesDestination))) {
                filteredList.add(f);
            }
        }

        flightTable.setItems(filteredList);
        flightTable.refresh();
    }


}