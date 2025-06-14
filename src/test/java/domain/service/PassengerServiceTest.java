package domain.service;

import data.PassengerData;
import domain.btree.TreeException;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.SinglyLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
}
