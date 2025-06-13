package domain.service;

import domain.common.Airport;
import domain.common.Route;
import domain.graph.GraphException;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;
import domain.linkedlist.SinglyLinkedList;
import domain.service.AirportsData; // Para limpiar el archivo de aeropuertos
import org.junit.jupiter.api.AfterEach; // Usar AfterEach para limpiar después de cada test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//import ucr.flightroutemanagementsystem.cli.AirNetworkSimulation;

import util.Utility;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AirNetworkSimulationTest {

    private static final String AIRPORTS_FILE_NAME = "airports.json";
    private static final String ROUTES_FILE_NAME = "routes.json";
    private static final String DATA_DIRECTORY = "JSON_FILES_DATA";



    @Test
    void testFullSimulationFlow() throws IOException, ListException, InterruptedException {

        System.out.println("--- Iniciando Simulación de Red Aérea ---\n");

        // 1. Instanciar servicios de datos y de red
        AirportsData airportsData = new AirportsData(); // Usando tu clase original
        RouteData routeData = new RouteData();
        AirNetworkService airNetworkService = new AirNetworkService();

        try {
            // --- PASO 1: Crear y Persistir Aeropuertos (si no existen) ---
            System.out.println("-> Creando y guardando aeropuertos en airports.json...");
            Airport sjo = new Airport(101, "Juan Santamaria Intl Airport", "Costa Rica", "Active");
            Airport pty = new Airport(202, "Tocumen Intl Airport", "Panama", "Active");
            Airport lax = new Airport(303, "Los Angeles Intl Airport", "USA", "Active");
            Airport mia = new Airport(404, "Miami Intl Airport", "USA", "Active");
            Airport bog = new Airport(505, "El Dorado Intl Airport", "Colombia", "Active");
            Airport mad = new Airport(606, "Adolfo Suarez Madrid-Barajas Airport", "Spain", "Active");

            // Guardar aeropuertos. AirportsData.createAirport() añade al JSON y maneja duplicados
            // Captura las excepciones de ListException si el aeropuerto ya existe para evitar detener la simulación
            try { airportsData.createAirport(sjo); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            try { airportsData.createAirport(pty); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            try { airportsData.createAirport(lax); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            try { airportsData.createAirport(mia); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            try { airportsData.createAirport(bog); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            try { airportsData.createAirport(mad); } catch (ListException e) { System.out.println("Advertencia: " + e.getMessage()); }
            System.out.println("Aeropuertos añadidos/verificados en airports.json.\n");


            // --- PASO 2: Crear y Persistir Rutas (si no existen) ---
            System.out.println("-> Creando y guardando rutas en routes.json...");
            Map<String, Route> currentRoutes = routeData.loadRoutesToMap(); // Cargar rutas existentes para añadir

            // Definir rutas (usando la clase Route ajustada con origin/destination codes)
            Route route1 = new Route("CM100", sjo.getCode(), pty.getCode(), "Copa Airlines", 1.5, 800, 250.0, LocalTime.of(8, 0), LocalTime.of(9, 30));
            Route route2 = new Route("CM200", pty.getCode(), lax.getCode(), "Copa Airlines", 7.0, 4800, 500.0, LocalTime.of(10, 0), LocalTime.of(17, 0));
            Route route3 = new Route("AA300", sjo.getCode(), mia.getCode(), "American Airlines", 3.0, 1500, 350.0, LocalTime.of(9, 0), LocalTime.of(12, 0));
            Route route4 = new Route("DL400", mia.getCode(), lax.getCode(), "Delta Airlines", 5.5, 3900, 400.0, LocalTime.of(13, 0), LocalTime.of(18, 30));
            Route route5 = new Route("AV500", pty.getCode(), bog.getCode(), "Avianca", 1.0, 700, 100.0, LocalTime.of(14, 0), LocalTime.of(15, 0));
            Route route6 = new Route("IB600", bog.getCode(), mad.getCode(), "Iberia", 9.0, 8000, 700.0, LocalTime.of(20, 0), LocalTime.of(11, 0));
            Route route7 = new Route("UA700", lax.getCode(), sjo.getCode(), "United Airlines", 6.0, 4000, 450.0, LocalTime.of(19,0), LocalTime.of(1,0));

            // Añadir rutas al mapa (si el ID de ruta ya existe, se sobrescribe)
            currentRoutes.put(route1.getRouteId(), route1);
            currentRoutes.put(route2.getRouteId(), route2);
            currentRoutes.put(route3.getRouteId(), route3);
            currentRoutes.put(route4.getRouteId(), route4);
            currentRoutes.put(route5.getRouteId(), route5);
            currentRoutes.put(route6.getRouteId(), route6);
            currentRoutes.put(route7.getRouteId(), route7);

            routeData.saveRoutesFromMap(currentRoutes); // Guardar todas las rutas (incluyendo las nuevas)
            System.out.println("Rutas añadidas/verificadas en routes.json.\n");


            // --- PASO 3: Cargar la Red Aérea en Memoria (Grafo) ---
            System.out.println("-> Cargando aeropuertos y rutas en el grafo en memoria...");

            // Cargar aeropuertos primero al grafo desde DoublyLinkedList
            DoublyLinkedList allPersistedAirportsList = airportsData.getElements(); // Usando tu método existente
            Node currentAirportNode = allPersistedAirportsList.getFirstNode();
            while (currentAirportNode != null) {
                if (currentAirportNode.data instanceof Airport) {
                    airNetworkService.addAirport((Airport) currentAirportNode.data);
                }
                currentAirportNode = currentAirportNode.next;
            }

            // Luego cargar rutas al grafo
            Map<String, Route> allPersistedRoutes = routeData.loadRoutesToMap();
            for (Route route : allPersistedRoutes.values()) {
                // airNetworkService.addRoute ahora espera los códigos de aeropuerto, no los objetos completos
                airNetworkService.addRoute(route.getOriginAirportCode(), route.getDestinationAirportCode(), route);
            }
            System.out.println("Grafo de red aérea cargado exitosamente en memoria.\n");

            System.out.println("--- Estado actual del Grafo de Rutas ---\n");
            System.out.println(airNetworkService); // Esto imprimirá el contenido del grafo


            // --- PASO 4: Realizar Búsquedas de Rutas Más Cortas (Dijkstra) ---
            System.out.println("\n--- Realizando Búsquedas de Rutas Más Cortas (Dijkstra) ---\n");

            // Ejemplo 1: SJO a LAX por Duración
            System.out.println("Buscando ruta más corta (duración) de SJO (101) a LAX (303):");
            List<Integer> path1 = airNetworkService.findShortestRoute(sjo.getCode(), lax.getCode(), "duration");
            if (path1 != null) {
                System.out.println("Ruta encontrada: " + path1);
            } else {
                System.out.println("No se encontró ruta de SJO a LAX.");
            }
            System.out.println("----------------------------------------\n");

            // Ejemplo 2: SJO a MAD por Precio
            System.out.println("Buscando ruta más corta (precio) de SJO (101) a MAD (606):");
            List<Integer> path2 = airNetworkService.findShortestRoute(sjo.getCode(), mad.getCode(), "price");
            if (path2 != null) {
                System.out.println("Ruta encontrada: " + path2);
            } else {
                System.out.println("No se encontró ruta de SJO a MAD.");
            }
            System.out.println("----------------------------------------\n");

            // Ejemplo 3: PTY a MIA (no hay ruta directa o conocida aquí)
            System.out.println("Buscando ruta más corta (distancia) de PTY (202) a MIA (404):");
            List<Integer> path3 = airNetworkService.findShortestRoute(pty.getCode(), mia.getCode(), "distance");
            if (path3 != null) {
                System.out.println("Ruta encontrada: " + path3);
            } else {
                System.out.println("No se encontró ruta de PTY a MIA.");
            }
            System.out.println("----------------------------------------\n");

            // Ejemplo 4: MIA a SJO (existencia de ruta)
            System.out.println("Verificando si existe ruta directa de MIA (404) a SJO (101): " + airNetworkService.containsRoute(mia.getCode(), sjo.getCode()));

            // --- PASO 5: Eliminar un Aeropuerto y ver el impacto ---
            System.out.println("\n-> Eliminando el aeropuerto BOG (505) de airports.json y del grafo...");
            // Eliminar del JSON
            // Eliminar del grafo en memoria y sus rutas

            airNetworkService.removeAirportFromGraph(bog.getCode());
            boolean bogDeleted = airportsData.deleteAirport(bog.getCode());
            if (bogDeleted) {
                System.out.println("Aeropuerto BOG (505) eliminado del airports.json.");
            } else {
                System.out.println("Aeropuerto BOG (505) NO se encontró en airports.json para eliminar.");
            }

            System.out.println("Aeropuerto BOG (505) eliminado del grafo en memoria y sus aristas.");
            System.out.println("\n--- Estado del Grafo después de eliminar BOG ---\n");
            System.out.println(airNetworkService);

            // Intenta buscar la ruta SJO a MAD de nuevo
            System.out.println("Buscando ruta más corta (precio) de SJO (101) a MAD (606) después de eliminar BOG:");
            List<Integer> pathAfterRemoval = airNetworkService.findShortestRoute(sjo.getCode(), mad.getCode(), "price");
            if (pathAfterRemoval != null) {
                System.out.println("Ruta encontrada: " + pathAfterRemoval);
            } else {
                System.out.println("No se encontró ruta de SJO a MAD (como se esperaba si BOG era crítico).");
            }
            System.out.println("----------------------------------------\n");


        } catch (IOException | GraphException | ListException e) {
            System.err.println("\nError durante la simulación");
            e.printStackTrace();
        }

        System.out.println("\n--- Simulación de Red Aérea Finalizada ---");
    }

}
