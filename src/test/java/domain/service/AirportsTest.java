package domain.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import domain.common.Airport;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static domain.service.AirportsData.*;

class AirportsTest {

//    @BeforeEach
//        // Este método se ejecutará antes de cada test
//    void setUp() throws IOException {
//        Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);
//        if (Files.exists(filePath)) {
//            Files.delete(filePath);
//            System.out.println("Archivo airports.json (en data/) limpiado antes del test.");
//        }
//        Files.createDirectories(filePath.getParent());
//    }

    private static final String FILE_NAME = "airports.json"; // Nombre del archivo JSON
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; // Nombre directorio


    @Test
    void insertar_airport(){
        Airport airport1 = new Airport(11, "Aeropuerto de Dortmund", "Alemania", "Active");
        Airport airport2 = new Airport(12, "Aeropuerto de Aalborg", "Dinamarca", "Active");
        Airport airport3 = new Airport(13, "Aeropuerto de La Coruña", "España", "Inactive");
        airport1.departuresBoard.add("hola");
        airport1.departuresBoard.add("adios");
        airport2.departuresBoard.add("chao");
        airport2.departuresBoard.add("goodbye");
        airport3.departuresBoard.add("frances");
        airport3.departuresBoard.add("español");

        //prueba de ver Aeropuertos
        try {
            //prueba de crear Aeropuertos
            createAirport(airport1);
            createAirport(airport2);

            //prueba de ver Aeropuertos
            //Leer desde archivo
            Gson gson = new Gson();
            String contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            Type listType = new TypeToken<List<Airport>>() {}.getType();
            List<Airport> listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuertos leídos desde archivo:");
            for (Airport a : listaLeida) {
                System.out.println("----------------------------------");
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
            }

            //prueba de ver Aeropuertos después de eliminar un Aeropuerto
            System.out.println("-----------------------------------------------------");
            System.out.println("Se elimino el aeropuerto con el ID=11? " + deleteAirport(11));
            System.out.println("-----------------------------------------------------");
            // Leer desde archivo
            gson = new Gson();
            contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            listType = new TypeToken<List<Airport>>() {}.getType();
            listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuertos leídos desde archivo, después de eliminar un Aeropuerto:");
            for (Airport a : listaLeida) {
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
                System.out.println("----------------------------------");
            }

            //prueba de ver Aeropuertos después de cambiar el status de un Aeropuerto
            System.out.println("-----------------------------------------------------");
            System.out.println("Se cambio el status del aeropuerto con el ID=12? " + changeStatusAirport(12));
            System.out.println("-----------------------------------------------------");
            // Leer desde archivo
            gson = new Gson();
            contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            listType = new TypeToken<List<Airport>>() {}.getType();
            listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuertos leídos desde archivo, después de cambiar el status de un aeropuerto:");
            for (Airport a : listaLeida) {
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
                System.out.println("----------------------------------");
            }

            //prueba de crear Aeropuerto
            createAirport(airport3);

            //prueba de ver Aeropuertos
            //Leer desde archivo
            gson = new Gson();
            contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            listType = new TypeToken<List<Airport>>() {}.getType();
            listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuerto nuevo agregado con el cambio de status y leídos desde archivo:");
            for (Airport a : listaLeida) {
                System.out.println("----------------------------------");
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
            }

            //prueba de ver Aeropuertos después de cambiar el status un Aeropuerto
            System.out.println("-----------------------------------------------------");
            System.out.println("Se cambio el status del aeropuerto con el ID=12? " + changeStatusAirport(12));
            System.out.println("Se cambio el status del aeropuerto con el ID=13? " + changeStatusAirport(13));

            System.out.println("-----------------------------------------------------");
            // Leer desde archivo
            gson = new Gson();
            contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            listType = new TypeToken<List<Airport>>() {}.getType();
            listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuertos leídos desde archivo, después de cambiar el status de un aeropuerto:");
            for (Airport a : listaLeida) {
                System.out.println("----------------------------------");
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
            }

            //prueba de ver Aeropuertos después de editar un Aeropuerto
            System.out.println("-----------------------------------------------------");
            System.out.println("Se edito el aeropuerto con el ID=13? " + editAirport(new Airport(13, "Aeropuerto de Agen-La Garenne", "Francia", "Inactive")));
            System.out.println("-----------------------------------------------------");
            // Leer desde archivo
            gson = new Gson();
            contenido = Files.readString(util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME));
            listType = new TypeToken<List<Airport>>() {}.getType();
            listaLeida = gson.fromJson(contenido, listType);

            System.out.println("Aeropuertos leídos desde archivo, después de cambiar el status de un aeropuerto:");
            for (Airport a : listaLeida) {
                System.out.println("Code: " + a.getCode());
                System.out.println("Name: " + a.getName());
                System.out.println("Country: " + a.getCountry());
                System.out.println("Status: " + a.getStatus());
                System.out.println("Departures Board: " + a.getDeparturesBoard());
                System.out.println("----------------------------------");
            }

            SinglyLinkedList list = listAirports("Active");
            System.out.println(list);


        } catch (IOException | ListException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    void crear(){
        Airport airport1 = new Airport(11, "Aeropuerto de Dortmund", "Alemania", "Active");
        Airport airport2 = new Airport(12, "Aeropuerto de Aalborg", "Dinamarca", "Active");
        Airport airport3 = new Airport(13, "Aeropuerto de La Coruña", "España", "Inactive");
        airport1.departuresBoard.add("hola");
        airport1.departuresBoard.add("adios");

        try {
            createAirport(airport1);
            createAirport(airport2);
            createAirport(airport3);
        } catch (IOException | ListException e) {
            throw new RuntimeException(e);
        }
    }

}