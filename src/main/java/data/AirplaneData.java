package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.common.Airplane;
import domain.linkedlist.ListException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import util.Utility;

public class AirplaneData {

    private static final String FILE_NAME = "airplanes.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    //Usa una única instancia de ObjectMapper, configurada para pretty printing y java.time
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Carga todos los aviones desde el archivo
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static List<Airplane> getAllAirplanesAsList() throws IOException {
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        if (Files.exists(filePath)) {
            try {
                //Lee el contenido del archivo y lo deserializa a una lista de Airplane
                List<Airplane> airplanes = objectMapper.readValue(Files.readString(filePath), new TypeReference<List<Airplane>>() {});

                //retornamos lista de Airplane
                return airplanes != null ? airplanes : new ArrayList<>();
            } catch (IOException e) {
                System.err.println("Error al leer los aviones del archivo JSON: " + e.getMessage());
                throw e; //Relanza la excepción para que service la maneje
            }
        }
        return new ArrayList<>(); //Si el archivo no existe, retorna una lista vacía
    }

    /**
     * Guarda una colección de objetos Airplane en el archivo
     * Sobrescribe el contenido actual del archivo
     *
     * @throws IOException Si ocurre un error al escribir en el archivo
     */
    public static void saveAllAirplanesFromList(Collection<Airplane> airplanes) throws IOException {
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        try {
            //Convierte la colección de aviones a una cadena JSON y la escribe en el archivo
            Files.writeString(filePath, objectMapper.writeValueAsString(airplanes));
        } catch (IOException e) {
            System.err.println("Error al guardar los aviones en el archivo JSON: " + e.getMessage());
            throw e; //Relanza excepción
        }
    }

}