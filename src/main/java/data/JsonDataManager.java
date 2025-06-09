package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.Node;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Type; // Importante para Gson y TypeTokens
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidad para gestionar la lectura y escritura de datos genéricos
 * desde y hacia archivos JSON, utilizando una DoublyLinkedList para el manejo en memoria.
 * Sigo probando
 */
public class JsonDataManager {

    // Configuración de Gson para serialización y deserialización
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Lee datos de un archivo JSON y los carga en una DoublyLinkedList.
     * Esto es crucial para que Gson sepa cómo deserializar la lista genérica.
     * @return Una DoublyLinkedList que contiene los objetos leídos del archivo.
     * @throws IOException Si ocurre un error de entrada/salida durante la lectura del archivo.
     */
    public static DoublyLinkedList readDataIntoDoublyLinkedList(String fileName, Type listType) throws IOException {
        // Inicializa una nueva lista doblemente enlazada
        DoublyLinkedList list = new DoublyLinkedList();

        // Obtiene la ruta del archivo
        Path filePath = Paths.get(fileName);

        // Si el archivo existe, procede a leer su contenido
        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath)) {
                // Deserializa el JSON a una List<?> (lista de tipo genérico desconocido en este punto)
                List<?> tempJavaList = gson.fromJson(reader, listType);

                // Si la lista deserializada no es nula, añade cada elemento a la DoublyLinkedList
                if (tempJavaList != null) {
                    for (Object item : tempJavaList) {
                        list.add(item); // Se asume que DoublyLinkedList.add puede manejar Objects
                    }
                }
            }
        }
        return list; // Retorna la lista doblemente enlazada con los datos cargados
    }

    /**
     * Escribe los datos de una DoublyLinkedList a un archivo JSON.
     *
     *  fileName El nombre del archivo JSON donde se guardarán los datos.
     *  list La DoublyLinkedList que contiene los objetos a serializar.
     *  <T> El tipo de los objetos contenidos en la DoublyLinkedList (ej. Airport, Flight).
     * Este tipo genérico se utiliza para el casting seguro al convertir la lista doblemente enlazada
     * a una lista estándar de Java para la serialización.
     * @throws IOException Si ocurre un error de entrada/salida durante la escritura del archivo.
     */
    public static <T> void writeDataFromDoublyLinkedList(String fileName, DoublyLinkedList list) throws IOException {
        // Crea una lista estándar  para almacenar los datos antes de serializarlos
        List<T> dataToSave = new ArrayList<>();

        // Si la DoublyLinkedList no está vacía, itera sobre ella y añade sus elementos a la lista estándar
        if (!list.isEmpty()) {
            Node current = list.getFirstNode(); // Obtiene el primer nodo de la lista doblemente enlazada
            while (current != null) {
                // Realiza un casting al tipo genérico T.
                // son del tipo T cuando se llama a este metodo.
                dataToSave.add((T) current.data);
                current = current.next; // Avanza al siguiente nodo
            }
        }

        // Obtiene la ruta del archivo
        Path filePath = Paths.get(fileName);

        // Escribe la lista estándar (con los datos actualizados) en el archivo JSON
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(dataToSave, writer);
        }
    }
}

