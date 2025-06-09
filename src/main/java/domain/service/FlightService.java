package domain.service;

import data.FlightData;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.CircularDoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List; // Necesario para la conversión a Collection

public class FlightService {
    private FlightData flightData; // Actúa como una utilidad de E/S
    private CircularDoublyLinkedList flightList; // ¡La lista enlazada es la estructura en memoria!

    public FlightService(FlightData flightData) {
        this.flightData = flightData;
        this.flightList = new CircularDoublyLinkedList(); // Inicializa la lista enlazada

        // Carga el Map de FlightData y lo convierte a CircularDoublyLinkedList
        Map<Integer, Flight> loadedMap = flightData.loadFlightsToMap();
        for (Flight flight : loadedMap.values()) {
            // Inicia el bloque try
            this.flightList.add(flight); // Añade cada vuelo cargado a la lista enlazada
        }
    }

    // Getter para la lista enlazada (útil para controladores)
    public CircularDoublyLinkedList getFlightList() {
        return flightList;
    }

    // Método para crear un vuelo, opera sobre la lista enlazada
    public boolean createFlight(Flight flight) throws ListException {
        if (findFlightByNumber(flight.getNumber()) != null) {
            throw new ListException("Vuelo con número " + flight.getNumber() + " ya existe.");
        }
        this.flightList.add(flight); // Añade directamente a la lista enlazada
        return true;
    }

    public Flight findFlightByNumber(int flightNumber) {
        try {
            if (flightList.isEmpty()) { // Primero, verifica si la lista está vacía de forma segura
                return null; // Si está vacía, el vuelo no puede existir
            }
            for (int i = 1; i <= flightList.size(); i++) { // Ahora, size() debería ser seguro
                Flight currentFlight = (Flight) flightList.getNode(i).data;
                if (currentFlight.getNumber() == flightNumber) {
                    return currentFlight;
                }
            }
        } catch (ListException e) {
            // Este catch maneja ListException si ocurren en getNode(i) por otras razones (e.g., índice inválido)
            System.err.println("Error al buscar vuelo en la lista enlazada: " + e.getMessage());
            // No relanzar, sino retornar null para indicar que no se encontró o hubo un problema
        }
        return null; // Vuelo no encontrado
    }

    // Dentro de domain.service.FlightService.java

    public boolean assignPassengerToFlight(int flightNumber, Passenger passenger) throws ListException {
        Flight flight = findFlightByNumber(flightNumber);
        if (flight != null) { // Verifica si el vuelo existe
            if (flight.getOccupancy() < flight.getCapacity()) { // Verifica si el vuelo tiene capacidad

                // Asegura que la lista de pasajeros del vuelo esté inicializada
                if (flight.getPasajeros() == null) {
                    flight.setPasajeros(new SinglyLinkedList());
                }

                // Intenta añadir el pasajero a la lista enlazada del vuelo
                flight.getPasajeros().add(passenger);
                flight.incrementOccupancy();

                // --- AÑADE ESTA LÓGICA AQUÍ ---
                // Asegura que la lista de historial de vuelos del pasajero esté inicializada
                if (passenger.getFlightHistory() == null) {
                    // Asumo que el Passenger tiene un método setFlightHistory(SinglyLinkedList)
                    passenger.setFlightHistory(new SinglyLinkedList());
                }
                // Añade el VUELO actual al historial de vuelos del pasajero
                // ¡Es crucial que el historial del pasajero guarde el objeto Flight, no el Passenger!
                passenger.getFlightHistory().add(flight);
                // -----------------------------

                return true; // Retorna true si todo fue exitoso

            } else {
                System.out.println("Vuelo " + flightNumber + " está lleno. No se puede añadir más pasajeros.");
                return false; // Retorna false si el vuelo está lleno
            }
        } else {
            System.out.println("Vuelo " + flightNumber + " no encontrado.");
            return false; // Retorna false si el vuelo no existe
        }
    }

    // Método para guardar datos: convierte la lista enlazada a Map y la pasa a FlightData
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
        flightData.saveFlightsFromMap(flightsToSave); // Pasa el Map a FlightData para guardar
    }

    // Métodos adicionales para operar sobre la CircularDoublyLinkedList
    public boolean updateFlight(Flight updatedFlight) throws ListException {
        // Encontrar el vuelo antiguo por su número
        Flight oldFlight = findFlightByNumber(updatedFlight.getNumber());
        if (oldFlight == null) {
            System.out.println("No se encontró un vuelo con el ID " + updatedFlight.getNumber() + " para actualizar.");
            return false;
        }

        // Eliminar el vuelo antiguo y añadir el nuevo (actualizado)
        // Esto asume que tu CircularDoublyLinkedList tiene un método para remover por índice o por objeto.
        // Si no lo tiene, necesitarías implementarlo o iterar y recrear la lista.
        int index = -1;
        for (int i = 1; i <= flightList.size(); i++) {
            if (((Flight) flightList.getNode(i).data).getNumber() == updatedFlight.getNumber()) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            flightList.remove(index); // Asume que remove(int index) existe
            flightList.add(updatedFlight); // Añade el vuelo actualizado
            System.out.println("Vuelo " + updatedFlight.getNumber() + " actualizado correctamente en lista.");
            return true;
        }
        return false;
    }

    public boolean deleteFlight(int flightNumber) throws ListException {
        Flight flightToDelete = findFlightByNumber(flightNumber);
        if (flightToDelete == null) {
            System.out.println("No se encontró un vuelo con el ID " + flightNumber + " para eliminar.");
            return false;
        }
        // Similar a update, necesitas encontrar el índice o implementar una eliminación por valor
        int index = -1;
        for (int i = 1; i <= flightList.size(); i++) {
            if (((Flight) flightList.getNode(i).data).getNumber() == flightNumber) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            flightList.remove(index); // Asume que remove(int index) existe
            System.out.println("Vuelo " + flightNumber + " eliminado correctamente de lista.");
            return true;
        }
        return false;
    }

    // Helper para obtener todos los vuelos como una Collection (útil para la UI)
    public Collection<Flight> getAllFlightsAsCollection() {
        java.util.ArrayList<Flight> flights = new java.util.ArrayList<>();
        try {
            if (flightList != null && !flightList.isEmpty()) {
                for (int i = 1; i <= flightList.size(); i++) {
                    flights.add((Flight) flightList.getNode(i).data);
                }
            }
        } catch (ListException e) {
            System.err.println("Error al obtener todos los vuelos de CircularDoublyLinkedList: " + e.getMessage());
            e.printStackTrace();
        }
        return flights;
    }
}
