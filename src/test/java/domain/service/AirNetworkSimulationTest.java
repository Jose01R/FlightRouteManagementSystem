package domain.service;

import data.RouteData;
import domain.common.Airport;
import domain.common.Route;
import domain.graph.GraphException;
import domain.linkedlist.DoublyLinkedList; // Keep if needed for other tests/assertions
import domain.linkedlist.ListException;
import domain.linkedlist.Node; // Keep if needed for other tests/assertions
import domain.linkedlist.SinglyLinkedList; // Keep if needed for other tests/assertions
import data.AirportsData; // For cleanup
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList; // Needed for list manipulations
import java.util.List;
import java.util.Map;
import java.util.Objects; // Needed for Objects.requireNonNull

import static org.junit.jupiter.api.Assertions.*; // Import all static assertion methods

class AirNetworkSimulationTest {

    private static final String AIRPORTS_FILE_NAME = "airports.json";
    private static final String ROUTES_FILE_NAME = "routes.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    // Service instances will be created in BeforeEach for test isolation
    private AirportService airportService;
    private RouteData routeData;
    private AirNetworkService airNetworkService;

    // Paths for direct file access in setup/teardown
    private Path airportsFilePath;
    private Path routesFilePath;

    @BeforeEach
    void setUp() throws IOException {
        airportsFilePath = Utility.getFilePath(DATA_DIRECTORY, AIRPORTS_FILE_NAME);
        routesFilePath = Utility.getFilePath(DATA_DIRECTORY, ROUTES_FILE_NAME);

        // Ensure directories exist
        Files.createDirectories(airportsFilePath.getParent());
        Files.createDirectories(routesFilePath.getParent());

        // Clean up files before each test to ensure a clean state
        if (Files.exists(airportsFilePath)) {
            Files.writeString(airportsFilePath, "[]"); // Write empty JSON array
            System.out.println("\n--- Setup: Cleared " + AIRPORTS_FILE_NAME + " before test. ---");
        }
        if (Files.exists(routesFilePath)) {
            Files.writeString(routesFilePath, "{}"); // Write empty JSON object for routes map
            System.out.println("--- Setup: Cleared " + ROUTES_FILE_NAME + " before test. ---");
        }

        // Initialize services AFTER files are cleaned, so they load fresh data
        airportService = new AirportService();
        routeData = new RouteData();
        // airNetworkService will be initialized later in the test or its constructor will load clean data
    }

//    @AfterEach
//    void tearDown() throws IOException {
//        // Clean up files after each test
//        if (Files.exists(airportsFilePath)) {
//            Files.writeString(airportsFilePath, "[]");
//            System.out.println("--- Teardown: Cleared " + AIRPORTS_FILE_NAME + " after test. ---");
//        }
//        if (Files.exists(routesFilePath)) {
//            Files.writeString(routesFilePath, "{}");
//            System.out.println("--- Teardown: Cleared " + ROUTES_FILE_NAME + " after test. ---");
//        }
//    }

    @Test
    void testFullSimulationFlow() throws IOException, ListException, GraphException { // Removed InterruptedException, usually not relevant here

        System.out.println("\n--- Iniciando Simulación de Red Aérea ---\n");

        // --- PASO 1: Crear y Persistir Aeropuertos ---
        System.out.println("-> Creando y guardando aeropuertos en airports.json...");
        Airport sjo = new Airport(101, "Juan Santamaria Intl Airport", "Costa Rica", "Active");
        Airport pty = new Airport(202, "Tocumen Intl Airport", "Panama", "Active");
        Airport lax = new Airport(303, "Los Angeles Intl Airport", "USA", "Active");
        Airport mia = new Airport(404, "Miami Intl Airport", "USA", "Active");
        Airport bog = new Airport(505, "El Dorado Intl Airport", "Colombia", "Active");
        Airport mad = new Airport(606, "Adolfo Suarez Madrid-Barajas Airport", "Spain", "Active");

        // Use airportService to create and persist airports. Handles internal map and JSON.
        // It's good to use the service methods to ensure proper data management.
        assertTrue(airportService.createAirport(sjo), "Should create SJO airport");
        assertTrue(airportService.createAirport(pty), "Should create PTY airport");
        assertTrue(airportService.createAirport(lax), "Should create LAX airport");
        assertTrue(airportService.createAirport(mia), "Should create MIA airport");
        assertTrue(airportService.createAirport(bog), "Should create BOG airport");
        assertTrue(airportService.createAirport(mad), "Should create MAD airport");
        System.out.println("Aeropuertos añadidos/verificados en airports.json.\n");

        // --- PASO 2: Crear y Persistir Rutas ---
        // Now that airports are persisted, we can create AirNetworkService.
        // Its constructor will load the *existing* airports into its graph.
        airNetworkService = new AirNetworkService(airportService, routeData);
        System.out.println("-> Creando y guardando rutas en routes.json a través de AirNetworkService...");

        // Define routes and add them using airNetworkService.addRoute().
        // This method adds to the internal map, adds edges to the graph, and saves to JSON.
        Route route1 = new Route("CM100", sjo.getCode(), pty.getCode(), "Copa Airlines", 1.5, 800, 250.0, LocalTime.of(8, 0), LocalTime.of(9, 30));
        Route route2 = new Route("CM200", pty.getCode(), lax.getCode(), "Copa Airlines", 7.0, 4800, 500.0, LocalTime.of(10, 0), LocalTime.of(17, 0));
        Route route3 = new Route("AA300", sjo.getCode(), mia.getCode(), "American Airlines", 3.0, 1500, 350.0, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Route route4 = new Route("DL400", mia.getCode(), lax.getCode(), "Delta Airlines", 5.5, 3900, 400.0, LocalTime.of(13, 0), LocalTime.of(18, 30));
        Route route5 = new Route("AV500", pty.getCode(), bog.getCode(), "Avianca", 1.0, 700, 100.0, LocalTime.of(14, 0), LocalTime.of(15, 0));
        Route route6 = new Route("IB600", bog.getCode(), mad.getCode(), "Iberia", 9.0, 8000, 700.0, LocalTime.of(20, 0), LocalTime.of(11, 0));
        Route route7 = new Route("UA700", lax.getCode(), sjo.getCode(), "United Airlines", 6.0, 4000, 450.0, LocalTime.of(19,0), LocalTime.of(1,0));

        // Use addRoute to add them to the service, which also adds them to the graph and saves to file
        assertTrue(airNetworkService.addRoute(route1), "Should add route CM100");
        assertTrue(airNetworkService.addRoute(route2), "Should add route CM200");
        assertTrue(airNetworkService.addRoute(route3), "Should add route AA300");
        assertTrue(airNetworkService.addRoute(route4), "Should add route DL400");
        assertTrue(airNetworkService.addRoute(route5), "Should add route AV500");
        assertTrue(airNetworkService.addRoute(route6), "Should add route IB600");
        assertTrue(airNetworkService.addRoute(route7), "Should add route UA700");
        System.out.println("Rutas añadidas/verificadas en routes.json y en el grafo.\n");

        // At this point, the airNetworkService instance already has the graph populated
        // because its constructor calls loadRoutesAndNetwork(), which reads the now-populated files.
        System.out.println("--- Estado actual del Grafo de Rutas (después de inicialización y adición) ---\n");
        System.out.println(airNetworkService); // This prints the graph content

        // --- PASO 3: Realizar Búsquedas de Rutas Más Cortas (Dijkstra) ---
        System.out.println("\n--- Realizando Búsquedas de Rutas Más Cortas (Dijkstra) ---\n");

        // Ejemplo 1: SJO a LAX por Duración
        System.out.println("Buscando ruta más corta (duración) de SJO (101) a LAX (303):");
        List<Integer> path1 = airNetworkService.findShortestRoute(sjo.getCode(), lax.getCode(), "duration");
        assertNotNull(path1, "Path from SJO to LAX should not be null");
        assertFalse(path1.isEmpty(), "Path from SJO to LAX should not be empty");
        System.out.println("Ruta encontrada: " + path1);
        System.out.println("----------------------------------------\n");

        // Ejemplo 2: SJO a MAD por Precio
        System.out.println("Buscando ruta más corta (precio) de SJO (101) a MAD (606):");
        List<Integer> path2 = airNetworkService.findShortestRoute(sjo.getCode(), mad.getCode(), "price");
        assertNotNull(path2, "Path from SJO to MAD should not be null");
        assertFalse(path2.isEmpty(), "Path from SJO to MAD should not be empty");
        System.out.println("Ruta encontrada: " + path2);
        // Assert that the path contains expected airports if you know the exact path.
        // For example: assertTrue(path2.contains(sjo.getCode()) && path2.contains(bog.getCode()) && path2.contains(mad.getCode()));
        System.out.println("----------------------------------------\n");

        // Ejemplo 3: PTY a MIA (ahora se espera ruta indirecta)
        System.out.println("Buscando ruta más corta (distancia) de PTY (202) a MIA (404):");
        List<Integer> path3 = airNetworkService.findShortestRoute(pty.getCode(), mia.getCode(), "distance");
        assertNotNull(path3, "Path from PTY to MIA should NOT be null (indirect route expected)");
        assertFalse(path3.isEmpty(), "Path from PTY to MIA should not be empty");
        assertEquals(List.of(202, 303, 101, 404), path3, "The path from PTY to MIA should be [202, 303, 101, 404]");
        System.out.println("Ruta encontrada: " + path3);
        System.out.println("----------------------------------------\n");

        // Ejemplo 4: MIA a SJO (existencia de ruta)
        boolean containsMIAtoSJO = airNetworkService.containsRoute(mia.getCode(), sjo.getCode());
        System.out.println("Verificando si existe ruta directa de MIA (404) a SJO (101): " + containsMIAtoSJO);
        assertFalse(containsMIAtoSJO, "Should not have a direct route from MIA to SJO"); // Adjust this assertion based on your actual route definitions
        System.out.println("----------------------------------------\n");


        // --- PASO 4: Eliminar un Aeropuerto y ver el impacto ---
        System.out.println("\n-> Eliminando el aeropuerto BOG (505) y sus rutas del grafo...");

        // First, remove from graph (which also updates its internal routes map and saves)
        airNetworkService.removeAirportFromGraph(bog.getCode());
        // Then, remove from AirportService (which removes from its DLL and saves its file)
        boolean bogDeletedFromAirportService = airportService.deleteAirport(bog.getCode());
        assertTrue(bogDeletedFromAirportService, "Aeropuerto BOG (505) debería ser eliminado del airports.json.");

        System.out.println("Aeropuerto BOG (505) eliminado del airports.json y del grafo en memoria y sus aristas.");
        System.out.println("\n--- Estado del Grafo después de eliminar BOG ---\n");
        System.out.println(airNetworkService);

        // Intenta buscar la ruta SJO a MAD de nuevo
        System.out.println("Buscando ruta más corta (precio) de SJO (101) a MAD (606) después de eliminar BOG:");
        // This path should now be null, as BOG was the intermediate airport for SJO -> BOG -> MAD
        List<Integer> pathAfterRemoval = airNetworkService.findShortestRoute(sjo.getCode(), mad.getCode(), "price");
        assertNull(pathAfterRemoval, "Ruta SJO a MAD debería ser nula después de eliminar BOG");
        System.out.println("No se encontró ruta de SJO a MAD (como se esperaba si BOG era crítico).");
        System.out.println("----------------------------------------\n");


        System.out.println("\n--- Simulación de Red Aérea Finalizada ---");
    }

}