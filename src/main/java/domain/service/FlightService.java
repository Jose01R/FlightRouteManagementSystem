package domain.service;

import data.FlightData;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import javafx.collections.FXCollections; // Importación necesaria
import javafx.collections.ObservableList; // Importación necesaria

import java.time.LocalDateTime;
import java.util.Collection; // Sigue siendo útil
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightService {
    private FlightData flightData;
    private CircularDoublyLinkedList flightList;
    private ObservableList<Flight> observableFlights;

    public FlightService(FlightData flightData) {
        this.flightData = flightData;
        this.flightList = new CircularDoublyLinkedList();
        this.observableFlights = FXCollections.observableArrayList();
        loadInitialFlights();
        generateInitialRandomFlights(10);

    }


    public ObservableList<Flight> getObservableFlights() {
        return observableFlights;
    }


    private void loadInitialFlights() {
        try {
            Map<Integer, Flight> loadedMap = flightData.loadFlightsToMap();
            if (loadedMap != null && !loadedMap.isEmpty()) {

                this.flightList = new CircularDoublyLinkedList();
                this.observableFlights.clear();
                for (Flight flight : loadedMap.values()) {

                    this.flightList.add(flight);
                    this.observableFlights.add(flight);
                }
            } else {

                this.flightList = new CircularDoublyLinkedList();
                this.observableFlights.clear();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar vuelos iniciales: " + e.getMessage());
            e.printStackTrace();

            this.flightList = new CircularDoublyLinkedList();
            this.observableFlights.clear();
        }
    }

    public void saveData() {
        Map<Integer, Flight> flightsToSave = new HashMap<>();
        try {
            if (flightList != null && !flightList.isEmpty()) {
                for (int i = 1; i <= flightList.size(); i++) {
                    Flight flight = (Flight) flightList.getNode(i).data;
                    flightsToSave.put(flight.getNumber(), flight);
                }
            }
        } catch (ListException e) {
            System.err.println("Error al preparar vuelos para guardar desde CircularDoublyLinkedList: " + e.getMessage());
            e.printStackTrace();
        }
        flightData.saveFlightsFromMap(flightsToSave);
    }



    public boolean createFlight(Flight flight) throws ListException {
        if (findFlightByNumber(flight.getNumber()) != null) {
            throw new ListException("Vuelo con número " + flight.getNumber() + " ya existe.");
        }
        this.flightList.add(flight);
        this.observableFlights.add(flight);

        return true;
    }

    public Flight findFlightByNumber(int flightNumber) {
        try {
            if (flightList.isEmpty()) {
                return null;
            }
            for (int i = 1; i <= flightList.size(); i++) {
                Flight currentFlight = (Flight) flightList.getNode(i).data;
                if (currentFlight.getNumber() == flightNumber) {
                    return currentFlight;
                }
            }
        } catch (ListException e) {
            System.err.println("Error al buscar vuelo en la lista enlazada: " + e.getMessage());
        }
        return null;
    }

    public boolean assignPassengerToFlight(int flightNumber, Passenger passenger) throws ListException {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado."); // Lanza excepción para mejor manejo
        }

        if (flight.getOccupancy() >= flight.getCapacity()) { // Verifica si el vuelo tiene capacidad
            throw new ListException("Vuelo " + flightNumber + " está lleno. No se puede añadir más pasajeros.");
        }


        if (flight.getPasajeros() == null) {
            flight.setPasajeros(new SinglyLinkedList());
        }

        //Verifica si el pasajero ya está asignado a este vuelo para evitar duplicados en la lista interna del vuelo
        if (flight.getPasajeros().contains(passenger)) {
            throw new ListException("Pasajero " + passenger.getId() + " ya está asignado al vuelo " + flightNumber);
        }

        flight.getPasajeros().add(passenger);//Añade el pasajero a la lista interna del vuelo
        flight.incrementOccupancy();// Incrementa la ocupación

        if (passenger.getFlightHistory() == null) {
            passenger.setFlightHistory(new SinglyLinkedList());
        }

        // verifica que el vuelo no esté ya en el historial para evitar duplicados
        if (!passenger.getFlightHistory().contains(flight)) {
            passenger.getFlightHistory().add(flight.getNumber());
        }

        //Actualiza la vista
        observableFlights.removeIf(f -> f.getNumber() == flight.getNumber()); // Remueve la versión vieja
        observableFlights.add(flight); // Añade la versión actualizada (con ocupación incrementada)

        return true;
    }

    public boolean updateFlight(Flight updatedFlight) throws ListException {
        Flight oldFlight = findFlightByNumber(updatedFlight.getNumber());
        if (oldFlight == null) {
            throw new ListException("No se encontró un vuelo con el número " + updatedFlight.getNumber() + " para actualizar.");
        }

        //actualiza  CircularDoublyLinkedList
        int index = -1;
        for (int i = 1; i <= flightList.size(); i++) {
            if (((Flight) flightList.getNode(i).data).getNumber() == updatedFlight.getNumber()) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            flightList.remove(index);//Elimina el vuelo antiguo
            flightList.add(updatedFlight); // Añade el vuelo actualizado
            System.out.println("Vuelo " + updatedFlight.getNumber() + " actualizado correctamente en lista interna.");

            observableFlights.removeIf(f -> f.getNumber() == updatedFlight.getNumber());
            observableFlights.add(updatedFlight);

            return true;
        }
        return false;
    }

    public boolean deleteFlight(int flightNumber) throws ListException {
        Flight flightToDelete = findFlightByNumber(flightNumber);
        if (flightToDelete == null) {
            throw new ListException("No se encontró un vuelo con el número " + flightNumber + " para eliminar.");
        }

        //elimina la CircularDoublyLinkedList
        int index = -1;
        for (int i = 1; i <= flightList.size(); i++) {
            if (((Flight) flightList.getNode(i).data).getNumber() == flightNumber) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            flightList.remove(index); //Elimina de la lista interna
            System.out.println("Vuelo " + flightNumber + " eliminado correctamente de lista interna.");

            //Actualiza el ObservableList
            observableFlights.removeIf(f -> f.getNumber() == flightNumber);

            return true;
        }
        return false;
    }
    public CircularDoublyLinkedList getFlightList() {
        return flightList;
    }

    public void generateInitialRandomFlights(int count) {
        String[] origins = {"San José", "Managua", "Panamá", "Ciudad de Guatemala", "San Salvador", "Tegucigalpa", "Bogotá"};
        String[] destinations = {"Miami", "Madrid", "Los Ángeles", "Toronto", "París", "Buenos Aires", "Ciudad de México"};

        for (int i = 0; i < count; i++) {
            int number = 1000 + util.Utility.random(9000);

            String origin = origins[util.Utility.random(origins.length)];
            String destination;

            // Asegurarse que el destino sea distinto al origen
            do {
                destination = destinations[util.Utility.random(destinations.length)];
            } while (destination.equals(origin));

            LocalDateTime departureTime = LocalDateTime.now()
                    .plusDays(util.Utility.random(30))
                    .withHour(util.Utility.random(24))
                    .withMinute(util.Utility.random(60));

            int []capacityArray={100,150,200};
            int capacity=capacityArray[util.Utility.random(capacityArray.length)];

            Flight flight = new Flight(number, origin, destination, departureTime, capacity);
            flight.setPasajeros(new SinglyLinkedList());

                if (findFlightByNumber(flight.getNumber()) == null) {
                    flightList.add(flight);
                    observableFlights.add(flight);
                } else {
                    i--; // Evita duplicados: si ya existe el número, repite el ciclo
                }
        }

        System.out.println(count + " vuelos aleatorios generados y agregados a la lista.");
    }

}