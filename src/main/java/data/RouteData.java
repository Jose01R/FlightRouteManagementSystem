package data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.common.Route;
import util.Utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RouteData {

    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";
    private static final String ROUTE_FILE_NAME = "routes.json";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Path routeFilePath;
    static {
        try {
            routeFilePath = Utility.getFilePath(DATA_DIRECTORY, ROUTE_FILE_NAME);
            //crea el directorio aquí si no existe
            Files.createDirectories(routeFilePath.getParent());
        } catch (IOException e) {
            System.err.println("Error al inicializar la ruta del archivo de rutas: " + e.getMessage());

            throw new RuntimeException("No se pudo crear o verificar el directorio de datos para rutas.", e);
        }
    }


    /**
     * Carga las rutas desde el archivo y las devuelve en un Map
     * key == el ID de ruta
     *
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static Map<String, Route> loadRoutesToMap() throws IOException {
        File file = routeFilePath.toFile();

        //Verifique la existencia del archivo y si está vacío
        if (Files.exists(routeFilePath) && Files.size(routeFilePath) > 0) {
            try {
                //retornamos mapa de Routes, mapeados por su ID de ruta
                return objectMapper.readValue(
                        file,
                        new TypeReference<HashMap<String, Route>>() {}
                );
            } catch (IOException e) {
                System.err.println("Error al cargar rutas desde el archivo '" + ROUTE_FILE_NAME + "': " + e.getMessage());
                e.printStackTrace();
                throw e; //Relanza la excepción 
            }
        } else {
            System.out.println("Archivo de rutas '" + ROUTE_FILE_NAME + "' no encontrado o vacío. Se inicia una colección nueva.");
        }
        return new HashMap<>(); //Retorna HashMap vacío si el archivo no existe o está vacío
    }

    /**
     * Guarda un Map de rutas en el archivo JSON.
     * Sobrescribe el contenido existente del archivo con el mapa proporcionado.
     *
     * @param routesToSave El Map de rutas a guardar.
     * @throws IOException Si ocurre un error al escribir en el archivo.
     */

    public static void saveRoutesFromMap(Map<String, Route> routesToSave) throws IOException {
        try {
            Files.createDirectories(routeFilePath.getParent());
            objectMapper.writeValue(routeFilePath.toFile(), routesToSave); // Use the static final Path
            System.out.println("Rutas guardadas exitosamente en '" + ROUTE_FILE_NAME + "'.");
        } catch (IOException e) {
            System.err.println("Error al guardar rutas en el archivo '" + ROUTE_FILE_NAME + "': " + e.getMessage());
            e.printStackTrace();
            throw e; //Relanza la excepción
        }
    }
}