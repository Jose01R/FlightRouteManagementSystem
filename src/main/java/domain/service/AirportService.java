package domain.service;

import data.AirportsData;
import domain.common.Airport;
import domain.common.Passenger;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.LinkedQueue;
import domain.linkedqueue.QueueException;
import util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AirportService {

    private DoublyLinkedList airportsDLL;

    public AirportService() {
        this.airportsDLL = new DoublyLinkedList();
        loadAirports();
    }

    private void loadAirports() {
        try {
            List<Airport> loadedList = AirportsData.getAllAirportsAsList();
            airportsDLL.clear();
            if (loadedList != null && !loadedList.isEmpty()) {
                for (Airport airport : loadedList) {
                    airportsDLL.add(airport);
                }
            }

            System.out.println("Airports loaded into AirportService (DLL): " + (airportsDLL.isEmpty() ? 0 : airportsDLL.size()));
        } catch (IOException | ListException e) {
            System.err.println("Error loading airports into AirportService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAirports() throws IOException, ListException {
        try {
            ArrayList<Object> objectsToSave = airportsDLL.toList();
            List<Airport> airportsToSave = new ArrayList<>();
            for (Object obj : objectsToSave) {
                if (obj instanceof Airport) {
                    airportsToSave.add((Airport) obj);
                } else {
                    System.err.println("Warning: Found non-Airport object in DLL during save: " + obj.getClass().getName());
                }
            }
            AirportsData.saveAllAirportsFromList(airportsToSave);

            System.out.println("Airports saved from AirportService (DLL): " + (airportsDLL.isEmpty() ? 0 : airportsDLL.size()));
        } catch (IOException e) {
            System.err.println("Error saving airports from AirportService: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    public boolean createAirport(Airport airport) throws ListException {
        if (!airportsDLL.isEmpty()) {
            if (airportsDLL.contains(airport)) {
                throw new ListException("The airport with code: " + airport.getCode() + " already exists.");
            }
        }
        airportsDLL.add(airport);//AGREGAMOS SI EL AIRPORT NO EXISTE
        try {
            saveAirports();
        } catch (IOException e) {
            //Revertir si falla el guardado
            try {
                airportsDLL.remove(airport);
            } catch (ListException removeEx) {
                System.err.println("CRITICAL: Failed to rollback airport creation after save error: " + removeEx.getMessage());
            }
            throw new ListException("Failed to save airport after creation: " + e.getMessage());
        }
        return true;
    }

    public Airport getAirportByCode(int code) throws ListException {
        if (airportsDLL.isEmpty()) {
            return null; //No hay aeropuertos para buscar
        }

        for (int i = 1; i <= airportsDLL.size(); i++) {
            Node node = airportsDLL.getNode(i);
            if (node != null && node.data instanceof Airport) {
                Airport currentAirport = (Airport) node.data;
                if (currentAirport.getCode() == code) {
                    return currentAirport;
                }
            }
        }
        return null; //Not found
    }

    public Airport getAirportByCountry(String country) throws ListException {
        if (airportsDLL.isEmpty()) {
            return null; //No hay aeropuertos para buscar
        }

        for (int i = 1; i <= airportsDLL.size(); i++) {
            Node node = airportsDLL.getNode(i);
            if (node != null && node.data instanceof Airport) {
                Airport currentAirport = (Airport) node.data;
                if (util.Utility.compare(currentAirport.getCountry(), country) == 0) {
                    return currentAirport;
                }
            }
        }
        return null; //Not found
    }

    public boolean deleteAirport(int codeAirport) throws ListException {
        Airport airportToDelete = getAirportByCode(codeAirport);
        if (airportToDelete == null) {
            throw new ListException("Airport with code: " + codeAirport + " not found for deletion.");
        }

        if (airportsDLL.isEmpty()) {
            throw new ListException("Doubly Linked List is unexpectedly empty during deletion attempt.");
        }
        airportsDLL.remove(airportToDelete);
        try {
            saveAirports();
        } catch (IOException e) {
            //Rollback
            try {
                airportsDLL.add(airportToDelete);
            } catch (Exception addEx) {
                System.err.println("CRITICAL: Failed to rollback airport deletion after save error: " + addEx.getMessage());
            }
            throw new ListException("Failed to save airport changes after deletion: " + e.getMessage());
        }
        return true;
    }

    public boolean updateAirport(Airport updatedAirport) throws ListException {
        if (airportsDLL.isEmpty()) {
            throw new ListException("Doubly Linked List is empty, no airports to update.");
        }

        Airport existingAirport = null;
        int index = -1;

        //Bucle para getNode
        for (int i = 1; i <= airportsDLL.size(); i++) {
            Node node = airportsDLL.getNode(i);
            if (node != null && node.data instanceof Airport) {
                Airport currentAirport = (Airport) node.data;
                if (currentAirport.getCode() == updatedAirport.getCode()) {
                    existingAirport = currentAirport;
                    index = i;
                    break;
                }
            }
        }

        if (existingAirport == null) {
            throw new ListException("Airport with code: " + updatedAirport.getCode() + " not found for update");
        }

        //modification of the node data
        try {
            airportsDLL.getNode(index).data = updatedAirport;
        } catch (ListException e) {
            System.err.println("Error accessing node during update: " + e.getMessage());
            throw new ListException("Internal error: Could not access airport for update. " + e.getMessage());
        }


        try {
            saveAirports();
        } catch (IOException e) {
            if (index != -1) {
                //Rollback
                try {
                    airportsDLL.getNode(index).data = existingAirport;
                } catch (ListException rollbackEx) {
                    System.err.println("CRITICAL: Failed to rollback airport update after save error: " + rollbackEx.getMessage());
                }
            }
            throw new ListException("Failed to save airport changes after update: " + e.getMessage());
        }
        return true;
    }

    public boolean changeAirportStatus(int codeAirport) throws ListException {
        if (airportsDLL.isEmpty()) {
            throw new ListException("Doubly Linked List is empty, no airports to change status");
        }

        Airport airportToUpdate = null;

        for (int i = 1; i <= airportsDLL.size(); i++) {
            Node node = airportsDLL.getNode(i);
            if (node != null && node.data instanceof Airport) {
                airportToUpdate = (Airport) node.data;
                if (airportToUpdate.getCode() == codeAirport) {
                    break; //Found the airport
                }
            }
        }

        if (airportToUpdate == null) {
            throw new ListException("Airport with code: " + codeAirport + " not found to change status");
        }

        String originalStatus = airportToUpdate.getStatus();
        if ("Active".equalsIgnoreCase(originalStatus)) {
            airportToUpdate.setStatus("Inactive");
        } else {
            airportToUpdate.setStatus("Active");
        }
        //Actualizamos y salvamos cambio
        try {
            saveAirports();
        } catch (IOException e) {
            airportToUpdate.setStatus(originalStatus); //Rollback
            throw new ListException("Failed to save airport status change: " + e.getMessage());
        }
        return true;
    }

    public ArrayList<Object> getAllAirportsAsList() throws ListException {
        return airportsDLL.toList();
    }

    public DoublyLinkedList getAllAirports() {
        return this.airportsDLL;
    }

    public ArrayList<Object> getAirportsByStatus(String status) throws ListException {
        ArrayList<Object> allAirportsObjects = null;
        allAirportsObjects = airportsDLL.toList();

        if ("Ambos".equalsIgnoreCase(status)) {
            if (allAirportsObjects.isEmpty()) {
                throw new ListException("No airports found in the system");
            }
            return allAirportsObjects;
        }

        List<Object> filteredList = allAirportsObjects.stream()
                .filter(obj -> {
                    if (obj instanceof Airport) {
                        return ((Airport) obj).getStatus().equalsIgnoreCase(status);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (filteredList.isEmpty() && !allAirportsObjects.isEmpty()) {
            throw new ListException("No airports found with status: " + status);
        } else if (filteredList.isEmpty() && allAirportsObjects.isEmpty()) {
            throw new ListException("No airports found in the system");
        }

        return new ArrayList<>(filteredList);
    }

    public SinglyLinkedList getAirportsByStatusAsSinglyLinkedList(String status) throws ListException {
        SinglyLinkedList filteredSLL = new SinglyLinkedList();

        System.out.println("\nSearching for airports with status: " + status + " in DLL...");

        if (airportsDLL.isEmpty()) {
            System.out.println("No airports in main DLL to process");
            throw new ListException("No airports found in the system for status: " + status);
        }

        try {
            for (int i = 1; i <= airportsDLL.size(); i++) {
                Node node = airportsDLL.getNode(i);
                if (node != null && node.data instanceof Airport) {
                    Airport currentAirport = (Airport) node.data;
                    if ("Ambos".equalsIgnoreCase(status) || currentAirport.getStatus().equalsIgnoreCase(status)) {
                        filteredSLL.add(currentAirport);
                    }
                } else {
                    System.err.println("Warning: Skipped non-Airport object or null node at index " + i + " in DLL during status filter");
                }
            }
        } catch (ListException e) {
            throw new ListException("Error accessing airports in DLL for status filter: " + e.getMessage());
        }

        if (filteredSLL.isEmpty()) {
            throw new ListException("No airports found with status: " + status);
        }

        System.out.println("Found " + filteredSLL.size() + " airports with status '" + status + "' (as SinglyLinkedList)");
        return filteredSLL;
    }


    public void addPassengerToBoardingQueue(int airportCode, Passenger passenger) throws ListException, QueueException {
        Airport airport = getAirportByCode(airportCode);
        if (airport == null) {
            throw new ListException("Airport " + airportCode + " not found to add passenger to queue.");
        }
        if (airport.getBoardingQueue() == null) {
            airport.setBoardingQueue(new LinkedQueue());
        }
        airport.getBoardingQueue().enQueue(passenger);
        System.out.println("Passenger " + passenger.getId() + " added to boarding queue at " + airport.getName());
    }

    public LinkedQueue getAirportBoardingQueue(int airportCode) throws ListException {
        Airport airport = getAirportByCode(airportCode);
        if (airport == null) {
            throw new ListException("Airport " + airportCode + " not found to retrieve boarding queue.");
        }
        return airport.getBoardingQueue();
    }

    public void generateInitialRandomAirports(int count) throws ListException {
        String[] airportNames = {"Juan Santamaría", "La Aurora", "Tocumen", "El Salvador", "Toncontín", "El Dorado", "Benito Juárez"};
        String[] countries = {"Costa Rica", "Guatemala", "Panamá", "El Salvador", "Honduras", "Colombia", "México"};

        System.out.println("Generating " + count + " random airports...");
        for (int i = 0; i < count; i++) {
            int code = 100 + Utility.random(900);
            String name = airportNames[Utility.random(airportNames.length)];
            String country = countries[Utility.random(countries.length)];
            String status = Utility.random(2) == 0 ? "Active" : "Inactive";

            Airport newAirport = new Airport(code, name, country, status);
            try {
                //Esto evita intentar agregar un duplicado y encontrar la ListException
                if (getAirportByCode(newAirport.getCode()) != null) {
                    System.out.println("Airport with code " + newAirport.getCode() + " already exists. Skipping random generation for this code");
                    continue; //Omite esta iteración e intenta generar otro aeropuerto único
                }
                createAirport(newAirport);
            } catch (ListException e) {
                System.err.println("Warning: Could not create random airport " + code + ". Error: " + e.getMessage());
            }
        }
        System.out.println(count + " random airports generated and saved");
    }
}