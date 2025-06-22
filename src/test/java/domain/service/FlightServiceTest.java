package domain.service;

import data.FlightData;
import data.PassengerData;
import domain.btree.AVL; // Keep if needed, ensure it exists
import domain.btree.TreeException; // Keep if needed, ensure it exists
import domain.common.*;
import domain.linkedlist.ListException; // Keep if needed, ensure it exists
import domain.linkedlist.SinglyLinkedList; // Keep if needed, ensure it exists
import util.Utility; // For file path utility

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException; // For file operations
import java.nio.file.Files; // For file operations
import java.nio.file.Path;  // For file paths
import java.time.LocalDateTime;
import java.time.LocalTime; // For Route dummy constructor
import java.util.Collection; // For iterating over collections (e.g., Map values)

// Removed all static assert imports as per requirement

class FlightServiceTest {
    FlightService flightService;
    PassengerService passengerService;
    FlightData flightData;
    PassengerData passengerData;

    // Define los nombres de los archivos para limpieza
    private static final String FLIGHT_FILE_NAME = "flights.json"; // Assuming flight data is in flights.json
    private static final String PASSENGERS_FILE_NAME = "passengers.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; // Consistent with previous tests

    private Path flightsFilePath;
    private Path passengersFilePath;

    // Dummy service implementations to satisfy FlightService constructor
    // In a real scenario, you'd use your actual implementations or mock frameworks
    private AirplaneService dummyAirplaneService;
    private AirNetworkService dummyAirNetworkService;
    private AirportService dummyAirportService;


    @BeforeEach
    void setUp() throws IOException {
        // Initialize Paths
        flightsFilePath = Utility.getFilePath(DATA_DIRECTORY, FLIGHT_FILE_NAME);
        passengersFilePath = Utility.getFilePath(DATA_DIRECTORY, PASSENGERS_FILE_NAME);

        // Ensure directories exist
        Files.createDirectories(flightsFilePath.getParent());

        // Clean up files before each test to ensure a clean state
        System.out.println("\n--- Setup: Cleaning up files before test ---");
        if (Files.exists(flightsFilePath)) {
            Files.writeString(flightsFilePath, "[]"); // Assuming flights are stored as a JSON array
            System.out.println("Cleaned " + FLIGHT_FILE_NAME);
        }
        if (Files.exists(passengersFilePath)) {
            Files.writeString(passengersFilePath, "[]"); // Assuming passengers are stored as a JSON array
            System.out.println("Cleaned " + PASSENGERS_FILE_NAME);
        }
        System.out.println("--- Setup: File cleanup complete ---");

        // Initialize dummy services for FlightService constructor
        this.dummyAirplaneService = new AirplaneService() {
            // Implement minimal methods needed by FlightService if any
            // Or just leave empty if constructor doesn't call methods immediately
        };
        this.dummyAirportService = new AirportService(); // Use your actual AirportService
        this.dummyAirNetworkService = new AirNetworkService(dummyAirportService, new data.RouteData()); // Needs AirportService and RouteData


        // Initialize data services
        this.flightData = new FlightData();
        this.passengerData = new PassengerData();

        // Initialize PassengerService
        this.passengerService = new PassengerService(passengerData);

        // Initialize FlightService with all required dependencies
        // The constructor of FlightService expects AirplaneService, AirNetworkService, AirportService
        this.flightService = new FlightService(this.flightData, this.dummyAirplaneService, this.dummyAirNetworkService, this.dummyAirportService,this.passengerService);

        System.out.println("--- Setup: Services initialized ---");
    }

//    @AfterEach
//    void tearDown() throws IOException {
//        // Clean up files after each test
//        System.out.println("\n--- Teardown: Cleaning up files after test ---");
//        if (Files.exists(flightsFilePath)) {
//            Files.writeString(flightsFilePath, "[]");
//            System.out.println("Cleaned " + FLIGHT_FILE_NAME);
//        }
//        if (Files.exists(passengersFilePath)) {
//            Files.writeString(passengersFilePath, "[]");
//            System.out.println("Cleaned " + PASSENGERS_FILE_NAME);
//        }
//        System.out.println("--- Teardown: File cleanup complete ---");
//    }

    @Test
    void createFlightWithPassengersAndCheckPersistence() throws ListException, TreeException, IOException {
        System.out.println("\n--- Starting test: createFlightWithPassengersAndCheckPersistence ---");

        // --- Registrar Pasajeros ---
        System.out.println("-> Registering passengers...");
        Passenger p1 = new Passenger(201, "Carlos", "Costarricense");
        Passenger p2 = new Passenger(202, "Ana", "Nicaragüense");
        Passenger p3 = new Passenger(203, "Luis", "Panameño");

        boolean p1Registered = passengerService.registerPassenger(p1);
        System.out.println("Passenger " + p1.getId() + " registered: " + (p1Registered ? "SUCCESS" : "FAIL"));
        boolean p2Registered = passengerService.registerPassenger(p2);
        System.out.println("Passenger " + p2.getId() + " registered: " + (p2Registered ? "SUCCESS" : "FAIL"));
        boolean p3Registered = passengerService.registerPassenger(p3);
        System.out.println("Passenger " + p3.getId() + " registered: " + (p3Registered ? "SUCCESS" : "FAIL"));

        // --- Crear Vuelo ---
        System.out.println("\n-> Creating flights...");
        LocalDateTime now = LocalDateTime.now();
        int capacity = 30;

        AirportService airport = new AirportService();

        // Dummy Airplane and Route for Flight constructor
        Airplane testAirplane = new Airplane("TestModel-A1");
        Route testRoute1 = new Route("RT_SJO_PTY", airport.getAirportByCode(11).getCode(), 202, "TestAir", 2.0, 800.0, 250.0, LocalTime.of(8,0), LocalTime.of(10,0));
        Route testRoute2 = new Route("RT_CAN_PTY", 500, 202, "TestAir", 4.0, 2500.0, 400.0, LocalTime.of(12,0), LocalTime.of(16,0));

        // Correct Flight instantiation using the provided constructor
        Flight flight99 = new Flight(99, now, capacity, testAirplane, testRoute1);
        Flight flight100 = new Flight(100, now, capacity, testAirplane, testRoute2);

        boolean flight99Created = flightService.createFlight(flight99);
        System.out.println("Flight " + flight99.getNumber() + " created: " + (flight99Created ? "SUCCESS" : "FAIL"));
        boolean flight100Created = flightService.createFlight(flight100);
        System.out.println("Flight " + flight100.getNumber() + " created: " + (flight100Created ? "SUCCESS" : "FAIL"));

        // --- Asignar Pasajeros al Vuelo ---
        System.out.println("\n-> Assigning passengers to Flight " + flight99.getNumber() + "...");
        boolean p1Assigned = flightService.assignPassengerToFlight(flight99.getNumber(), p1);
        System.out.println("Passenger " + p1.getId() + " assigned to Flight " + flight99.getNumber() + ": " + (p1Assigned ? "SUCCESS" : "FAIL"));
        boolean p2Assigned = flightService.assignPassengerToFlight(flight99.getNumber(), p2);
        System.out.println("Passenger " + p2.getId() + " assigned to Flight " + flight99.getNumber() + ": " + (p2Assigned ? "SUCCESS" : "FAIL"));
        boolean p3Assigned = flightService.assignPassengerToFlight(flight99.getNumber(), p3);
        System.out.println("Passenger " + p3.getId() + " assigned to Flight " + flight99.getNumber() + ": " + (p3Assigned ? "SUCCESS" : "FAIL"));

        // --- Verificar el Estado del Vuelo en Memoria ---
        System.out.println("\n-> Verifying flight state in memory for Flight " + flight99.getNumber() + "...");
        Flight assignedFlightInMemory = flightService.findFlightByNumber(flight99.getNumber());
        System.out.println("Assigned Flight (in memory) found: " + (assignedFlightInMemory != null ? "YES" : "NO"));
        if (assignedFlightInMemory != null) {
            System.out.println("Flight " + assignedFlightInMemory.getNumber() + " passenger count: " + assignedFlightInMemory.getPasajeros().size() + " (Expected: 3)");
            System.out.println("Flight " + assignedFlightInMemory.getNumber() + " occupancy: " + assignedFlightInMemory.getOccupancy() + " (Expected: 3)");

            System.out.println("--- Passengers in Flight " + assignedFlightInMemory.getNumber() + " (in memory): ---");
            try {
                for (int i = 1; i <= assignedFlightInMemory.getPasajeros().size(); i++) {
                    System.out.println(" - " + assignedFlightInMemory.getPasajeros().getNode(i).data);
                }
            } catch (ListException e) {
                System.err.println("Error accessing passenger list in memory: " + e.getMessage());
            }
        }

        // --- Guardar Datos en Archivos (simulando el cierre de la aplicación) ---
        System.out.println("\n-> Saving all data to files...");
        flightService.saveData();
        passengerService.saveData();
        System.out.println("Data saved successfully to " + FLIGHT_FILE_NAME + " and " + PASSENGERS_FILE_NAME + ".");


        // Simula un nuevo inicio de la aplicación cargando los datos desde cero.
        System.out.println("\n-> Simulating application restart and loading data from files...");
        // Re-initialize all dependencies for the new FlightService instance
        AirplaneService newDummyAirplaneService = new AirplaneService() {};
        AirportService newDummyAirportService = new AirportService();
        AirNetworkService newDummyAirNetworkService = new AirNetworkService(newDummyAirportService, new data.RouteData()); // Fresh dependencies

        FlightData newFlightData = new FlightData();
        FlightService newFlightService = new FlightService(newFlightData, newDummyAirplaneService, newDummyAirNetworkService, newDummyAirportService,this.passengerService);

        PassengerData newPassengerData = new PassengerData();
        PassengerService newPassengerService = new PassengerService(newPassengerData);


        // Verificar que el vuelo se cargó correctamente
        System.out.println("\n-> Verifying loaded flight data for Flight " + flight99.getNumber() + "...");
        Flight loadedFlight = newFlightService.findFlightByNumber(flight99.getNumber());
        System.out.println("Loaded Flight " + flight99.getNumber() + " found: " + (loadedFlight != null ? "YES" : "NO"));
        if (loadedFlight != null) {
            System.out.println("Loaded Flight number: " + loadedFlight.getNumber() + " (Expected: 99)");
            System.out.println("Loaded Flight passenger count: " + loadedFlight.getPasajeros().size() + " (Expected: 3)");
            System.out.println("Loaded Flight occupancy: " + loadedFlight.getOccupancy() + " (Expected: 3)");

            System.out.println("--- Passengers in Flight " + loadedFlight.getNumber() + " (loaded from file): ---");
            try {
                for (int i = 1; i <= loadedFlight.getPasajeros().size(); i++) {
                    System.out.println(" - " + loadedFlight.getPasajeros().getNode(i).data);
                }
            } catch (ListException e) {
                System.err.println("Error accessing passenger list from loaded flight: " + e.getMessage());
            }
        }


        // Verificar que los pasajeros se cargaron correctamente y su historial de vuelos
        System.out.println("\n-> Verifying loaded passenger data and flight history...");
        Passenger loadedP1 = newPassengerService.findPassengerById(201);
        System.out.println("Loaded Passenger " + p1.getId() + " found: " + (loadedP1 != null ? "YES" : "NO"));
        if (loadedP1 != null) {
            System.out.println("Loaded Passenger " + loadedP1.getId() + " name: " + loadedP1.getName() + " (Expected: Carlos)");
            System.out.println("Loaded Passenger " + loadedP1.getId() + " flight history size: " + loadedP1.getFlightHistory().size() + " (Expected: 1)");
            try {
                if (!loadedP1.getFlightHistory().isEmpty() && loadedP1.getFlightHistory().getNode(1).data instanceof Flight) {
                    Flight historyFlight = (Flight) loadedP1.getFlightHistory().getNode(1).data;
                    System.out.println("Loaded Passenger " + loadedP1.getId() + " flight history: Contains Flight " + historyFlight.getNumber() + " (Expected: 99)");
                } else {
                    System.out.println("Loaded Passenger " + loadedP1.getId() + " flight history is empty or contains non-Flight data.");
                }
            } catch (ListException e) {
                System.err.println("Error accessing flight history for loaded passenger: " + e.getMessage());
            }
        }

        System.out.println("\n--- Test finished: createFlightWithPassengersAndCheckPersistence ---");
    }
}