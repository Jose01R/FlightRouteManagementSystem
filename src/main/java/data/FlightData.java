package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Asegúrate de que esta dependencia esté bien configurada
import domain.common.Flight;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlightData {

    private static final String FLIGHT_FILE = "flight.json";
    private ObjectMapper objectMapper;

    public FlightData() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Asegúrate de que JavaTimeModule esté registrado para LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());
        // findAndRegisterModules() es un método más general, registerModule es específico
        // this.objectMapper.findAndRegisterModules(); // Si no funciona registerModule, prueba este
    }

    // Método para cargar vuelos del archivo en un Map
    public Map<Integer, Flight> loadFlightsToMap() {
        File file = new File(FLIGHT_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                // Deserializa directamente a un HashMap<Integer, Flight>
                return objectMapper.readValue(
                        file,
                        new TypeReference<HashMap<Integer, Flight>>() {}
                );
            } catch (IOException e) {
                System.err.println("Error al cargar vuelos desde el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Archivo de vuelos no encontrado o vacío. Se inicia una colección nueva.");
        }
        return new HashMap<>(); // Retorna un mapa vacío si no hay archivo o hay error
    }

    // Método para guardar un Map de vuelos en el archivo
    public void saveFlightsFromMap(Map<Integer, Flight> flightsToSave) {
        try {
            objectMapper.writeValue(new File(FLIGHT_FILE), flightsToSave);
            System.out.println("Vuelos guardados exitosamente en " + FLIGHT_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar vuelos en el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
