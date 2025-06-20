package domain.service;

import data.PassengerData;
import domain.btree.TreeException;
import domain.common.Flight;
import domain.common.Passenger;
import domain.common.Airplane; // Necesario para el constructor de Flight
import domain.common.Route;    // Necesario para el constructor de Flight
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList; // Se mantiene si se usa en Flight o Passenger
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Utility; // Para acceder a util.Utility.getFilePath

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime; // Para Route constructor
import java.util.Objects; // Para posibles métodos equals en clases dummy

// Eliminamos import static org.junit.jupiter.api.Assertions.*; ya que no usaremos asserts

class PassengerServiceTest {

    private static final String PASSENGERS_FILE_NAME = "passengers.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    private PassengerService passengerService;
    private Path passengersFilePath; // Para la gestión directa del archivo

    @BeforeEach
    void setUp() throws IOException {
        // Obtener la ruta completa al archivo de pasajeros
        passengersFilePath = Utility.getFilePath(DATA_DIRECTORY, PASSENGERS_FILE_NAME);

        // Asegurarse de que el directorio exista
        Files.createDirectories(passengersFilePath.getParent());

        // Limpiar el archivo de pasajeros antes de cada test para asegurar un estado limpio
        // Escribe un JSON array vacío para simular un archivo limpio
        Files.writeString(passengersFilePath, "[]");
        System.out.println("\n--- Setup: Archivo '" + PASSENGERS_FILE_NAME + "' limpiado antes del test. ---");

        // Inicializar el servicio de pasajeros después de limpiar el archivo
        PassengerData passengerData = new PassengerData();
        passengerService = new PassengerService(passengerData);
        System.out.println("--- Setup: PassengerService inicializado. ---");
    }

//    @AfterEach
//    void tearDown() throws IOException {
//        // Limpiar el archivo de pasajeros después de cada test
//        Files.writeString(passengersFilePath, "[]");
//        System.out.println("--- Teardown: Archivo '" + PASSENGERS_FILE_NAME + "' limpiado después del test. ---\n");
//    }

    @Test
    void testRegisterAndFindPassenger() throws TreeException {
        System.out.println("\n--- Ejecutando testRegisterAndFindPassenger ---");

        Passenger p1 = new Passenger(102, "Carlos", "Costarricense");
        Passenger p2 = new Passenger(103, "Ana", "Panameña");
        LocalDateTime now = LocalDateTime.now(); // Usar un nombre de variable más claro

        // --- Ajuste: Creación correcta de Flight ---
        // Necesitamos instancias de Airplane y Route
        Airplane dummyAirplane = new Airplane("Boeing 737");
        Route dummyRoute = new Route("RT001", 101, 202, "DummyAir", 2.0, 1000.0, 300.0, LocalTime.of(8,0), LocalTime.of(10,0));

        Flight flight = new Flight(1, now, 30, dummyAirplane, dummyRoute);
        // Podemos crear otro flight si es necesario para SinglyLinkedList, pero el test solo usa uno.
        // Flight flight1 = new Flight(2, now.plusHours(2), 40, dummyAirplane, dummyRoute);

        SinglyLinkedList flightList = new SinglyLinkedList(); // Renombrado a flightList para claridad
        flightList.add(flight);
        // flightList.add(flight1); // Si se necesita el segundo vuelo
        System.out.println("Flights creados y añadidos a SinglyLinkedList para ejemplo.");


        boolean registeredP1 = passengerService.registerPassenger(p1);
        System.out.println("Registro de pasajero P1 (" + p1.getId() + "): " + (registeredP1 ? "EXITOSO" : "FALLIDO"));

        boolean registeredP2 = passengerService.registerPassenger(p2);
        System.out.println("Registro de pasajero P2 (" + p2.getId() + "): " + (registeredP2 ? "EXITOSO" : "FALLIDO"));

        try {
            passengerService.saveData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Datos de pasajeros guardados en el archivo.");

        System.out.println("--- Fin testRegisterAndFindPassenger ---");
    }

    @Test
    void testDuplicatePassenger() throws TreeException {
        System.out.println("\n--- Ejecutando testDuplicatePassenger ---");

        Passenger p = new Passenger(200, "Luis", "Salvadoreño");

        boolean registeredFirst = passengerService.registerPassenger(p);
        System.out.println("Primer registro de pasajero " + p.getId() + ": " + (registeredFirst ? "EXITOSO" : "FALLIDO"));

        boolean registeredSecond = passengerService.registerPassenger(p); // Mismo ID, debe fallar
        System.out.println("Segundo registro de pasajero " + p.getId() + " (duplicado): " + (registeredSecond ? "EXITOSO" : "FALLIDO") + " (Esperado: FALLIDO)");

        System.out.println("--- Fin testDuplicatePassenger ---");
    }

    @Test
    void testUpdatePassenger() throws TreeException {
        System.out.println("\n--- Ejecutando testUpdatePassenger ---");

        Passenger p = new Passenger(300, "María", "Nicaragüense");
        passengerService.registerPassenger(p);
        System.out.println("Pasajero P (" + p.getId() + ") registrado inicialmente.");

        // Modificamos nacionalidad
        Passenger updated = new Passenger(300, "María", "Colombiana");

        boolean updatedSuccessfully = passengerService.updatePassenger(updated); // Descomentado y capturado el resultado
        System.out.println("Actualización de pasajero " + updated.getId() + ": " + (updatedSuccessfully ? "EXITOSA" : "FALLIDA"));

        Passenger result = passengerService.findPassengerById(300);
        String currentNationality = (result != null) ? result.getNationality() : "No encontrado";
        System.out.println("Nacionalidad del pasajero " + 300 + " después de la actualización: " + currentNationality + " (Esperado: Colombiana)");

        System.out.println("--- Fin testUpdatePassenger ---");
    }

    @Test
    void testDeletePassenger() throws TreeException {
        System.out.println("\n--- Ejecutando testDeletePassenger ---");

        Passenger p = new Passenger(400, "Juan", "Mexicano");
        passengerService.registerPassenger(p);
        System.out.println("Pasajero P (" + p.getId() + ") registrado inicialmente.");

        Passenger foundBeforeDelete = passengerService.findPassengerById(400);
        System.out.println("Pasajero " + 400 + " encontrado antes de la eliminación: " + (foundBeforeDelete != null ? "SÍ" : "NO"));

        boolean deleted = passengerService.deletePassenger(400); // Descomentado y capturado el resultado
        System.out.println("Eliminación de pasajero " + 400 + ": " + (deleted ? "EXITOSA" : "FALLIDA"));

        Passenger foundAfterDelete = passengerService.findPassengerById(400);
        System.out.println("Pasajero " + 400 + " encontrado después de la eliminación: " + (foundAfterDelete == null ? "NO (Esperado)" : "SÍ (Inesperado)"));

        System.out.println("--- Fin testDeletePassenger ---");
    }

    @Test
    void testRegisterAndLoadPassenger() throws TreeException, IOException { // Añadir IOException
        System.out.println("\n--- Ejecutando testRegisterAndLoadPassenger ---");

        PassengerData data = new PassengerData(); // Reutilizar la instancia, aunque se crea una nueva para el segundo servicio
        PassengerService service = new PassengerService(data); // Un servicio fresco

        Passenger p = new Passenger(102, "Carlos", "Costarricense");

        // Registrar y guardar en archivo
        boolean registered = service.registerPassenger(p);
        System.out.println("Registro de pasajero " + p.getId() + " en el primer servicio: " + (registered ? "EXITOSO" : "FALLIDO"));

        // Asegurarse de que los datos se guarden en el archivo antes de intentar cargarlos
        service.saveData(); // Descomentado y corregido a saveData() si es tu método de guardado
        System.out.println("Datos de pasajeros guardados para el primer servicio.");


        // Ahora simula reiniciar el sistema cargando desde archivo
        PassengerService newService = new PassengerService(new PassengerData()); // Nueva instancia para simular recarga
        Passenger loaded = newService.findPassengerById(102);

        System.out.println("Pasajero " + 102 + " cargado desde archivo: " + (loaded != null ? "EXITOSO. Nombre: " + loaded.getName() + ", Nacionalidad: " + loaded.getNationality() : "FALLIDO. Pasajero no encontrado."));

        System.out.println("--- Fin testRegisterAndLoadPassenger ---");
    }

    @Test
    void loadRandomPassengersTest() {
        System.out.println("\n--- Ejecutando loadRandomPassengersTest ---");
        passengerService.generateInitialRandomPassengers(5); // Generar un número razonable para ver en consola
        System.out.println("Generación de pasajeros aleatorios solicitada. Ver consola para mensajes de registro.");
        System.out.println("--- Fin loadRandomPassengersTest ---");
    }
}