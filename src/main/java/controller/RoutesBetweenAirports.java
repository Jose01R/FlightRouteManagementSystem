package controller;

import domain.common.*;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.service.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class RoutesBetweenAirports
{
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
    @FXML
    private TableColumn<Flight,String>airPlaneIdColumn;
    @javafx.fxml.FXML
    private TextField searchByOrigenTf;
    @javafx.fxml.FXML
    private TextField searchByArrivalTf;


    private FlightService flightService;
    private AirportService airportService;
    private AirplaneService airplaneService;
    private AirNetworkService airNetworkService;
    private CircularDoublyLinkedList circularDoublyLinkedList;
    private ScheduledExecutorService scheduler;
    private Map<Integer, String> airportCodeToName = new HashMap<>();

    private Stage stage;

    @javafx.fxml.FXML
    public void initialize() {

        flightTableStatusColumn.setCellValueFactory(cellData -> {
            boolean completed = cellData.getValue().isCompleted();
            String status = completed ? "Completed" : "Active";
            return new ReadOnlyStringWrapper(status);
        });

        airPlaneIdColumn.setCellValueFactory(cellData -> {
            Airplane airplane = cellData.getValue().getAssignedAirplane();
            if (airplane != null) {
                String info = airplane.getSerialNumber() + " (" + airplane.getModel() + ")";
                return new SimpleStringProperty(info);
            } else {
                return new SimpleStringProperty("Without airplane");
            }
        });

        flightTableNumberColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNumber()).asObject());
        flightTableOriginColumn.setCellValueFactory(cell -> {
            Flight flight = cell.getValue();
            int code = flight.getAssignedRoute().getOriginAirportCode();
            String name = getAirportNameByCode(code);
            return new SimpleStringProperty(code + " - " + name);
        });
        flightTableDestinationColumn.setCellValueFactory(cell -> {
            Flight flight = cell.getValue();
            int code = flight.getAssignedRoute().getDestinationAirportCode();
            String name = getAirportNameByCode(code);
            return new SimpleStringProperty(code + " - " + name);
        });
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


        searchByOrigenTf.textProperty().addListener((obs, oldText, newText) -> {
            reorderFlightTableView(searchByOrigenTf.getText(), searchByArrivalTf.getText());
        });

        searchByArrivalTf.textProperty().addListener((obs, oldText, newText) -> {
            reorderFlightTableView(searchByOrigenTf.getText(), searchByArrivalTf.getText());
        });

    }

    private String getAirportNameByCode(int code) {
        return airportCodeToName.getOrDefault(code, "Unknown");
    }

    public void setServices(PassengerService passengerService, FlightService flightService, AirplaneService airplaneService, AirNetworkService airNetworkService, AirportService airportService) {
        //this.passengerService = passengerService;
        this.flightService = flightService;
        this.airplaneService=airplaneService;
        this.airNetworkService= airNetworkService;
        this.airportService= airportService;
//        passengerTable.setItems(this.passengerService.getObservablePassengers());
//        assignedFlightComboBox.setItems(this.flightService.getObservableFlights());
        flightTable.setItems(this.flightService.getObservableFlights());
//        routeComboBox.setItems(airNetworkService.getObservableRoutes());
//        airplaneComboBox.setItems(airplaneService.getObservableAirplanes());

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

    public void close(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void exit(ActionEvent actionEvent) {
        stage.close();
    }


}