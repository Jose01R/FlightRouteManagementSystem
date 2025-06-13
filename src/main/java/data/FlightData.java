package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Asegúrate de que esta dependencia esté bien configurada
import domain.common.Flight;
import domain.linkedlist.SinglyLinkedList;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightData {

    private static final String FLIGHT_FILE = "flight.json";
    private ObjectMapper objectMapper;
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";
    public FlightData() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.objectMapper.registerModule(new JavaTimeModule());


    }

    //Método para cargar vuelos del archivo en un Map
    public Map<Integer, Flight> loadFlightsToMap() {
        Map<Integer, Flight> flightsMap = new HashMap<>();

        try {
            File file = util.Utility.getFilePath(DATA_DIRECTORY, FLIGHT_FILE).toFile();

            if (file.exists() && file.length() > 0) {
                // Deserializa directamente a un HashMap<Integer, Flight>
                flightsMap = objectMapper.readValue(
                        file,
                        new TypeReference<HashMap<Integer, Flight>>() {}
                );
            } else {
                System.out.println("Archivo de vuelos no encontrado o vacío. Se inicia una colección nueva.");
            }

        } catch (IOException e) {
            System.err.println("Error al leer vuelos del archivo: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Error al deserializar vuelos al mapa: " + e.getMessage());
            e.printStackTrace();
        }

        return flightsMap;
    }


    // Método para guardar un Map de vuelos en el archivo
    public void saveFlightsFromMap(Map<Integer, Flight> flightsToSave) {
        try {
            objectMapper.writeValue(util.Utility.getFilePath(DATA_DIRECTORY, FLIGHT_FILE).toFile(), flightsToSave);
            System.out.println("Vuelos guardados exitosamente en " + FLIGHT_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar vuelos en el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


