package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;

public class PassengerService {
    private PassengerData passengerData;
    private AVL avlTree; // Este es el árbol en memoria

    public PassengerService(PassengerData passengerData) {
        this.passengerData = passengerData;
        this.avlTree = passengerData.readPassengers(); // Carga los datos al inicio
    }

    public AVL getAvlTree() {
        return avlTree;
    }

    public boolean registerPassenger(Passenger passenger) throws TreeException {
        // Solo modifica el árbol en memoria
        if (findPassengerById(passenger.getId()) != null) {
            // Si el pasajero ya existe, lanza la excepción y retorna false (o solo lanza la excepción)
            throw new TreeException("Pasajero con ID " + passenger.getId() + " ya existe.");
        }

        // Si el pasajero no existe, intenta añadirlo al árbol AVL
        // Si avlTree.add() lanza una TreeException por alguna otra razón, esta se propagará
        this.avlTree.add(passenger);

        // Si la adición fue exitosa (no se lanzó ninguna excepción hasta este punto), retorna true
        return true;
    }

    public void updatePassenger(Passenger passenger) throws TreeException {
        // Esto asume que tu AVL permite actualizar un nodo existente o que lo borras y lo insertas de nuevo
        // Si el AVL no tiene un método de "actualizar", tendrías que remover y volver a añadir
        if (avlTree.contains(passenger)) { // Asume que 'contains' usa el equals de Passenger
            // La estrategia para "actualizar" en un AVL sin un método directo de actualización
            // podría ser eliminar el viejo y añadir el nuevo si los datos modificados afectan el orden o la clave
            // Si solo se cambian atributos no clave, el objeto en el árbol ya está actualizado en memoria.
            // Si la clave (ID) NO cambia, el objeto ya está actualizado.
            // Si la clave cambia, entonces sería un remove y add con la nueva clave.
            // Por simplicidad aquí, asumimos que el objeto 'passenger' que se pasa ya es la referencia del árbol.
            System.out.println("Pasajero " + passenger.getId() + " actualizado en memoria.");
            // No necesitas hacer nada más si la referencia ya es la misma y los atributos no clave cambiaron.
            // Si necesitas forzar una "actualización" para que Jackson lo guarde correctamente,
            // asegúrate de que el objeto en el AVL sea la versión más reciente.
            // Esto es más complejo si el AVL hace copias de los objetos.
        } else {
            throw new TreeException("Pasajero con ID " + passenger.getId() + " no encontrado para actualizar.");
        }
        // *** ¡QUITAR LA LLAMADA A passengerData.writePassengers() AQUÍ! ***
    }

    public Passenger findPassengerById(int passengerId) {

        return (Passenger) avlTree.getPassengerById(passengerId);

        //return null; // Pasajero no encontrado o árbol vacío
    }

    public void saveData() {
        passengerData.saveAllPassengers(this.avlTree);
    }
}

