package ucr.flightroutemanagementsystem;

import controller.HelloController;
// import controller.flightcontroller.FlightController; // Not directly used here, can remove if not needed elsewhere

import domain.graph.GraphException; // New import for AirNetworkService constructor
import domain.linkedlist.ListException; // New import for service constructors
import domain.linkedqueue.QueueException; // New import for AirportService
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// Import all Data classes
import data.PassengerData;
import data.FlightData;
import data.AirplaneData;
import domain.service.AirportService; // Changed from AirportData to AirportsData for consistency
import domain.service.AirNetworkService;
import data.AirportsData;
import data.RouteData;

// Import all Service classes
import domain.service.PassengerService;
import domain.service.FlightService;
import domain.service.AirplaneService;
import domain.service.AirportService;
import domain.service.AirNetworkService;

public class HelloApplication extends Application {

    // --- Data Layer Instances ---
    private static PassengerData passengerData;
    private static FlightData flightData;
    private static AirplaneData airplaneData;
    private static AirportsData airportsData; // Renamed for consistency
    private static RouteData routeData;

    // --- Service Layer Instances ---
    private static PassengerService passengerService;
    private static FlightService flightService;
    private static AirplaneService airplaneService;
    private static AirportService airportService;
    private static AirNetworkService airNetworkService;


    @Override
    public void start(Stage stage) throws IOException {
        // 1. Initialize all Data layer instances
        passengerData = new PassengerData();
        flightData = new FlightData();
        airplaneData = new AirplaneData(); // Initialize AirplaneData
        airportsData = new AirportsData(); // Initialize AirportsData
        routeData = new RouteData();       // Initialize RouteData

        // 2. Initialize Service layer instances in the correct order of dependencies
        // AirportService does not have external data dependency in its constructor
        // based on the AirplaneService code provided, it's handling data internally.
        // If AirportService constructor *does* need AirportsData, adjust here.
        airportService = new AirportService();

        // AirplaneService now loads its own data internally
        airplaneService = new AirplaneService();

        // AirNetworkService depends on AirportService and RouteData
        airNetworkService = new AirNetworkService(airportService, routeData);

        // PassengerService depends on PassengerData
        passengerService = new PassengerService(passengerData);

        // FlightService depends on FlightData, AirplaneService, AirNetworkService, AirportService
        flightService = new FlightService(flightData, airplaneService, airNetworkService, airportService,passengerService);

        // 3. Load the initial login scene
        FXMLLoader loginFxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/logininterface/login.fxml"));
        Scene loginScene = new Scene(loginFxmlLoader.load());

        stage.setTitle("Airport Operations System - Login");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }

    public static void loadMainApplicationScene(Stage stage) throws IOException {
        FXMLLoader helloFxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/hello-view.fxml"));
        Scene helloScene = new Scene(helloFxmlLoader.load());

        HelloController controller = helloFxmlLoader.getController();
        // Pass ALL initialized services to the HelloController
        controller.setServices(passengerService, flightService, airplaneService, airNetworkService, airportService);

        String css = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/stylesheet.css").toExternalForm();
        helloScene.getStylesheets().add(css);

        String alertCss = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/alert_styles.css").toExternalForm();
        helloScene.getStylesheets().add(alertCss);

        stage.setTitle("Airport Operations System");
        stage.setScene(helloScene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("La aplicación se está cerrando. Guardando datos...");

        // Save data for all services that manage persistence
        if (passengerService != null) {
            passengerService.saveData();
            System.out.println("Datos de pasajeros guardados exitosamente.");
        }
        if (flightService != null) {
            flightService.saveData();
            System.out.println("Datos de vuelos guardados exitosamente.");
        }
        if (airplaneService != null) {
            airplaneService.saveAirplanes(); // Call saveAirplanes() for AirplaneService
            System.out.println("Datos de aviones guardados exitosamente.");
        }
        if (airportService != null) {
            airportService.saveAirports(); // Call saveAirports() for AirportService
            System.out.println("Datos de aeropuertos guardados exitosamente.");
        }
        if (airNetworkService != null) {
            airNetworkService.saveRoutes(); // Call saveRoutes() for AirNetworkService
            System.out.println("Datos de rutas guardados exitosamente.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}