package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.common.Airport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import util.Utility;
import util.LinkedQueueAdapter;

public class AirportsData {

    private static final String FILE_NAME = "airports.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Carga todos los aeropuertos desde el archivo JSON como una lista de Airport
     *
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static List<Airport> getAllAirportsAsList() throws IOException {
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        if (Files.exists(filePath) && Files.size(filePath) > 0) { //Comprobamos si hay un archivo vacío
            try {
                //Retorna lista de Airports
                return objectMapper.readValue(Files.readString(filePath), new TypeReference<List<Airport>>() {});
            } catch (IOException e) {
                System.err.println("Error al leer aeropuertos del archivo JSON: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        return new ArrayList<>(); //Si el archivo no existe o está vacío, devuelve lista vacía
    }

    /**
     * Guarda una colección de objetos Airport en el archivo JSON
     * Sobrescribe el contenido actual del archivo
     *
     * @param airports La colección de aeropuertos a guardar
     * @throws IOException Si ocurre un error al escribir en el archivo
     */
    public static void saveAllAirportsFromList(Collection<Airport> airports) throws IOException {
        Path filePath = Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, objectMapper.writeValueAsString(airports));
            System.out.println("Aeropuertos guardados exitosamente en " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error al guardar aeropuertos en el archivo JSON: " + e.getMessage());
            e.printStackTrace(); //
            throw e; //Relanzamos la excepcion
        }
    }


}