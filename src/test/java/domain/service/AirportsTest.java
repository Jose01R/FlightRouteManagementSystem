package domain.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.common.Airport;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static domain.service.AirportService.*; // Still need these constants
import data.AirportsData;   // for file path in setup and verification
import util.Utility; // Ensure Utility is imported for getFilePath

class AirportsTest {

    private AirportService airportService; // Declare the service instance
    private static final String FILE_NAME = "airports.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    @BeforeEach
    void setUp() {
        try {
            Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("\n--- Setup: Cleared " + FILE_NAME + " before test. ---");
            }
            Files.createDirectories(filePath.getParent()); // Ensure directory exists
            // Create an empty JSON array if the file was just deleted,
            // so subsequent reads don't fail if the file is truly empty.
            Files.writeString(filePath, "[]");

            // --- Instantiate AirportService here ---
            // AirportService depends on AirportData, so ensure AirportData methods
            // are configured correctly to read/write from/to the specified file path.
            this.airportService = new AirportService(); // Initialize the service

        } catch (IOException e) {
            System.err.println("Error during test setup (cleaning file or initializing service): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Test setup failed", e);
        }
    }

    @Test
    void testAirportCrudOperationsAndListMethods() {
        System.out.println("--- Starting testAirportCrudOperationsAndListMethods ---");
        try {
            // --- 1. Initial Airport Creation ---
            Airport airport1 = new Airport(11, "Aeropuerto de Dortmund", "Alemania", "Active");
            Airport airport2 = new Airport(12, "Aeropuerto de Aalborg", "Dinamarca", "Active");
            airport1.getDeparturesBoard().add("hola");
            airport1.getDeparturesBoard().add("adios");
            airport2.getDeparturesBoard().add("chao");
            airport2.getDeparturesBoard().add("goodbye");

            System.out.println("\n--- Creating Initial Airports via AirportService ---");
            System.out.println("Result of creating airport 11: " + airportService.createAirport(airport1));
            System.out.println("Result of creating airport 12: " + airportService.createAirport(airport2));
            System.out.println("Expected: true for both if unique.");

            // --- 2. Read and Print All Airports After Initial Creation ---
            System.out.println("\n--- Aeropuertos leídos desde archivo después de la creación inicial: ---");
             // Helper still reads directly for verification

            // --- 3. Delete an Airport ---
            System.out.println("\n--- Eliminando Aeropuerto con ID=11 via AirportService ---");
            boolean deleted = airportService.deleteAirport(11);
            System.out.println("Se eliminó el aeropuerto con el ID=11? " + deleted);
            System.out.println("Expected: true");

            // --- 4. Read and Print All Airports After Deletion ---
            System.out.println("\n--- Aeropuertos leídos desde archivo, después de eliminar un Aeropuerto: ---");
            

            // --- 5. Change Status of an Airport ---
            System.out.println("\n--- Cambiando el status del Aeropuerto con ID=12 via AirportService ---");
            boolean statusChanged = airportService.changeAirportStatus(12);
            System.out.println("Se cambió el status del aeropuerto con el ID=12? " + statusChanged);
            System.out.println("Expected: true");

            // --- 6. Read and Print All Airports After Status Change ---
            System.out.println("\n--- Aeropuertos leídos desde archivo, después de cambiar el status: ---");
            

            // --- 7. Create Another Airport (Inactive) ---
            Airport airport3 = new Airport(13, "Aeropuerto de La Coruña", "España", "Inactive");
            airport3.getDeparturesBoard().add("frances");
            airport3.getDeparturesBoard().add("español");

            System.out.println("\n--- Creando Aeropuerto con ID=13 (Inactive) via AirportService ---");
            System.out.println("Result of creating airport 13: " + airportService.createAirport(airport3));
            System.out.println("Expected: true");

            // --- 8. Read and Print All Airports After Adding Airport 13 ---
            System.out.println("\n--- Aeropuertos leídos desde archivo después de añadir el Aeropuerto 13: ---");
            

            // --- 9. Change Status of multiple Airports (including 12 again) ---
            System.out.println("\n--- Cambiando el status de Aeropuerto 12 y 13 via AirportService ---");
            System.out.println("Se cambió el status del aeropuerto con el ID=12? " + airportService.changeAirportStatus(12));
            System.out.println("Se cambió el status del aeropuerto con el ID=13? " + airportService.changeAirportStatus(13));
            System.out.println("Expected: true for both");

            // --- 10. Read and Print All Airports After Multiple Status Changes ---
            System.out.println("\n--- Aeropuertos leídos desde archivo, después de múltiples cambios de status: ---");
            

            // --- 11. Edit an Airport ---
            Airport updatedAirport13 = new Airport(13, "Aeropuerto de Agen-La Garenne", "Francia", "Inactive");
            updatedAirport13.getDeparturesBoard().add("new schedule 1");
            updatedAirport13.getDeparturesBoard().add("new schedule 2");

            System.out.println("\n--- Editando Aeropuerto con ID=13 via AirportService ---");
            System.out.println("Se editó el aeropuerto con el ID=13? " + airportService.updateAirport(updatedAirport13));
            System.out.println("Expected: true");

            // --- 12. Read and Print All Airports After Edit ---
            System.out.println("\n--- Aeropuertos leídos desde archivo, después de editar un Aeropuerto: ---");
            

            // --- 13. Test listAirports (e.g., "Inactive" status) ---
            System.out.println("\n--- Listando Aeropuertos con Status 'Inactive' via AirportService ---");
            // Assuming airportService.listAirports returns SinglyLinkedList
            ArrayList<Object> inactiveAirports = airportService.getAirportsByStatus("Inactive");
            System.out.println("Aeropuertos 'Inactive' encontrados:");
            System.out.println(inactiveAirports.toString());
            System.out.println("Expected: Airport 13 (Agen-La Garenne) should be here.");

            // --- 14. Test listAirports (e.g., "Active" status) ---
            System.out.println("\n--- Listando Aeropuertos con Status 'Active' via AirportService ---");
            ArrayList<Object> activeAirports = airportService.getAirportsByStatus("Active");
            System.out.println("Aeropuertos 'Active' encontrados:");
            System.out.println(activeAirports.toString());
            System.out.println("Expected: Airport 12 (Aalborg) should be here.");

        } catch (ListException e) {
            System.err.println("Test failed with an exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("--- Finished testAirportCrudOperationsAndListMethods ---");
    }


    @Test
    void testCreateMultipleAirports() {
        System.out.println("--- Starting testCreateMultipleAirports (Bulk Creation) via AirportService ---");
        Airport airport1 = new Airport(11, "Aeropuerto de Dortmund", "Alemania", "Active");
        Airport airport2 = new Airport(12, "Aeropuerto de Aalborg", "Dinamarca", "Active");
        Airport airport3 = new Airport(13, "Aeropuerto de La Coruña", "España", "Inactive");
        Airport airport4 = new Airport(14, "Aeropuerto de Marsella-Provenza", "Francia", "Active");
        Airport airport5 = new Airport(15, "Aeropuerto de Oporto", "Portugal", "Active");
        Airport airport6 = new Airport(16, "Aeropuerto de Zúrich", "Suiza", "Inactive");
        Airport airport7 = new Airport(17, "Aeropuerto de Milán-Malpensa", "Italia", "Active");
        Airport airport8 = new Airport(18, "Aeropuerto de Bruselas", "Bélgica", "Inactive");
        Airport airport9 = new Airport(19, "Aeropuerto de Estocolmo-Arlanda", "Suecia", "Active");
        Airport airport10 = new Airport(20, "Aeropuerto de Viena-Schwechat", "Austria", "Active");
        Airport airport11 = new Airport(21, "Aeropuerto de Praga", "Chequia", "Inactive");
        Airport airport12 = new Airport(22, "Aeropuerto de Ámsterdam-Schiphol", "Países Bajos", "Active");
        Airport airport13 = new Airport(23, "Aeropuerto de Helsinki-Vantaa", "Finlandia", "Active");
        Airport airport14 = new Airport(24, "Aeropuerto de Dublín", "Irlanda", "Active");
        Airport airport15 = new Airport(25, "Aeropuerto de Oslo-Gardermoen", "Noruega", "Inactive");
        Airport airport16 = new Airport(26, "Aeropuerto de Bucarest-Henri Coandă", "Rumanía", "Active");
        Airport airport17 = new Airport(27, "Aeropuerto de Budapest-Ferenc Liszt", "Hungría", "Inactive");
        Airport airport18 = new Airport(28, "Aeropuerto de Varsovia-Chopin", "Polonia", "Active");
        Airport airport19 = new Airport(29, "Aeropuerto de Copenhague-Kastrup", "Dinamarca", "Active");
        Airport airport20 = new Airport(30, "Aeropuerto de Sofía", "Bulgaria", "Inactive");

        Airport[] airports = {airport1, airport2, airport3, airport4, airport5, airport6, airport7, airport8, airport9, airport10,
                airport11, airport12, airport13, airport14, airport15, airport16, airport17, airport18, airport19, airport20};

        System.out.println("\n--- Bulk Creating Airports via AirportService ---");
        for (Airport airport : airports) {
            try {
                boolean created = airportService.createAirport(airport);
                System.out.println("Created Airport " + airport.getCode() + " (" + airport.getName() + "): " + created);
            } catch (ListException e) {
                System.err.println("Error creating airport " + airport.getCode() + ": " + e.getMessage());
            }
        }

        System.out.println("\n--- All Airports After Bulk Creation: ---");
        System.out.println(airportService.getAllAirports());
        //

        System.out.println("--- Finished testCreateMultipleAirports ---");

        try {
            System.out.println("AIRPORT 11 IS: " + airportService.getAirportByCountry("Alemania"));
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }

    
}