package domain.service;

import domain.common.Airplane;
import domain.common.Flight;
import domain.linkedstack.LinkedStack;
import domain.linkedlist.ListException;
import data.AirplaneData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AirplaneService {

    private Map<String, Airplane> airplanesMap; //String para el número de serie como key
    private ObservableList<Airplane> observableAirplanes;

    public AirplaneService() {
        this.airplanesMap = new HashMap<>();
        this.observableAirplanes= FXCollections.observableArrayList();
        try {
            generateInitialRandomAirplanes(10);
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
        loadAirplanes();
    }

    public ObservableList<Airplane> getObservableAirplanes() {
        return observableAirplanes;
    }

    private void loadAirplanes() {
        try {
            List<Airplane> loadedList = AirplaneData.getAllAirplanesAsList();
            airplanesMap.clear(); //Limpia el mapa antes de cargar
            if (loadedList != null) {
                for (Airplane airplane : loadedList) {
                    airplanesMap.put(airplane.getSerialNumber(), airplane);
                }
                observableAirplanes.setAll(airplanesMap.values());
            }
            System.out.println("Airplanes loaded in AirplaneService: " + airplanesMap.size());
        } catch (IOException e) {
            System.err.println("Error loading airplanes in AirplaneService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAirplanes() {
        try {
            AirplaneData.saveAllAirplanesFromList(new ArrayList<>(airplanesMap.values()));
            System.out.println("Airplanes saved from AirplaneService: " + airplanesMap.size());
        } catch (IOException e) {
            System.err.println("Error saving airplanes from AirplaneService: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Crea un nuevo avión y lo agrega al mapa en memoria y lo conserva
     *
     * @param airplane The Airplane object to create.
     * @return true if created successfully.
     * @throws ListException If an airplane with the same serial number already exists, or save fails.
     */
    public boolean createAirplane(Airplane airplane) throws ListException {
        Objects.requireNonNull(airplane, "Airplane object cannot be null.");

        if (airplanesMap.containsKey(airplane.getSerialNumber())) {
            throw new ListException("El avión con número de serie: " + airplane.getSerialNumber() + " ya existe");
        }
        airplanesMap.put(airplane.getSerialNumber(), airplane);
        observableAirplanes.add(airplane);

        try {
            saveAirplanes();
        } catch (Exception e) {
            airplanesMap.remove(airplane.getSerialNumber()); //Revertir cambios en memoria
            throw new ListException("Failed to save airplane after creation: " + e.getMessage());
        }
        System.out.println("Avión " + airplane.getSerialNumber() + " creado y guardado.");
        return true;
    }

    /**
     * Encuentra un avión por su número de serie en el mapa en memoria
     *
     * @param serialNumber The serial number of the airplane.
     * @return The Airplane object, or null if not found.
     */
    public Airplane findAirplaneBySerialNumber(String serialNumber) {
        return airplanesMap.get(serialNumber);
    }

    /**
     * Elimina un avión por su número de serie del mapa en memoria y conserva los cambios
     *
     * @throws ListException If the airplane is not found, or save fails.
     */
    public boolean deleteAirplane(String serialNumber) throws ListException {
        if (!airplanesMap.containsKey(serialNumber)) {
            throw new ListException("Avión con número de serie: " + serialNumber + " no encontrado para eliminación");
        }
        Airplane airplaneToDelete = airplanesMap.get(serialNumber); //Obtener una posible reversión

        airplanesMap.remove(serialNumber);
        observableAirplanes.remove(airplaneToDelete);

        // --- Revertir si falla el guardado ---
        try {
            saveAirplanes();
        } catch (Exception e) {
            airplanesMap.put(serialNumber, airplaneToDelete); //Revertir cambios en memoria
            throw new ListException("Failed to save airplane changes after deletion: " + e.getMessage());
        }
        System.out.println("Avión " + serialNumber + " eliminado y guardado");
        return true; //verdadero si se borro correctamente
    }

    /**
     * Actualiza un avión existente en el mapa en memoria y conserva los cambios
     *
     * @throws ListException If the airplane is not found, or save fails
     */
    public boolean updateAirplane(Airplane updatedAirplane) throws ListException {
        Objects.requireNonNull(updatedAirplane, "Updated airplane object cannot be null");

        if (!airplanesMap.containsKey(updatedAirplane.getSerialNumber())) {
            throw new ListException("Avión con número de serie: " + updatedAirplane.getSerialNumber() + " no encontrado para actualización");
        }
        Airplane oldAirplane = airplanesMap.get(updatedAirplane.getSerialNumber());

        //Reemplazamos el objeto antiguo por el actualizado
        airplanesMap.put(updatedAirplane.getSerialNumber(), updatedAirplane);

        int index = observableAirplanes.indexOf(oldAirplane);
        if (index >= 0) {
            observableAirplanes.set(index, updatedAirplane);
        }

        // --- Revertir si falla el guardado ---
        try {
            saveAirplanes();
        } catch (Exception e) {
            airplanesMap.put(oldAirplane.getSerialNumber(), oldAirplane); //Revertir cambios en memoria
            throw new ListException("Failed to save airplane changes after update: " + e.getMessage());
        }
        System.out.println("Avión " + updatedAirplane.getSerialNumber() + " actualizado y guardado.");
        return true; //si se actualizó correctamente
    }

    /**
     * Devuelve una lista de todos los aviones actualmente en la memoria
     */
    public List<Airplane> getAllAirplanes() {
        return new ArrayList<>(airplanesMap.values());
    }

    /**
     * Genera un número específico de aviones aleatorios iniciales
     *
     * @throws ListException If there's a problem creating an airplane
     */
    public void generateInitialRandomAirplanes(int count) throws ListException {
        String[] models = {"Boeing 737", "Airbus A320", "Embraer E190", "Boeing 787", "Airbus A350"};
        int[] capacities = {100, 150, 200};

        int generatedCount = 0;

        //Usamos un while para asegurarnos de que se contabilicen los aviones únicos generados
        while (generatedCount < count) {
            String serialNumber = "SN" + (1000 + Utility.random(9000));
            String model = models[Utility.random(models.length)];
            int capacity = capacities[Utility.random(capacities.length)];

            Airplane newAirplane = new Airplane(serialNumber, model, capacity);

            try {
                if (createAirplane(newAirplane)) {
                    observableAirplanes.add(newAirplane);
                    generatedCount++;
                }
            } catch (ListException e) {

                System.err.println("Error creating random airplane " + serialNumber + ": " + e.getMessage());

            }
        }
        System.out.println(generatedCount + " unique random airplanes generated and saved.");
    }
}