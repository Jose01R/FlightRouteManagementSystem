package domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.common.Route;
import util.Utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteData {

    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; //Nombre del Directorio
    private static final String ROUTE_FILE_NAME = "routes.json"; //Nombre del archivo
    private ObjectMapper objectMapper;
    private Path routeFilePath; //Almacena ruta completa

    /**
     * Constructor de RouteData
     * Inicializa el ObjectMapper y configura para Pretty Printing y soporte de JavaTimeModule
     */
    public RouteData() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Registrar el módulo para manejar tipos de fecha/hora (como LocalTime en Route)
        this.objectMapper.registerModule(new JavaTimeModule());

        try {
            this.routeFilePath = Utility.getFilePath(DATA_DIRECTORY, ROUTE_FILE_NAME); //inicializamos la ruta del archivo
        } catch (IOException e) {
            System.err.println("Error al inicializar la ruta del archivo de rutas: " + e.getMessage());

            throw new RuntimeException("No se pudo crear la ruta al directorio de datos para rutas.", e);
        }
    }

    /**
     * Carga las rutas desde el archivo JSON y las devuelve en un Map
     * La key será el ID de ruta
     */
    public Map<String, Route> loadRoutesToMap() {
        File file = this.routeFilePath.toFile();

        if (file.exists() && file.length() > 0) {
            try {
                //Leemos un object JSON en un HashMap
                //Retorna map K = routeId  - V = la Route
                return objectMapper.readValue(
                        file,
                        new TypeReference<HashMap<String, Route>>() {}
                );
            } catch (IOException e) {
                System.err.println("Error al cargar rutas desde el archivo '" + ROUTE_FILE_NAME + "': " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Archivo de rutas '" + ROUTE_FILE_NAME + "' no encontrado o vacío. Se inicia una colección nueva.");
        }
        return new HashMap<>(); //Retorna un HashMap vacío si el archivo no existe o está vacío
    }

    /**
     * Guarda un Map de rutas en el archivo JSON
     * Sobrescribe el contenido existente del archivo con el mapa proporcionado
     */
    public void saveRoutesFromMap(Map<String, Route> routesToSave) {
        //routesToSave es el Map de rutas a guardar

        try {
            // Se serializa directamente el mapa, lo que resultará en un objeto JSON
            objectMapper.writeValue(this.routeFilePath.toFile(), routesToSave);
            System.out.println("Rutas guardadas exitosamente en '" + ROUTE_FILE_NAME + "'.");
        } catch (IOException e) {
            System.err.println("Error al guardar rutas en el archivo '" + ROUTE_FILE_NAME + "': " + e.getMessage());
            e.printStackTrace();
        }

    }
}