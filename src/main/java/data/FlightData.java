package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.common.Flight;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlightData {

    private static final String FLIGHT_FILE="flight.json";
    private Map<Integer, Flight>flightMap;
    private ObjectMapper objectMapper;

    public FlightData() {
        this.flightMap = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.findAndRegisterModules();

        loadFlightFromFile();
    }


    private void loadFlightFromFile() {

        File file = new File(FLIGHT_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                this.flightMap = objectMapper.readValue(
                        file,
                        objectMapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, Flight.class)
                );
                System.out.println("Pasajeros cargados exitosamente desde " + FLIGHT_FILE);
            } catch (IOException e) {
                System.err.println("Error al cargar pasajeros: " + e.getMessage());
            }
        } else {
            System.out.println("Archivo de pasajeros no encontrado o vacío. Se inicia una colección nueva.");
        }
    }
    public void saveFlightToFile() {
        try {
            objectMapper.writeValue(new File(FLIGHT_FILE), flightMap);
            System.out.println("Pasajeros guardados exitosamente en " + FLIGHT_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar pasajeros: " + e.getMessage());
        }
    }

    public boolean registerFlight(Flight flight) {
        if (flightMap.containsKey(flight.getNumber())) {
            System.out.println("Ya existe un pasajero con el ID " + flight.getNumber());
            return false;
        }

        flightMap.put(flight.getNumber(),flight);
        saveFlightToFile();
        return true;
    }

    public boolean updateFlight(Flight updatedFlight) {
        if (!flightMap.containsKey(updatedFlight.getNumber())) {
            System.out.println("No se encontró un pasajero con el ID " + updatedFlight.getNumber());
            return false;
        }

        flightMap.put(updatedFlight.getNumber(), updatedFlight);
        saveFlightToFile();
        System.out.println("Pasajero actualizado correctamente.");
        return true;
    }

    public boolean deleteFlight(int flightNumber) {
        if (!flightMap.containsKey(flightNumber)) {
            System.out.println("No se encontró un pasajero con el ID " + flightNumber);
            return false;
        }

        flightMap.remove(flightNumber);
        saveFlightToFile();
        return true;
    }

    public Flight getFlightByNumber(int flightNumber) {
        return flightMap.get(flightNumber);
    }

    public Map<Integer, Flight> getAllFlights() {
        return new HashMap<>(flightMap);
    }
}
