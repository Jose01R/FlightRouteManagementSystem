package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.common.Flight;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Utility;

public class FlightData {

    private static final String FLIGHT_FILE = "flights.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Carga todos los vuelos desde el archivo JSON en un Map.
     *
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static Map<Integer, Flight> loadFlightsToMap() throws IOException {
        // --- ADJUSTMENT 1: Use Utility for file path ---
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FLIGHT_FILE);
        File file = filePath.toFile(); //Convertir ruta a archivo para ObjectMapper

        if (file.exists() && file.length() > 0) {
            try {

                //retornamos mapa de objetos Flight, mapeados por su número de vuelo
                return objectMapper.readValue(
                        file,
                        new TypeReference<HashMap<Integer, Flight>>() {}
                );

            } catch (IOException e) {
                System.err.println("Error al cargar vuelos desde el archivo: " + e.getMessage());
                e.printStackTrace();
                throw e; //Relanza excepción
            }
        } else {
            System.out.println("Archivo de vuelos no encontrado o vacío. Se inicia una colección nueva.");
        }
        return new HashMap<>(); //Retorna mapa vacío si no hay archivo
    }

    /**
     * Guarda un Map de objetos Flight en el archivo
     * Sobrescribe el contenido actual del archivo
     *
     * @throws IOException Si ocurre un error al escribir en el archivo
     */
    public static void saveFlightsFromMap(Map<Integer, Flight> flightsToSave) throws IOException {

        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FLIGHT_FILE);
        File file = filePath.toFile(); //Convertir ruta a archivo para ObjectMapper

        try {
            Files.createDirectories(filePath.getParent());
            objectMapper.writeValue(file, flightsToSave);
            System.out.println("Vuelos guardados exitosamente en " + FLIGHT_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar vuelos en el archivo: " + e.getMessage());
            e.printStackTrace();
            throw e; //Relanza excepción
        }
    }
}