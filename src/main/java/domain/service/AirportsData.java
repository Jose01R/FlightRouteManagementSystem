package domain.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import domain.common.Airport;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AirportsData {

    private static final String FILE_NAME = "airports.json"; // Nombre del archivo JSON
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; // Nombre directorio

    

    /**
     * Crea un nuevo aeropuerto y lo ingresa al "airports.json"
     * si el archivo "airports.json" ya tiene datos, lo que hace es leerlos y guardarlos previamente en una
     * "Lista Enlazada Doble", y luego el nuevo aeropuerto lo añade a la "Lista Enlazada Doble"
     * Al finalizar los datos de la "Lista Enlazada Doble" son ingresados al "airports.json",
     */
    public static void createAirport(Airport airport) throws IOException, ListException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

        Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME); //obtenemos ruta

        //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath)) {
                Type listType = new TypeToken<List<Airport>>() {}.getType();
                List<Airport> tempJavaList = gson.fromJson(reader, listType);

                //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                if (tempJavaList != null) {
                    for (Airport a : tempJavaList) {
                        airportsDoublyList.add(a);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        airportsDoublyList.add(airport);

        //Añadir el nuevo aeropuerto en la "Lista Enlazada Doble"
        if(airportsDoublyList.contains(airport.getCode())){
            throw new ListException("The airport already exists");
        }//cambiarlo por boolean si lleva false no se inserto porque el codigo del aeropuerto ya existe

        //Convertir la "Lista Enlazada Doble" a una lista estandar para serializar en en formato Json
        List<Airport> airportsToSave = new ArrayList<>();
        if (!airportsDoublyList.isEmpty()) {
            Node current = airportsDoublyList.getFirstNode(); //accesos al primero nodo o con el metodo de getNode(1)
            while (current != null) {
                airportsToSave.add((Airport) current.data);
                current = current.next;
            }
        }

        //Guardar la lista actualizada en el archivo
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(airportsToSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un nuevo aeropuerto y lo ingresa al "airports.json"
     * si el archivo "airports.json" ya tiene datos, lo que hace es leerlos y guardarlos previamente en una
     * "Lista Enlazada Doble", y luego busca en la "Lista Enlazada Doble" si hay un Aeropuerto con el mismo ID que le paso
     * Si esta el Aeropuerto, lo que hace es elimimar y cambia de valor un boleano en verdadero
     * Al finalizar los datos de la "Lista Enlazada Doble" son ingresados al "airports.json",
     */
    public static boolean deleteAirport(int codeAirport) throws ListException, IOException {
        boolean deleted = false;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

        Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

        //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath)) {
                Type listType = new TypeToken<List<Airport>>() {}.getType();
                List<Airport> tempJavaList = gson.fromJson(reader, listType);

                //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                if (tempJavaList != null) {
                    for (Airport a : tempJavaList) {
                        airportsDoublyList.add(a);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Verifica si el avión existe por su ID
        Airport airport = new Airport(codeAirport);
        if(airportsDoublyList.contains(airport)){
            airportsDoublyList.remove(airport);
            deleted = true;
        }

        //Convertir la "Lista Enlazada Doble" a una lista estandar para serializar en en formato Json
        List<Airport> airportsToSave = new ArrayList<>();
        if (!airportsDoublyList.isEmpty()) {
            Node current = airportsDoublyList.getFirstNode(); // Access the first node of the DoublyLinkedList
            while (current != null) {
                airportsToSave.add((Airport) current.data);
                current = current.next;
            }
        }

        //Guardar la lista actualizada en el archivo
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(airportsToSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deleted;
    }

    public static boolean editAirport(Airport airport) throws ListException, IOException {
        boolean editAirport = false;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

        Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

        //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath)) {
                Type listType = new TypeToken<List<Airport>>() {}.getType();
                List<Airport> tempJavaList = gson.fromJson(reader, listType);

                //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                if (tempJavaList != null) {
                    for (Airport a : tempJavaList) {
                        airportsDoublyList.add(a);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Verifica si el avión existe por su ID
        if(airportsDoublyList.contains(airport)){
            //indice del aeropuerto
            int i = airportsDoublyList.indexOf(airport);

            // Si el índice es válido (es decir, el aeropuerto fue encontrado)
            if (i != -1) {
                // Obtenemos el nodo del aeropuerto en la posición encontrada
                Node airportNode = airportsDoublyList.getNode(i);

                // Verifica que el nodo y sus datos no sean nulos y que los datos sean de tipo Airport
                if (airportNode != null && airportNode.data instanceof Airport) {
                    // cast de los datos a Airport y actualizamos el estado
                    airportNode.data =  airport;
                    editAirport = true;
                }//fin del if
            }//fin if
        }//fin if

        //Convertir la "Lista Enlazada Doble" a una lista estandar para serializar en en formato Json
        List<Airport> airportsToSave = new ArrayList<>();
        if (!airportsDoublyList.isEmpty()) {
                Node current = airportsDoublyList.getFirstNode(); // Access the first node of the DoublyLinkedList
                while (current != null) {
                    airportsToSave.add((Airport) current.data);
                    current = current.next;
                }
            }

            //Guardar la lista actualizada en el archivo
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(airportsToSave, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return editAirport;
        }

        //cambia el status del Aeropuerto en cue
        public static boolean changeStatusAirport(int codeAirport) throws ListException, IOException {
            boolean updated = false;

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

            Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

            //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
            if (Files.exists(filePath)) {
                try (Reader reader = Files.newBufferedReader(filePath)) {
                    Type listType = new TypeToken<List<Airport>>() {}.getType();
                    List<Airport> tempJavaList = gson.fromJson(reader, listType);

                    //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                    if (tempJavaList != null) {
                        for (Airport a : tempJavaList) {
                            airportsDoublyList.add(a);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Verifica si el avión existe por su ID
            Airport airport = new Airport(codeAirport);
            if(airportsDoublyList.contains(airport)){
                //indice del aeropuerto
                int i = airportsDoublyList.indexOf(airport);

                // Si el índice es válido (es decir, el aeropuerto fue encontrado)
                if (i != -1) {
                    // Obtenemos el nodo del aeropuerto en la posición encontrada
                    Node airportNode = airportsDoublyList.getNode(i);

                    // Verifica que el nodo y sus datos no sean nulos y que los datos sean de tipo Airport
                    if (airportNode != null && airportNode.data instanceof Airport) {
                        // cast de los datos a Airport y actualizamos el estado
                        Airport airportToUpdate = (Airport) airportNode.data;

                        switch (airportToUpdate.getStatus()){
                            case "Active":
                                airportToUpdate.setStatus("Inactive");
                                updated = true; // El estado se actualizó correctamente

                                break;
                            case "Inactive":
                                airportToUpdate.setStatus("Active");
                                updated = true; // El estado se actualizó correctamente
                                break;
                        }//fin del switch
                    }//fin del if
                }//fin if
            }//fin if

            //Convertir la "Lista Enlazada Doble" a una lista estandar para serializar en en formato Json
            List<Airport> airportsToSave = new ArrayList<>();
            if (!airportsDoublyList.isEmpty()) {
                Node current = airportsDoublyList.getFirstNode(); // Access the first node of the DoublyLinkedList
                while (current != null) {
                    airportsToSave.add((Airport) current.data);
                    current = current.next;
                }
            }

            //Guardar la lista actualizada en el archivo
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(airportsToSave, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return updated;
        }

        //hace una lista de Aeropuertos
        public static SinglyLinkedList listAirports(String status) throws ListException, IOException {
            SinglyLinkedList list = new SinglyLinkedList();// Listar aeropuertos (activos, inactivos, o ambos)

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

            Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

            //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
            if (Files.exists(filePath)) {
                try (Reader reader = Files.newBufferedReader(filePath)) {
                    Type listType = new TypeToken<List<Airport>>() {}.getType();
                    List<Airport> tempJavaList = gson.fromJson(reader, listType);

                    //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                    if (tempJavaList != null) {
                        for (Airport a : tempJavaList) {
                            airportsDoublyList.add(a);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Verifica si el avión existe por su ID
            if(!airportsDoublyList.isEmpty()){
                int i = 1;
                Node airportNode = airportsDoublyList.getNode(1);
                while(airportNode!=null){
                    // Verifica que el nodo y sus datos no sean nulos y que los datos sean de tipo Airport
                    if (airportNode.data instanceof Airport) {
                        // cast de los datos a Airport y guardamos los status en un "Lista Enlazada Simple"
                        Airport airportToUpdate = (Airport) airportNode.data;
                        if(airportToUpdate.getStatus().equalsIgnoreCase(status) && status.equalsIgnoreCase("Active")){
                            list.add(airportToUpdate);
                        } else if (airportToUpdate.getStatus().equalsIgnoreCase(status) && status.equalsIgnoreCase("Inactive")) {
                            list.add(airportToUpdate);
                        } else if (status.equalsIgnoreCase("Ambos")) {
                            list.add(airportToUpdate);
                        }
                    }//fin if
                    airportNode = airportNode.next;
                }//fin while
            }//fin if

            //Convertir la "Lista Enlazada Doble" a una lista estandar para serializar en en formato Json
            List<Airport> airportsToSave = new ArrayList<>();
            if (!airportsDoublyList.isEmpty()) {
                Node current = airportsDoublyList.getFirstNode(); // Access the first node of the DoublyLinkedList
                while (current != null) {
                    airportsToSave.add((Airport) current.data);
                    current = current.next;
                }
            }

            //Guardar la lista actualizada en el archivo
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                gson.toJson(airportsToSave, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return list;
        }

        // Me da lo que esta en el json registrado, para actualizar el tableView
        public static DoublyLinkedList getElements() throws IOException {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            DoublyLinkedList airportsDoublyList = new DoublyLinkedList();

            Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

            //Si el archivo existe, cargar la lista en la "Lista Enlazada Doble"
            if (Files.exists(filePath)) {
                try (Reader reader = Files.newBufferedReader(filePath)) {
                    Type listType = new TypeToken<List<Airport>>() {}.getType();
                    List<Airport> tempJavaList = gson.fromJson(reader, listType);

                    //Añade elemento de la lista de "airports.json" a la "Lista Enlazada Doble"
                    if (tempJavaList != null) {
                        for (Airport a : tempJavaList) {
                            airportsDoublyList.add(a);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return airportsDoublyList;
        }

    /**
     * Obtiene un Airport por su código
     * Lee el archivo JSON para buscar el aeropuerto
     */

    public static Airport getAirportByCode(int code) throws IOException, ListException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path filePath = util.Utility.getFilePath(DATA_DIRECTORY, FILE_NAME);

        if (Files.exists(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath)) {
                Type listType = new TypeToken<List<Airport>>() {}.getType();
                List<Airport> tempJavaList = gson.fromJson(reader, listType);

                if (tempJavaList != null) {
                    for (Airport a : tempJavaList) {
                        if (a.getCode() == code) {
                            return a; //RETORNAMOS UN AIRPORT si se encontro
                        }
                    }
                }
            }
        }
        return null; //AIRPORT no encontrado
    }

}
