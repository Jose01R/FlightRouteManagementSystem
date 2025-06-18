package domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import data.PassengerData;
import domain.btree.TreeException;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.SinglyLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PassengerServiceTest {

    private PassengerService passengerService;

    @BeforeEach
    void setUp() {
        //File file = new File("passengers.json");
       // if (file.exists()) file.delete();

        PassengerData passengerData = new PassengerData();
        passengerService = new PassengerService(passengerData);
    }

    @Test
    void testRegisterAndFindPassenger() throws TreeException {
        Passenger p1 = new Passenger(102, "Carlos", "Costarricense");
        Passenger p2 = new Passenger(103, "Ana", "Panameña");
        LocalDateTime localDateTime = LocalDateTime.now();


        Flight flight= new Flight(1,"Costa Rica","Canada",localDateTime,30);
        Flight flight1= new Flight(2,"Canada","Costa Rica",localDateTime,30);
        SinglyLinkedList singlyLinkedList= new SinglyLinkedList();
        singlyLinkedList.add(flight);
        singlyLinkedList.add(flight1);

        passengerService.registerPassenger(p1);
        passengerService.registerPassenger(p2);
        passengerService.saveData();

    }

    @Test
    void testDuplicatePassenger() throws TreeException {
        Passenger p = new Passenger(200, "Luis", "Salvadoreño");

        assertTrue(passengerService.registerPassenger(p));
        assertFalse(passengerService.registerPassenger(p)); // Mismo ID, debe fallar
    }

    @Test
    void testUpdatePassenger() throws TreeException {
        Passenger p = new Passenger(300, "María", "Nicaragüense");
        passengerService.registerPassenger(p);

        // Modificamos nacionalidad
        Passenger updated = new Passenger(300, "María", "Colombiana");

       // assertTrue(passengerService.updatePassenger(updated));
        Passenger result = passengerService.findPassengerById(300);
        assertEquals("Colombiana", result.getNationality());

    }

    @Test
    void testDeletePassenger() throws TreeException {
        Passenger p = new Passenger(400, "Juan", "Mexicano");
        passengerService.registerPassenger(p);

        assertNotNull(passengerService.findPassengerById(400));

        //assertTrue(passengerService.deletePassenger(400));

        assertNull(passengerService.findPassengerById(400));
    }
    @Test
    void testRegisterAndLoadPassenger() throws TreeException {
        PassengerData data = new PassengerData();
        PassengerService service = new PassengerService(data);

        Passenger p = new Passenger(102, "Carlos", "Costarricense");

        //Registrar y guardar en archivo
        service.registerPassenger(p);
       // service.saveAllPassengers();

        //Ahora simula reiniciar el sistema cargando desde archivo
        PassengerService newService = new PassengerService(new PassengerData());
        Passenger loaded = newService.findPassengerById(102);

        assertNotNull(loaded);
        System.out.println("Cargado correctamente: " + loaded.getName());
    }

    @Test
    void loadRandomPassengersTest(){
        passengerService.generateInitialRandomPassengers(10);
    }
    @Test
    public void testPassengerSharedFlightSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // <-- Soluciona el problema
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Usa formato ISO-8601 legible


        // Vuelo compartido
        Flight sharedFlight = new Flight(3863, "San José", "Madrid", LocalDateTime.of(2025, 7, 1, 10, 30), 100);

        // Pasajeros que comparten vuelo
        Passenger p1 = new Passenger(1, "Ana", "CR");
        Passenger p2 = new Passenger(2, "Luis", "CR");

        p1.setFlightHistoryFromList(Arrays.asList(sharedFlight));
        p2.setFlightHistoryFromList(Arrays.asList(sharedFlight));

        // Serializar
        String json = mapper.writeValueAsString(Arrays.asList(p1, p2));
        assertNotNull(json);
        assertTrue(json.contains("3863"));

        // Deserializar
        Passenger[] deserialized = mapper.readValue(json, Passenger[].class);

        assertEquals(2, deserialized.length);
        Flight f1 = deserialized[0].getFlightHistoryAsList().get(0);
        Flight f2 = deserialized[1].getFlightHistoryAsList().get(0);

        // Verificar que comparten la misma instancia de vuelo
        assertSame(f1, f2, "Ambos pasajeros deberían tener la misma instancia de vuelo");
    }
}
