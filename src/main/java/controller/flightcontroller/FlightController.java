package controller.flightcontroller;

import data.FlightData;
import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import domain.service.FlightService;
import domain.service.PassengerService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

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
    private TextField destinationField;

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
    private TableColumn<Passenger,Integer> idColumn;
    @FXML
    private TableColumn<Passenger, String> nameColumn;
    @FXML
    private TableColumn<Passenger, String> nationalityColumn;
    @FXML
    private TableColumn<Passenger, String> flightHistoryColumn;

    @FXML
    private TextField nationalityField;

    @FXML
    private TextField originField;

    @FXML
    private TextField passengerIdField;

    @FXML
    private TextField passengerNameField;

    @FXML
    private TableView<Passenger> passengerTable;


    private AVL avlPassengers;
    private PassengerService passengerService;
    private PassengerData passengerData;
    private FlightData flightData;
    private FlightService flightService;
    private CircularDoublyLinkedList circularDoublyLinkedList;

    @FXML
    public void initialize() {
        // Servicios y estructuras
        passengerData = new PassengerData();
        passengerService = new PassengerService(passengerData);
        avlPassengers = passengerService.getAvlTree();

        flightData = new FlightData();
        flightService = new FlightService(flightData);
        circularDoublyLinkedList = flightService.getFlightList(); // Carga la lista de vuelos desde el servicio

        // Configuración de las columnas de la tabla de PASAJEROS
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        nationalityColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNationality()));

        // Columna para el ÚLTIMO VUELO (si aún la necesitas)
        flightColumn.setCellValueFactory(cell -> {
            Passenger p = cell.getValue();
            if (p != null && p.getFlightHistory() != null && !p.getFlightHistory().isEmpty()) {
                try {
                    Flight lastFlight = (Flight) p.getFlightHistory().getNode(p.getFlightHistory().size()).data;
                    return new SimpleStringProperty(String.valueOf(lastFlight.getNumber()));
                } catch (Exception e) {
                    return new SimpleStringProperty("N/A");
                }
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        // Configuración de COLUMNA: HISTORIAL DE VUELOS
        flightHistoryColumn.setCellValueFactory(cell -> {
            Passenger p = cell.getValue();
            if (p != null && p.getFlightHistory() != null && !p.getFlightHistory().isEmpty()) {
                StringBuilder historyBuilder = new StringBuilder();
                try {
                    for (int i = 1; i <= p.getFlightHistory().size(); i++) {
                        Flight flight = (Flight) p.getFlightHistory().getNode(i).data;
                        if (flight != null) {
                            historyBuilder.append("#").append(flight.getNumber());
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


        // Configuración de las columnas de la tabla de vuelos (EXISTENTE)
        flightTableNumberColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNumber()).asObject());
        flightTableOriginColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrigin()));
        flightTableDestinationColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDestination()));
        flightTableDepartureTimeColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDepartureTime()));
        flightTableCapacityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCapacity()).asObject());
        flightTableOccupancyColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getOccupancy()).asObject());

        updatePassengerTable();
        updateFlightTable(); // Llama a este método que ahora tendrá la verificación
        populateAssignedFlightComboBox(); // Llama a este método que ahora tendrá la verificación
    }


    @FXML
    void handleRegisterPassenger(ActionEvent event) {
        try {
            int passengerId = Integer.parseInt(passengerIdField.getText());
            String passengerName = passengerNameField.getText();
            String nationality = nationalityField.getText();

            if (passengerName.isEmpty() || nationality.isEmpty()) {
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
                passengerService.registerPassenger(passenger);
            } else {
                passenger.setName(passengerName);
                passenger.setNationality(nationality);
            }

            if (passenger.getFlightHistory() == null) {
                passenger.setFlightHistory(new SinglyLinkedList());
            }
            passenger.getFlightHistory().add(flightFound);

            passengerService.updatePassenger(passenger); // Persiste los cambios en el pasajero

            boolean assignedSuccessfully = flightService.assignPassengerToFlight(flightFound.getNumber(), passenger);

            if (assignedSuccessfully) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Pasajero registrado y asignado al vuelo " + flightFound.getNumber() + ".");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de Asignación", "No se pudo asignar el pasajero al vuelo.");
                return;
            }

            updatePassengerTable();
            updateFlightTable();
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
            String origin = originField.getText();
            String destination = destinationField.getText();
            int capacity = Integer.parseInt(capacityField.getText());

            LocalDate selectedDate = departureDatePicker.getValue();
            String timeString = departureTimeField.getText();

            if (origin.isEmpty() || destination.isEmpty() || timeString.isEmpty() || selectedDate == null) {
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

            Flight flight = new Flight(flightNumber, origin, destination, departureDateTime, capacity);

            // Asegúrate que flightService.createFlight() gestiona la adición a la lista en memoria
            // y la persistencia en FlightData.
            flightService.createFlight(flight); // Esto debería añadirlo también a circularDoublyLinkedList a través del servicio

            updateFlightTable();
            populateAssignedFlightComboBox();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Vuelo " + flightNumber + " registrado correctamente.");

            flightNumberField.clear();
            originField.clear();
            destinationField.clear();
            capacityField.clear();
            departureDatePicker.setValue(null);
            departureTimeField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Entrada", "Número de vuelo y capacidad deben ser números válidos.");
        } catch (ListException e) { // Esto podría ocurrir si tu createFlight usa ListException de forma interna
            showAlert(Alert.AlertType.ERROR, "Error al manipular la lista de vuelos", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error al registrar el vuelo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePassengerTable() {
        ObservableList<Passenger> data = FXCollections.observableArrayList();
        try {
            for (Object obj : avlPassengers.inOrderNodes1()) {
                data.add((Passenger) obj);
            }
        } catch (TreeException e) {
            e.printStackTrace();
        }
        passengerTable.setItems(data);
    }

    private void updateFlightTable() {
        ObservableList<Flight> data = FXCollections.observableArrayList();
        try {
            // ¡VERIFICACIÓN AÑADIDA AQUÍ!
            if (circularDoublyLinkedList.isEmpty()) {
                flightTable.setItems(data); // Establece una lista vacía
                return; // Sale del método
            }

            // Itera sobre la CircularDoublyLinkedList de vuelos
            for (int i = 1; i <= circularDoublyLinkedList.size(); i++) {
                data.add((Flight) Objects.requireNonNull(circularDoublyLinkedList.getNode(i)).data);
            }
        } catch (ListException e) {
            // Captura la excepción si ocurre por otra razón, por ejemplo, getNode(i) falla
            e.printStackTrace();
            // Opcional: mostrar una alerta si ocurre un error inesperado al cargar la tabla
            showAlert(Alert.AlertType.ERROR, "Error al cargar vuelos", "No se pudieron cargar los vuelos: " + e.getMessage());
        }
        flightTable.setItems(data);
    }

    private void populateAssignedFlightComboBox() {
        ObservableList<Flight> flights = FXCollections.observableArrayList();
        try {
            // ¡VERIFICACIÓN AÑADIDA AQUÍ!
            if (circularDoublyLinkedList.isEmpty()) {
                assignedFlightComboBox.setItems(flights); // Establece una lista vacía
                return; // Sale del método
            }

            for (int i = 1; i <= circularDoublyLinkedList.size(); i++) {
                flights.add((Flight) Objects.requireNonNull(circularDoublyLinkedList.getNode(i)).data);
            }
        } catch (ListException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al cargar vuelos", "No se pudieron cargar los vuelos en el ComboBox: " + e.getMessage());
        }
        assignedFlightComboBox.setItems(flights);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}