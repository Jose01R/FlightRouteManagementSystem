package domain.service;

import data.FlightData;
import domain.common.Airplane;
import domain.common.Flight;
import domain.common.Passenger;
import domain.common.Route;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.LinkedQueue;
import domain.linkedqueue.QueueException;
import domain.linkedstack.LinkedStack;
import domain.linkedstack.StackException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlightService {
    private FlightData flightData;
    private CircularDoublyLinkedList flightList;
    private ObservableList<Flight> observableFlights;
    private AirplaneService airplaneService;
    private AirNetworkService routeService;
    private AirportService airportService;

    public FlightService(FlightData flightData,
                         AirplaneService airplaneService,
                         AirNetworkService routeService,
                         AirportService airportService) {

        this.flightData = Objects.requireNonNull(flightData, "FlightData cannot be null");
        this.airplaneService = Objects.requireNonNull(airplaneService, "AirplaneService cannot be null");
        this.routeService = Objects.requireNonNull(routeService, "AirNetworkService cannot be null");
        this.airportService = Objects.requireNonNull(airportService, "AirportService cannot be null");

        this.flightList = new CircularDoublyLinkedList();
        this.observableFlights = FXCollections.observableArrayList();
        loadInitialFlights();

        // generateInitialRandomFlights(10);
    }

    public ObservableList<Flight> getObservableFlights() {
        return observableFlights;
    }

    private void loadInitialFlights() {
        try {
            Map<Integer, Flight> loadedMap = flightData.loadFlightsToMap();
            if (loadedMap != null && !loadedMap.isEmpty()) {
                for (Flight flight : loadedMap.values()) {
                    this.flightList.add(flight); //Agregamos Flight en la CircularDoublyLinkedList

                }
            }
            // Después de cargar en flightList, copiar a observableFlights
            observableFlights.clear(); // Limpiar por si acaso
            if (!flightList.isEmpty()) {
                for (int i = 0; i < flightList.size(); i++) {
                    Node node = flightList.getNode(i); // Asegúrate que getNode(i) y size() son correctos
                    if (node != null && node.data instanceof Flight) {
                        observableFlights.add((Flight) node.data);
                    } else {
                        System.err.println("Advertencia: Objeto no válido encontrado al cargar vuelos a observable: " + (node != null ? node.data : "null"));
                    }
                }
            }
            System.out.println("Flights loaded into FlightService (CircularDoublyLinkedList): " + flightList.size());
        } catch (ListException e) {
            System.err.println("Error al cargar vuelos iniciales: " + e.getMessage());
            e.printStackTrace();
            this.flightList.clear();
            this.observableFlights.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveData() throws IOException {
        Map<Integer, Flight> flightsToSave = new HashMap<>();
        try {
            if (flightList != null && !flightList.isEmpty()) {

                for (int i = 0; i < flightList.size(); i++) {
                    Node node = flightList.getNode(i);
                    if (node != null && node.data instanceof Flight) {
                        Flight flight = (Flight) node.data;
                        flightsToSave.put(flight.getNumber(), flight);
                    } else {
                        System.err.println("Warning: Found non-Flight object in flightList during save: " + (node != null ? node.data.getClass().getName() : "null"));
                    }
                }
            }
        } catch (ListException e) {
            System.err.println("Error al preparar vuelos para guardar desde CircularDoublyLinkedList: " + e.getMessage());
            e.printStackTrace();
        }
        flightData.saveFlightsFromMap(flightsToSave);
    }

    public boolean createFlight(Flight flight) throws ListException {

        Objects.requireNonNull(flight, "Flight object cannot be null");

        if (findFlightByNumber(flight.getNumber()) != null) {
            throw new ListException("Vuelo con número " + flight.getNumber() + " ya existe.");
        }
        if (flight.getAssignedAirplane() == null || flight.getAssignedRoute() == null) {
            throw new ListException("El vuelo debe tener un avión y una ruta asignados.");
        }
        this.flightList.add(flight); //Agregar a la lista
        this.observableFlights.add(flight);
        try {
            saveData(); //Salvamos la modificacion
        } catch (IOException e) {
            //Rollback if save fails
            try {
                //REMOVEMOS EL VUELO
                flightList.remove(flight);
            } catch (ListException removeEx) {
                System.err.println("CRITICAL: Failed to rollback flight creation after save error: " + removeEx.getMessage());
            }
            observableFlights.remove(flight); //También eliminar de la lista observable
            throw new ListException("Failed to save flight after creation: " + e.getMessage());
        }
        return true;
    }

    public Flight findFlightByNumber(int flightNumber) {
        try {
            if (flightList.isEmpty()) {
                return null;
            }
            for (int i = 0; i < flightList.size(); i++) {
                Node node = flightList.getNode(i);
                if (node != null && node.data instanceof Flight) {
                    Flight currentFlight = (Flight) node.data;
                    if (currentFlight.getNumber() == flightNumber) {
                        return currentFlight;
                    }
                }
            }
        } catch (ListException e) {
            System.err.println("Error al buscar vuelo en la lista enlazada: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean assignPassengerToFlight(int flightNumber, Passenger passenger) throws ListException {

        Objects.requireNonNull(passenger, "Passenger cannot be null");

        Flight flight = findFlightByNumber(flightNumber);

        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado");
        }

        if (flight.getOccupancy() >= flight.getCapacity()) {
            throw new ListException("Vuelo " + flightNumber + " está lleno. No se puede añadir más pasajeros");
        }

        if (flight.getPasajeros() == null) {
            flight.setPasajeros(new SinglyLinkedList());
        }

        if (flight.getPasajeros().contains(passenger)) {
            throw new ListException("Pasajero " + passenger.getId() + " ya está asignado al vuelo " + flightNumber);
        }

        flight.getPasajeros().add(passenger); //Add Passenger a la  SinglyLinkedList
        flight.incrementOccupancy();

        if (passenger.getFlightHistory() == null) {
            passenger.setFlightHistory(new SinglyLinkedList());
        }
        //Almacenamos el número de vuelo, no el objeto Vuelo completo, para evitar referencias circulares
        if (!passenger.getFlightHistory().contains(flight.getNumber())) {
            passenger.getFlightHistory().add(flight.getNumber()); //Add int
        }

        //Actualizamos la ObservableList: eliminar la antigua, agregar la actualizada (esto actualizará la UI)
        observableFlights.removeIf(f -> f.getNumber() == flight.getNumber());
        observableFlights.add(flight);

        try {
            saveData(); //Save changes
        } catch (IOException e) {
            System.err.println("Warning: Failed to save data after assigning passenger to flight: " + e.getMessage());
        }
        return true;
    }

    public boolean updateFlight(Flight updatedFlight) throws ListException {
        Objects.requireNonNull(updatedFlight, "Updated Flight object cannot be null");

        Flight oldFlight = findFlightByNumber(updatedFlight.getNumber());
        if (oldFlight == null) {
            throw new ListException("No se encontró un vuelo con el número " + updatedFlight.getNumber() + " para actualizar.");
        }

        //Actualizamos CircularDoublyLinkedList
        int index = -1;
        for (int i = 0; i < flightList.size(); i++) {
            Node node = flightList.getNode(i);
            if (node != null && node.data instanceof Flight) {
                if (((Flight) node.data).getNumber() == updatedFlight.getNumber()) {
                    index = i;
                    break;
                }
            }
        }
        if (index != -1) {
            flightList.remove(index); //Elimina el antiguo vuelo por índice
            flightList.add(updatedFlight); //Añadimos el vuelo actualizado


            System.out.println("Vuelo " + updatedFlight.getNumber() + " actualizado correctamente en lista interna.");

            //Actualizamos ObservableList
            observableFlights.removeIf(f -> f.getNumber() == updatedFlight.getNumber());
            observableFlights.add(updatedFlight);

            try {
                saveData(); // Save changes
            } catch (IOException e) {
                System.err.println("Warning: Failed to save data after updating flight: " + e.getMessage());
                throw new ListException("Failed to save flight changes after update: " + e.getMessage());
            }
            return true;
        }
        return false; //Should not happen if findFlightByNumber worked
    }

    public boolean deleteFlight(int flightNumber) throws ListException {
        Flight flightToDelete = findFlightByNumber(flightNumber);
        if (flightToDelete == null) {
            throw new ListException("No se encontró un vuelo con el número " + flightNumber + " para eliminar.");
        }

        //Borramos de CircularDoublyLinkedList
        int index = -1;
        for (int i = 0; i < flightList.size(); i++) {
            Node node = flightList.getNode(i);
            if (node != null && node.data instanceof Flight) {
                if (((Flight) node.data).getNumber() == flightNumber) {
                    index = i;
                    break;
                }
            }
        }
        if (index != -1) {
            flightList.remove(index); //Eliminamos de la lista interna
            System.out.println("Vuelo " + flightNumber + " eliminado correctamente de lista interna.");

            // Actualizamos ObservableList
            observableFlights.removeIf(f -> f.getNumber() == flightNumber);

            try {
                saveData(); //Save changes
            } catch (IOException e) {

                flightList.add(flightToDelete);
                observableFlights.add(flightToDelete);
                System.err.println("Warning: Failed to save data after deleting flight: " + e.getMessage());
                throw new ListException("Failed to save flight changes after deletion: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    public CircularDoublyLinkedList getFlightList() {
        return flightList;
    }

    public void generateInitialRandomFlights(int count) {
        if (airplaneService == null || routeService == null) {
            System.err.println("Cannot generate random flights: AirplaneService or AirNetworkService not initialized.");
            return;
        }

        List<Airplane> availableAirplanes = airplaneService.getAllAirplanes();
        List<Route> availableRoutes = routeService.getAllRoutes();

        if (availableAirplanes.isEmpty()) {
            System.err.println("No hay aviones disponibles para generar vuelos. Genere aviones primero.");
            return;
        }
        if (availableRoutes.isEmpty()) {
            System.err.println("No hay rutas disponibles para generar vuelos. Genere rutas primero.");
            return;
        }

        System.out.println("Generating " + count + " random flights...");
        for (int i = 0; i < count; i++) {
            int number = 1000 + util.Utility.random(9000);

            Airplane selectedAirplane = availableAirplanes.get(util.Utility.random(availableAirplanes.size()));
            Route selectedRoute = availableRoutes.get(util.Utility.random(availableRoutes.size()));

            LocalDateTime departureTime = LocalDateTime.now()
                    .plusDays(util.Utility.random(30))
                    .withHour(util.Utility.random(24))
                    .withMinute(util.Utility.random(60));

            int capacity = selectedAirplane.getTotalCapacity(); //Capacidad del avión

            Flight newFlight = new Flight(number, departureTime, capacity, selectedAirplane, selectedRoute);
            newFlight.setPasajeros(new SinglyLinkedList()); //Inicializamos la lista de pasajeros

            try {
                createFlight(newFlight);
            } catch (ListException e) {
                System.err.println("Warning: Could not create random flight " + newFlight.getNumber() + ". Retrying. Error: " + e.getMessage());
                i--; //Decremento para cumplir con el conteo
            }
        }
        System.out.println(count + " random flights generated and added to the list.");
    }

    // ========================================

    /**
     * Simula el embarque de pasajeros para un vuelo determinado desde la cola de embarque del aeropuerto
     *
     * @param flightNumber The number of the flight to board
     * @param airportCode  The code of the airport where boarding is happening
     * @return The number of passengers boarded
     * @throws ListException  If flight or airport not found, or other issues
     * @throws QueueException If there's an issue with the boarding queue
     */
    public int boardPassengers(int flightNumber, int airportCode) throws ListException, QueueException {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado para el abordaje");
        }

        //Obtener la cola de embarque del aeropuerto desde AirportService (devuelve LinkedQueue)
        LinkedQueue boardingQueue = airportService.getAirportBoardingQueue(airportCode);
        if (boardingQueue == null) {
            //happen if getAirportBoardingQueue returns null
            throw new ListException("Aeropuerto " + airportCode + " no tiene cola de abordaje o no existe");
        }

        int boardedCount = 0;
        while (flight.getOccupancy() < flight.getCapacity() && !boardingQueue.isEmpty()) {
            Object dequeuedObj = boardingQueue.deQueue(); //Desencolamos de la cola del aeropuerto
            if (dequeuedObj instanceof Passenger) {
                Passenger passengerToBoard = (Passenger) dequeuedObj;
                try {
                    assignPassengerToFlight(flightNumber, passengerToBoard);
                    boardedCount++;
                    System.out.println("Pasajero " + passengerToBoard.getId() + " embarcado en vuelo " + flightNumber);
                } catch (ListException e) {
                    System.err.println("Error al embarcar pasajero " + passengerToBoard.getId() + " en vuelo " + flightNumber + ": " + e.getMessage());
                }
            } else {
                System.err.println("Warning: Non-Passenger object found in boarding queue: " + (dequeuedObj != null ? dequeuedObj.getClass().getName() : "null"));
            }
        }
        System.out.println("Vuelo " + flightNumber + ": " + boardedCount + " pasajeros embarcados. Ocupación actual: " + flight.getOccupancy());
        try {
            airportService.saveAirports(); //Save airport state
            saveData(); //Guardamos datos de vuelo como ocupación y lista de pasajeros fueron modificados
        } catch (IOException e) {
            System.err.println("Error saving data after boarding passengers: " + e.getMessage());
            throw new ListException("Failed to save data after boarding passengers: " + e.getMessage());
        }
        return boardedCount;
    }

    /**
     * Simula la salida y finalización del vuelo
     * Se le llamará una vez finalizado el embarque
     *
     * @param flightNumber The number of the flight to simulate
     * @throws ListException  If the flight is not found
     * @throws StackException If there's an issue with the airplane's flight history stack
     */
    public void simulateFlightDepartureAndLanding(int flightNumber) throws ListException, StackException {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado para simular despegue/aterrizaje.");
        }

        System.out.println("Simulando despegue del vuelo " + flight.getNumber() +
                " de " + flight.getAssignedRoute().getOriginAirportCode() +
                " a " + flight.getAssignedRoute().getDestinationAirportCode() +
                " con avión " + flight.getAssignedAirplane().getSerialNumber());

        System.out.println("Vuelo " + flight.getNumber() + " en ruta...");
        System.out.println("Vuelo " + flight.getNumber() + " aterrizando en " + flight.getAssignedRoute().getDestinationAirportCode());

        //Vaciar pasajeros del vuelo
        if (flight.getPasajeros() != null) {
            flight.getPasajeros().clear(); //Limpiamos la SinglyLinkedList de passengers para este vuelo
        }
        flight.setOccupancy(0); //Reset occupancy

        //Registrar el vuelo en la pila del avión
        Airplane assignedAirplane = flight.getAssignedAirplane();
        if (assignedAirplane != null) {

            Airplane liveAirplane = airplaneService.findAirplaneBySerialNumber(assignedAirplane.getSerialNumber());
            if (liveAirplane != null) {

                if (liveAirplane.getFlightHistory() == null) {
                    liveAirplane.setFlightHistory(new LinkedStack());
                }
                liveAirplane.getFlightHistory().push(flight); //Push el Flight completo
                System.out.println("Vuelo " + flight.getNumber() + " registrado en el historial del avión " + liveAirplane.getSerialNumber());
                airplaneService.saveAirplanes(); //Save airplane state after modifying its history
            } else {
                System.err.println("Warning: Live airplane " + assignedAirplane.getSerialNumber() + " not found to update history.");
            }
        }

        flight.setCompleted(true);

        //Update the ObservableList and save flight data
        observableFlights.removeIf(f -> f.getNumber() == flight.getNumber());
        observableFlights.add(flight); //Añadir el vuelo actualizado (ahora completado, vacío)
        try {
            saveData(); // Save flight state
        } catch (IOException e) {
            System.err.println("Error saving flight data after simulation: " + e.getMessage());
            throw new ListException("Failed to save flight data after simulation: " + e.getMessage());
        }
        System.out.println("Vuelo " + flight.getNumber() + " completado. Pasajeros vaciados y vuelo registrado en historial del avión.");
    }
}