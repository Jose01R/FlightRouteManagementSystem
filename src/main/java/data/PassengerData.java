package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.common.Passenger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PassengerData {
    private static final String PASSENGERS_FILE = "passengers.json";
    private Map<Integer, Passenger> passengers;
    private ObjectMapper objectMapper;

    public PassengerData() {
        this.passengers = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.findAndRegisterModules();

        loadPassengersFromFile();
    }

    private void loadPassengersFromFile() {
        File file = new File(PASSENGERS_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                this.passengers = objectMapper.readValue(
                        file,
                        objectMapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, Passenger.class)
                );
                System.out.println("Pasajeros cargados exitosamente desde " + PASSENGERS_FILE);
            } catch (IOException e) {
                System.err.println("Error al cargar pasajeros: " + e.getMessage());
            }
        } else {
            System.out.println("Archivo de pasajeros no encontrado o vacío. Se inicia una colección nueva.");
        }
    }

    public void savePassengersToFile() {
        try {
            objectMapper.writeValue(new File(PASSENGERS_FILE), passengers);
            System.out.println("Pasajeros guardados exitosamente en " + PASSENGERS_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar pasajeros: " + e.getMessage());
        }
    }

    public boolean registerPassenger(Passenger passenger) {
        if (passengers.containsKey(passenger.getId())) {
            System.out.println("Ya existe un pasajero con el ID " + passenger.getId());
            return false;
        }

        passengers.put(passenger.getId(), passenger);
        savePassengersToFile();
        return true;
    }

    public boolean updatePassenger(Passenger updatedPassenger) {
        if (!passengers.containsKey(updatedPassenger.getId())) {
            System.out.println("No se encontró un pasajero con el ID " + updatedPassenger.getId());
            return false;
        }

        passengers.put(updatedPassenger.getId(), updatedPassenger);
        savePassengersToFile();
        System.out.println("Pasajero actualizado correctamente.");
        return true;
    }

    public boolean deletePassenger(int id) {
        if (!passengers.containsKey(id)) {
            System.out.println("No se encontró un pasajero con el ID " + id);
            return false;
        }

        passengers.remove(id);
        savePassengersToFile();
        return true;
    }

    public Passenger getPassengerById(int id) {
        return passengers.get(id);
    }

    public Map<Integer, Passenger> getAllPassengers() {
        return new HashMap<>(passengers);
    }
}

