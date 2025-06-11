package domain.service;

import data.FlightData;
import data.PassengerData;
import domain.btree.AVL; // Asegúrate de que esta clase exista y sea compatible con PassengerService
import domain.btree.TreeException; // Asegúrate de que esta excepción exista
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.ListException; // Asegúrate de que esta excepción exista
import domain.linkedlist.SinglyLinkedList; // Asegúrate de que esta clase exista
import domain.service.FlightService;
import domain.service.PassengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File; // Para la limpieza de archivos
import java.time.LocalDateTime;
import java.util.Collection; // Para iterar sobre los valores del Map

import static org.junit.jupiter.api.Assertions.*;

class FlightServiceTest {
    FlightService flightService;
    PassengerService passengerService;
    FlightData flightData;
    PassengerData passengerData;

    // Define los nombres de los archivos para limpieza
    private static final String FLIGHT_FILE = "flight.json";
    private static final String PASSENGERS_FILE = "passengers.json";

    @BeforeEach
    void setUp() {

        File flightFile = new File(FLIGHT_FILE);
        if (flightFile.exists()) {
            flightFile.delete();
        }
        File passengerFile = new File(PASSENGERS_FILE);
        if (passengerFile.exists()) {
            passengerFile.delete();
        }


        this.flightData = new FlightData();
        this.flightService = new FlightService(flightData);

        this.passengerData = new PassengerData();
        this.passengerService = new PassengerService(passengerData);
    }

    @Test
    void createFlightWithPassengersAndCheckPersistence() throws ListException, TreeException {
        // --- Registrar Pasajeros ---
        Passenger p1 = new Passenger(201, "Carlos", "Costarricense");
        Passenger p2 = new Passenger(202, "Ana", "Nicaragüense");
        Passenger p3 = new Passenger(203, "Luis", "Panameño");

        assertTrue(passengerService.registerPassenger(p1), "Debería registrar p1 exitosamente.");
        assertTrue(passengerService.registerPassenger(p2), "Debería registrar p2 exitosamente.");
        assertTrue(passengerService.registerPassenger(p3), "Debería registrar p3 exitosamente.");

        // --- Crear Vuelo ---
        LocalDateTime now = LocalDateTime.now();
        Flight flight = new Flight(99, "San José", "Ciudad de Panamá", now, 30);
        Flight flight2 = new Flight(100, "Canada", "Ciudad de Panamá", now, 30);
        assertTrue(flightService.createFlight(flight), "Debería crear el vuelo 99 exitosamente.");
        assertTrue(flightService.createFlight(flight2), "Debería crear el vuelo 99 exitosamente.");
        // ---  Asignar Pasajeros al Vuelo ---
        assertTrue(flightService.assignPassengerToFlight(99, p1), "Debería asignar p1 al vuelo 99.");
        assertTrue(flightService.assignPassengerToFlight(99, p2), "Debería asignar p2 al vuelo 99.");
        assertTrue(flightService.assignPassengerToFlight(99, p3), "Debería asignar p3 al vuelo 99.");

        // --- Verificar el Estado del Vuelo en Memoria ---
        Flight assignedFlight = flightService.findFlightByNumber(99);
        assertNotNull(assignedFlight, "El vuelo asignado no debe ser nulo.");
        assertEquals(3, assignedFlight.getPasajeros().size(), "El vuelo debe tener 3 pasajeros.");
        assertEquals(3, assignedFlight.getOccupancy(), "La ocupación del vuelo debe ser 3.");

        System.out.println("--- Pasajeros en vuelo 99 (en memoria): ---");
        for (int i = 1; i <= assignedFlight.getPasajeros().size(); i++) {
            System.out.println(" - " + assignedFlight.getPasajeros().getNode(i).data);
        }

        // --- Guardar Datos en Archivos (simulando el cierre de la aplicación) ---

        flightService.saveData();
        passengerService.saveData();


        // Simula un nuevo inicio de la aplicación cargando los datos desde cero.
        FlightData newFlightData = new FlightData();
        FlightService newFlightService = new FlightService(newFlightData);

        PassengerData newPassengerData = new PassengerData();
        PassengerService newPassengerService = new PassengerService(newPassengerData);

        // Verificar que el vuelo se cargó correctamente
        Flight loadedFlight = newFlightService.findFlightByNumber(99);
        assertNotNull(loadedFlight, "El vuelo cargado después de guardar no debe ser nulo.");
        assertEquals(99, loadedFlight.getNumber(), "El número de vuelo debe ser 99.");
        assertEquals(3, loadedFlight.getPasajeros().size(), "El vuelo cargado debe tener 3 pasajeros.");
        assertEquals(3, loadedFlight.getOccupancy(), "La ocupación del vuelo cargado debe ser 3.");

        System.out.println("\n--- Pasajeros en vuelo 99 (cargado desde archivo): ---");
        for (int i = 1; i <= loadedFlight.getPasajeros().size(); i++) {
            System.out.println(" - " + loadedFlight.getPasajeros().getNode(i).data);
        }

        // Verificar que los pasajeros se cargaron correctamente
        Passenger loadedP1 = newPassengerService.findPassengerById(201);
        assertNotNull(loadedP1, "El pasajero p1 cargado no debe ser nulo.");
        assertEquals("Carlos", loadedP1.getName(), "El nombre del pasajero p1 debe ser Carlos.");
        assertEquals(1, loadedP1.getFlightHistory().size(), "El historial de vuelos de p1 debe contener 1 vuelo.");
        assertEquals(99, ((Flight)loadedP1.getFlightHistory().getNode(1).data).getNumber(), "El vuelo en el historial de p1 debe ser el 99.");

    }


}