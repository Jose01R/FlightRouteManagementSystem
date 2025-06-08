package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Flight;
import domain.common.Passenger;
import domain.linkedlist.SinglyLinkedList;
import domain.service.PassengerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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


        Flight flight= new Flight(1,"Costa Rica","Canada",localDateTime,30,"No se");
        Flight flight1= new Flight(2,"Canada","Costa Rica",localDateTime,30,"Si se");
        SinglyLinkedList singlyLinkedList= new SinglyLinkedList();
        singlyLinkedList.add(flight);
        singlyLinkedList.add(flight1);
        p1.setFlightHistory(singlyLinkedList);
        // Registrar pasajeros
        assertTrue(passengerService.registerPassenger(p1));
        assertTrue(passengerService.registerPassenger(p2));

        // Buscar por ID
       // Passenger result1 = passengerService.findPassengerById(101);
       // Passenger result2 = passengerService.findPassengerById(102);


        //assertEquals("Carlos", result1.getName());
        //assertEquals("Ana", result2.getName());
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

        assertTrue(passengerService.updatePassenger(updated));
        Passenger result = passengerService.findPassengerById(300);
        assertEquals("Colombiana", result.getNationality());

    }

    @Test
    void testDeletePassenger() throws TreeException {
        Passenger p = new Passenger(400, "Juan", "Mexicano");
        passengerService.registerPassenger(p);

        assertNotNull(passengerService.findPassengerById(400));

        assertTrue(passengerService.deletePassenger(400));

        assertNull(passengerService.findPassengerById(400));
    }
    @Test
    void loadToAVL(){
        AVL avl;
        avl=passengerService.getAvlTree();
        System.out.println(avl);
    }
}
