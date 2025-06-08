package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;

public class PassengerService {
    private PassengerData passengerData;
    private AVL avlTree;

    public PassengerService(PassengerData passengerData) {
        this.passengerData = passengerData;
        this.avlTree = new AVL();
        loadPassengersIntoAVL(); // Cargamos desde JSON a la estructura AVL
    }

    /**
     * Carga todos los pasajeros desde PassengerData al árbol AVL.
     */
    private void loadPassengersIntoAVL() {
        avlTree.clear(); // aseguramos árbol limpio
        for (Passenger passenger : passengerData.getAllPassengers().values()) {
            avlTree.add(passenger);
        }
        System.out.println("Pasajeros cargados en AVL.");
    }

    /**
     * Registra un nuevo pasajero, valida duplicados por ID.
     */
    public boolean registerPassenger(Passenger passenger) throws TreeException {
        // Verificar existencia antes de registrar
        if (avlTree.getPassengerById(passenger.getId()) != null) {
            System.out.println("Ya existe un pasajero con ID: " + passenger.getId());
            return false;
        }

        passengerData.registerPassenger(passenger); // persistencia
        avlTree.add(passenger);                    // estructura en memoria
        return true;
    }

    /**
     * Actualiza un pasajero en el sistema y AVL.
     */
    public boolean updatePassenger(Passenger passenger) throws TreeException {
        // Verifica si existe en el AVL antes de actualizar
        Passenger existing = avlTree.getPassengerById(passenger.getId());
        if (existing == null) {
            System.out.println("No se puede actualizar. No existe pasajero con ID: " + passenger.getId());
            return false;
        }

        // Actualiza primero en el archivo
        boolean updatedInData = passengerData.updatePassenger(passenger);
        if (!updatedInData) {
            System.out.println("Falló la actualización en el archivo.");
            return false;
        }

        // Actualiza en el AVL: elimina la versión anterior y agrega la nueva
        avlTree.remove(existing); // Usamos el que sí está dentro
        avlTree.add(passenger);   // Inserta la nueva versión

        return true;
    }


    /**
     * Elimina un pasajero del sistema y del árbol.
     */
    public boolean deletePassenger(int id) throws TreeException {
        Passenger passenger = avlTree.getPassengerById(id);
        if (passenger == null) {
            System.out.println("No se encontró pasajero con ID: " + id);
            return false;
        }

        passengerData.deletePassenger(id);
        avlTree.remove(passenger);
        return true;
    }

    /**
     * Busca un pasajero por ID.
     */
    public Passenger findPassengerById(int id) throws TreeException {
        return avlTree.getPassengerById(id);
    }

    /**
     * Devuelve el árbol AVL actual (útil para vistas u operaciones masivas).
     */
    public AVL getAvlTree() {
        return avlTree;
    }
}

