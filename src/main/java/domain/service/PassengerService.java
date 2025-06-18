package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;
import domain.linkedlist.SinglyLinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PassengerService {
    private PassengerData passengerData;
    private AVL avlTree;
    private ObservableList<Passenger> observablePassengers;

    public PassengerService(PassengerData passengerData) {
        this.passengerData = passengerData;
        this.avlTree = new AVL();

        this.observablePassengers = FXCollections.observableArrayList();
        loadInitialPassengers();
        generateInitialRandomPassengers(10);

    }


    public ObservableList<Passenger> getObservablePassengers() {
        return observablePassengers;
    }


    private void loadInitialPassengers() {
        try {
            // Carga el árbol AVL desde el archivo
            AVL loadedTree = passengerData.readPassengers();
            if (loadedTree != null) {
                this.avlTree = loadedTree; // Asigna el árbol cargado
                //llena la ObservableList con los datos del árbol AVL
                List<Passenger> passengersFromTree = new ArrayList<>();
                for (Object obj : this.avlTree.inOrderNodes1()) {
                    passengersFromTree.add((Passenger) obj);
                }
                observablePassengers.setAll(passengersFromTree);// Establece todos los elementos de una vez
            }
        } catch (TreeException e) {
            System.err.println("Error al cargar pasajeros iniciales del archivo: " + e.getMessage());

            this.avlTree = new AVL();
            this.observablePassengers.clear();
        } catch (Exception e) {
            System.err.println("Error inesperado al cargar pasajeros iniciales: " + e.getMessage());
            e.printStackTrace();
            //this.avlTree = new AVL();
            //this.observablePassengers.clear();
        }
    }

    //Método para guardar el estado actual del AVL en el archivo
    public void saveData() {
        try {
            System.out.println("Guardando datos... cantidad de pasajeros: " + avlTree.size());
        } catch (TreeException e) {
            throw new RuntimeException(e);
        }
        passengerData.saveAllPassengers(this.avlTree);
    }


    public boolean registerPassenger(Passenger passenger) throws TreeException {
        if (findPassengerById(passenger.getId()) != null) {
            throw new TreeException("Pasajero con ID " + passenger.getId() + " ya existe.");
        }

        this.avlTree.add(passenger);
        observablePassengers.add(passenger);
        return true;
    }

    public void updatePassenger(Passenger passenger) throws TreeException {

        Passenger existingPassenger = findPassengerById(passenger.getId());

        if (existingPassenger != null) {

            this.avlTree.updatePassenger(passenger);
            System.out.println("Pasajero " + passenger.getId() + " actualizado en memoria.");


            observablePassengers.removeIf(p -> p.getId() == passenger.getId());//Elimina la versión antigua
            observablePassengers.add(passenger); //Añade la versión actualizada

        } else {
            throw new TreeException("Pasajero con ID " + passenger.getId() + " no encontrado para actualizar.");
        }
    }

    public Passenger findPassengerById(int passengerId) {

        return avlTree.getPassengerById(passengerId);
    }


    public void generateInitialRandomPassengers(int count) {

        String[] names = {"Juan", "Maria", "Pedro", "Ana", "Luis", "Sofía", "Diego", "Valeria","Jared","Jose","Luis","Pablo","Mario","Miguel","Kristel","Alondra","Lincy","Fabricio"};
        String[] nationalities = {"Costarricense", "Nicaragüense", "Panameño", "Guatemalteco", "Salvadoreño", "Hondureño","Colombiano"};

        for (int i = 0; i < count; i++) {

            int id = 100_000_000 + util.Utility.random(900_000_000);

            String name = names[util.Utility.random(names.length)];
            String nationality = nationalities[util.Utility.random(nationalities.length)];
            Passenger passenger= new Passenger(id, name, nationality);
            passenger.setFlightHistory(new SinglyLinkedList());
            avlTree.add(passenger);
            observablePassengers.add(passenger);
        }

    }

    public AVL getAvlTree() {
        return avlTree;
    }
}
