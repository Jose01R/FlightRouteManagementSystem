package domain.service;

import data.PassengerData;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;
import domain.linkedlist.SinglyLinkedList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PassengerService {
    private PassengerData passengerData;
    private AVL avlTree;
    private ObservableList<Passenger> observablePassengers;

    public PassengerService(PassengerData passengerData) {
        this.passengerData = Objects.requireNonNull(passengerData, "PassengerData cannot be null");
        this.avlTree = new AVL();

        this.observablePassengers = FXCollections.observableArrayList();
        loadInitialPassengers();

    }

    public ObservableList<Passenger> getObservablePassengers() {
        return observablePassengers;
    }

    private void loadInitialPassengers() {
        try {
            AVL loadedTree = passengerData.readPassengers(); //Reads a AVL
            this.avlTree = new AVL(); //Always re-initialize to ensure a clean tree before adding
            this.observablePassengers.clear(); //Clear observable list

            if (loadedTree != null && !loadedTree.isEmpty()) {
                List<Object> objectsFromTree = loadedTree.inOrderNodes1(); //Get all objects from the loaded tree
                List<Passenger> passengersForObservableList = new ArrayList<>();

                for (Object obj : objectsFromTree) {
                    if (obj instanceof Passenger) {
                        passengersForObservableList.add((Passenger) obj);
                        this.avlTree.add(obj); //Add to AVL
                    } else {
                        System.err.println("Warning: Object in loaded AVL tree is not a Passenger. Type: " + (obj != null ? obj.getClass().getName() : "null"));
                    }
                }
                observablePassengers.setAll(passengersForObservableList); //Set all elements at once
                System.out.println("Passengers loaded in PassengerService: " + this.avlTree.size());
            } else {
                System.out.println("No passengers found to load. Initializing empty PassengerService.");
            }
        } catch (TreeException e) {
            System.err.println("Error loading initial passengers from file (TreeException): " + e.getMessage());
            e.printStackTrace();
            this.avlTree = new AVL();
            this.observablePassengers.clear();
        } catch (Exception e) {
            System.err.println("Unexpected error loading initial passengers: " + e.getMessage());
            e.printStackTrace();
            this.avlTree = new AVL();
            this.observablePassengers.clear();
        }
    }

    //Método para guardar el estado actual del AVL en el archivo
    public void saveData() throws TreeException, IOException {
        System.out.println("Saving passenger data... Number of passengers: " + avlTree.size());
        passengerData.saveAllPassengers(this.avlTree);
    }

    /**
     * Registra un nuevo pasajero
     *
     * @param passenger The passenger to register
     * @return true if registered successfully
     * @throws TreeException if a passenger with the same ID already exists or save fails
     */
    public boolean registerPassenger(Passenger passenger) throws TreeException {
        Objects.requireNonNull(passenger, "Passenger object cannot be null.");

        if (findPassengerById(passenger.getId()) != null) {
            throw new TreeException("Passenger with ID " + passenger.getId() + " already exists.");
        }
        this.avlTree.add(passenger); //Add Passenger to AVL
        this.observablePassengers.add(passenger); //Add to ObservableList

        try {
            saveData(); //Save changes
        } catch (TreeException | IOException e) {
            //Rollback: try to remove the passenger that was just added
            try {
                this.avlTree.remove(passenger);
                this.observablePassengers.remove(passenger); //Also remove from observable list
            } catch (TreeException removeEx) {
                System.err.println("CRITICAL: Failed to rollback passenger registration after save error: " + removeEx.getMessage());
            }
            throw new TreeException("Failed to save passenger after registration: " + e.getMessage());
        }
        return true;
    }

    /**
     * Actualiza un pasajero existente
     *
     * @param passenger The passenger object with updated details
     * @return true if updated successfully
     * @throws TreeException if the passenger is not found or save fails
     */
    public boolean updatePassenger(Passenger passenger) throws TreeException {
        Objects.requireNonNull(passenger, "Passenger object cannot be null");

        Passenger existingPassenger = findPassengerById(passenger.getId());

        if (existingPassenger != null) {

            this.avlTree.updatePassenger(passenger); // AVL.updatePassenger(Object)
            System.out.println("Passenger " + passenger.getId() + " updated in memory.");

            //Update ObservableList for UI by replacing the old instance
            for (int i = 0; i < observablePassengers.size(); i++) {
                if (observablePassengers.get(i).getId() == passenger.getId()) {
                    observablePassengers.set(i, passenger); //Replace the old object with the new one
                    break;
                }
            }

            try {
                saveData(); //Save changes
            } catch (TreeException | IOException e) {

                System.err.println("Failed to save passenger changes after update. Manual rollback might be needed: " + e.getMessage());
                throw new TreeException("Failed to save passenger changes after update: " + e.getMessage());
            }
            return true;
        } else {
            throw new TreeException("Passenger with ID " + passenger.getId() + " not found for update.");
        }
    }

    /**
     * Encuentra a un pasajero por su ID
     *
     * @param passengerId The ID of the passenger to find
     * @return The Passenger object, or null if not found
     */
    public Passenger findPassengerById(int passengerId) {

        Object foundObj = avlTree.getPassengerById(passengerId);
        if (foundObj instanceof Passenger) {
            return (Passenger) foundObj;
        } else if (foundObj != null) {
            System.err.println("Error: Object retrieved from AVL for ID " + passengerId + " is not a Passenger. Type: " + foundObj.getClass().getName());
        }
        return null; // Not found or error occurred
    }

    /**
     * Elimina un pasajero por su ID
     *
     * @param passengerId The ID of the passenger to delete
     * @return true if deleted successfully
     * @throws TreeException if the passenger is not found or save fails
     */
    public boolean deletePassenger(int passengerId) throws TreeException {
        Passenger passengerToDelete = findPassengerById(passengerId);
        if (passengerToDelete == null) {
            throw new TreeException("Passenger with ID " + passengerId + " not found for deletion.");
        }

        this.avlTree.remove(passengerToDelete);
        this.observablePassengers.removeIf(p -> p.getId() == passengerId); //Remove from ObservableList

        try {
            saveData(); //Save changes
        } catch (TreeException e) {
            //Rollback: try to re-add the passenger
            this.avlTree.add(passengerToDelete);
            this.observablePassengers.add(passengerToDelete); //Add back to observable list
            throw new TreeException("Failed to save passenger changes after deletion: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Passenger " + passengerId + " deleted from memory.");
        return true;
    }

    /**
     * Returns a list of all passengers currently in memory
     * @return A list of all Passenger objects
     */
    public List<Passenger> getAllPassengers() {
        List<Passenger> passengers = new ArrayList<>();
        try {

            for (Object obj : avlTree.inOrderNodes1()) {
                if (obj instanceof Passenger) {
                    passengers.add((Passenger) obj);
                } else {
                    System.err.println("Warning: Non-Passenger object found in AVL tree during getAllPassengers: " + (obj != null ? obj.getClass().getName() : "null"));
                }
            }
        } catch (TreeException e) {
            System.err.println("Error traversing AVL tree to get all passengers: " + e.getMessage());
            e.printStackTrace();
        }
        return passengers;
    }

    public void generateInitialRandomPassengers(int count) {
        String[] names = {"Juan", "Maria", "Pedro", "Ana", "Luis", "Sofía", "Diego", "Valeria", "Jared", "Jose", "Pablo", "Mario", "Miguel", "Kristel", "Alondra", "Lincy", "Fabricio"};
        String[] nationalities = {"Costarricense", "Nicaragüense", "Panameño", "Guatemalteco", "Salvadoreño", "Hondureño", "Colombiano"};

        int addedCount = 0;
        for (int i = 0; i < count; i++) {
            int id = 100_000_000 + util.Utility.random(900_000_000); //Generates a random 9-digit ID

            String name = names[util.Utility.random(names.length)];
            String nationality = nationalities[util.Utility.random(nationalities.length)];
            Passenger passenger = new Passenger(id, name, nationality);


            if (passenger.getFlightHistory() == null) {

                passenger.setFlightHistory(new SinglyLinkedList());
            }

            try {
                if (registerPassenger(passenger)) { //Use the service's register method to handle duplicates
                    addedCount++;
                }
            } catch (TreeException e) {
                //If a duplicate ID is generated, simply skip and try again.
                System.err.println("Duplicate random passenger ID generated: " + id + ". Trying again.");
                i--; //Retry this iteration to ensure 'count' unique passengers are added
            }
        }
        System.out.println(addedCount + " unique random passengers generated and added to the list.");
    }

    public AVL getAvlTree() {
        return avlTree;
    }
}