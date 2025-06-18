package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PassengerData {
    private final String PASSENGERS_FILE = "passengers.json";
    private ObjectMapper objectMapper;
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    public PassengerData() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Lee la lista desde archivo JSON y reconstruye el AVL
    public AVL readPassengers() {
        AVL avlPassengers = new AVL();
        try {
            File file = util.Utility.getFilePath(DATA_DIRECTORY, PASSENGERS_FILE).toFile();
            if (file.exists() && file.length() > 0) {
                List<Passenger> passengerList = objectMapper.readValue(file, new TypeReference<List<Passenger>>() {});
                for (Passenger passenger : passengerList) {
                    avlPassengers.add(passenger);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer pasajeros del archivo: " + e.getMessage());
        }
        return avlPassengers;
    }

    // Guarda el AVL serializándolo como lista en JSON
    public void saveAllPassengers(AVL passengersToSave) {
        try {
            List<Passenger> passengerList = new ArrayList<>();
            if (passengersToSave != null && !passengersToSave.isEmpty()) {
                for (Object obj : passengersToSave.inOrderNodes1()) {
                    passengerList.add((Passenger) obj);
                }
            }
            File file = util.Utility.getFilePath(DATA_DIRECTORY, PASSENGERS_FILE).toFile();
            objectMapper.writeValue(file, passengerList);
            System.out.println("Pasajeros guardados exitosamente en " + PASSENGERS_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar pasajeros en el archivo: " + e.getMessage());
        } catch (TreeException e) {
            System.err.println("Error al iterar el árbol de pasajeros para guardar: " + e.getMessage());
        }
    }
}

