package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;
import util.Utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PassengerData {

    private static final String FILE_NAME = "passengers.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);


    /**
     * Carga todos los pasajeros desde el archivo JSON y los inserta en un árbol AVL
     *
     * @return Un árbol AVL de objetos Passenger. Retorna un AVL vacío si el archivo no existe,
     * está vacío o hay un error al leer/procesar
     *
     * @throws IOException Si ocurre un error al leer el archivo
     */

    public static AVL readPassengers() throws IOException {
        AVL avlPassengers = new AVL();

        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        File file = filePath.toFile();

        if (file.exists() && file.length() > 0) {
            try {
                List<Passenger> passengerList = objectMapper.readValue(file, new TypeReference<List<Passenger>>() {});
                for (Passenger passenger : passengerList) {
                    avlPassengers.add(passenger);
                }
            } catch (IOException e) {
                System.err.println("Error al leer y/o deserializar pasajeros del archivo JSON: " + e.getMessage());
                e.printStackTrace();
                throw e; //Relanza excepción para que el service la maneje
            }
        }
        return avlPassengers;
    }

    /**
     * Guarda todos los pasajeros de un árbol AVL en el archivo
     * Convierte el AVL a una lista para serialización
     *
     * @throws IOException Si ocurre un error al escribir en el archivo o al iterar el árbol
     */

    public static void saveAllPassengers(AVL passengersToSave) throws IOException {
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        File file = filePath.toFile();

        try {
            List<Passenger> passengerList = new ArrayList<>();
            if (passengersToSave != null && !passengersToSave.isEmpty()) {
                for (Object obj : passengersToSave.inOrderNodes1()) {
                    passengerList.add((Passenger) obj);
                }
            }

            Files.createDirectories(filePath.getParent());
            objectMapper.writeValue(file, passengerList);
            System.out.println("Pasajeros guardados exitosamente en " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error al guardar pasajeros en el archivo: " + e.getMessage());
            e.printStackTrace();
            throw e; //Relanza la excepción
        } catch (TreeException e) {
            System.err.println("Error al iterar el árbol AVL de pasajeros para guardar: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to save passengers due to AVL tree error: " + e.getMessage(), e);
        }
    }
}