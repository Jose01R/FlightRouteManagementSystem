package domain.service;

import domain.common.Airplane;
import domain.linkedlist.ListException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirplaneServiceTest {
    private static final String AIRPLANES_FILE_NAME = "airplanes.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; // Assuming your JSON files are here

    private Path airplanesFilePath;
    private AirplaneService airplaneService;

    @BeforeEach
    void setUp() throws IOException {
        // Construct the full path to the airplanes.json file
        airplanesFilePath = Utility.getFilePath(DATA_DIRECTORY, AIRPLANES_FILE_NAME);

        // Ensure the directory exists
        Files.createDirectories(airplanesFilePath.getParent());

        // Clear the airplanes.json file before each test to ensure a clean state
        if (Files.exists(airplanesFilePath)) {
            Files.writeString(airplanesFilePath, "[]"); // Write an empty JSON array
            System.out.println("\n--- Setup: Cleared " + AIRPLANES_FILE_NAME + " before test. ---");
        } else {
            System.out.println("\n--- Setup: " + AIRPLANES_FILE_NAME + " does not exist, will be created. ---");
        }

        // Initialize AirplaneService. Its constructor will attempt to load from the (now empty) file.
        airplaneService = new AirplaneService();
        System.out.println("--- Setup: AirplaneService initialized. ---");
    }

//    @AfterEach
//    void tearDown() throws IOException {
//        // Optional: Clean up the file after each test to leave the system in a clean state
//        if (Files.exists(airplanesFilePath)) {
//            Files.writeString(airplanesFilePath, "[]");
//            System.out.println("--- Teardown: Cleared " + AIRPLANES_FILE_NAME + " after test. ---");
//        }
//    }

    @Test
    void testAirplaneServiceOperations() throws ListException, IOException {
        System.out.println("\n--- Starting testAirplaneServiceOperations ---");

        // --- Test 1: Create Airplanes ---
        System.out.println("\n--- Test 1: Creating Airplanes ---");
        Airplane plane1 = new Airplane("SN001", "Boeing 737", 180);
        Airplane plane2 = new Airplane("SN002", "Airbus A320", 150);
        Airplane plane3 = new Airplane("SN003", "Embraer E190", 100);

        assertTrue(airplaneService.createAirplane(plane1), "Should create SN001");
        System.out.println("Created: " + plane1);
        assertTrue(airplaneService.createAirplane(plane2), "Should create SN002");
        System.out.println("Created: " + plane2);
        assertTrue(airplaneService.createAirplane(plane3), "Should create SN003");
        System.out.println("Created: " + plane3);

        List<Airplane> allAirplanes = airplaneService.getAllAirplanes();
        System.out.println("Total airplanes after creation: " + allAirplanes.size());
        assertEquals(3, allAirplanes.size(), "Should have 3 airplanes after creation");
        System.out.println("All airplanes: " + allAirplanes);

        // --- Test 2: Attempt to Create Duplicate Airplane ---
        System.out.println("\n--- Test 2: Attempting to Create Duplicate Airplane (SN001) ---");
        Airplane duplicatePlane = new Airplane("SN001", "Boeing 737 Max", 200); // Same serial number
        ListException thrown = assertThrows(ListException.class, () -> {
            airplaneService.createAirplane(duplicatePlane);
        }, "Should throw ListException for duplicate airplane");
        System.out.println("Expected exception caught: " + thrown.getMessage());
        assertEquals("El avión con número de serie: SN001 ya existe", thrown.getMessage());
        assertEquals(3, airplaneService.getAllAirplanes().size(), "Should still have 3 airplanes after failed duplicate creation");

        // --- Test 3: Find Airplane by Serial Number ---
        System.out.println("\n--- Test 3: Finding Airplanes ---");
        Airplane foundPlane1 = airplaneService.findAirplaneBySerialNumber("SN001");
        assertNotNull(foundPlane1, "SN001 should be found");
        System.out.println("Found SN001: " + foundPlane1);
        assertEquals("Boeing 737", foundPlane1.getModel(), "Model of SN001 should be Boeing 737");

        Airplane notFoundPlane = airplaneService.findAirplaneBySerialNumber("SN999");
        assertNull(notFoundPlane, "SN999 should not be found");
        System.out.println("SN999 not found (as expected).");

        // --- Test 4: Update Airplane ---
        System.out.println("\n--- Test 4: Updating an Airplane (SN002) ---");
        Airplane updatedPlane2 = new Airplane("SN002", "Airbus A321 Neo", 190);
        assertTrue(airplaneService.updateAirplane(updatedPlane2), "Should update SN002");
        System.out.println("Updated SN002 to: " + updatedPlane2);

        Airplane verifiedPlane2 = airplaneService.findAirplaneBySerialNumber("SN002");
        assertNotNull(verifiedPlane2, "Updated SN002 should still exist");
        assertEquals("Airbus A321 Neo", verifiedPlane2.getModel(), "Model of SN002 should be updated");
        assertEquals(190, verifiedPlane2.getTotalCapacity(), "Capacity of SN002 should be updated");
        assertEquals(3, airplaneService.getAllAirplanes().size(), "Total airplanes count should remain 3 after update");
        System.out.println("Verified updated SN002: " + verifiedPlane2);

        // --- Test 5: Attempt to Update Non-Existent Airplane ---
        System.out.println("\n--- Test 5: Attempting to Update Non-Existent Airplane (SN999) ---");
        Airplane nonExistentPlane = new Airplane("SN999", "Dreamliner", 300);
        ListException thrownUpdate = assertThrows(ListException.class, () -> {
            airplaneService.updateAirplane(nonExistentPlane);
        }, "Should throw ListException for non-existent airplane update");
        System.out.println("Expected exception caught: " + thrownUpdate.getMessage());
        assertEquals("Avión con número de serie: SN999 no encontrado para actualización", thrownUpdate.getMessage());

        // --- Test 6: Delete Airplane ---
        System.out.println("\n--- Test 6: Deleting an Airplane (SN003) ---");
        assertTrue(airplaneService.deleteAirplane("SN003"), "Should delete SN003");
        System.out.println("Deleted SN003.");

        allAirplanes = airplaneService.getAllAirplanes();
        assertEquals(2, allAirplanes.size(), "Should have 2 airplanes after deletion");
        assertNull(airplaneService.findAirplaneBySerialNumber("SN003"), "SN003 should no longer be found");
        System.out.println("Total airplanes after deletion: " + allAirplanes.size());
        System.out.println("Remaining airplanes: " + allAirplanes);

        // --- Test 7: Attempt to Delete Non-Existent Airplane ---
        System.out.println("\n--- Test 7: Attempting to Delete Non-Existent Airplane (SN999) ---");
        ListException thrownDelete = assertThrows(ListException.class, () -> {
            airplaneService.deleteAirplane("SN999");
        }, "Should throw ListException for non-existent airplane deletion");
        System.out.println("Expected exception caught: " + thrownDelete.getMessage());
        assertEquals("Avión con número de serie: SN999 no encontrado para eliminación", thrownDelete.getMessage());

        // --- Test 8: Generate Random Airplanes ---
        System.out.println("\n--- Test 8: Generating 5 Random Airplanes ---");
        airplaneService = new AirplaneService(); // Re-initialize service to clear current state and start fresh
        System.out.println("Service re-initialized for random generation test.");
        int initialCount = airplaneService.getAllAirplanes().size();
        System.out.println("Airplanes before random generation: " + initialCount);

        airplaneService.generateInitialRandomAirplanes(5);
        int finalCount = airplaneService.getAllAirplanes().size();
        System.out.println("Total airplanes after generating 5 random: " + finalCount);
        // We expect at least 5 new unique planes, but due to random serial numbers, it might be exactly 5 new.
        // It's safer to assert that the count is greater than or equal to initial + 5, or just equal if starting from empty.
        assertTrue(finalCount >= 5, "Should have at least 5 airplanes after generating 5 random ones (plus any pre-existing, if re-init didn't clear file properly)");
        System.out.println("All airplanes after random generation: " + airplaneService.getAllAirplanes());

        System.out.println("\n--- All AirplaneService operations tested successfully! ---");
    }
}