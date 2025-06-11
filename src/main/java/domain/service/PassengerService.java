package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;
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
            this.avlTree = new AVL();
            this.observablePassengers.clear();
        }
    }

    //Método para guardar el estado actual del AVL en el archivo
    public void saveData() {
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


    public void loadRandomPassengersIntoFile(int n){
        passengerData.generateAndSaveInitialRandomPassengers(n);
        //
    }

    // Si aún necesitas acceder al AVL directamente para otras operaciones internas
    public AVL getAvlTree() {
        return avlTree;
    }
}
