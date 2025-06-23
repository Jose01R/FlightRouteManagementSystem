package ucr.flightroutemanagementsystem;

import controller.login.AdminHelloController;
// import controller.flightcontroller.FlightController; // Not directly used here, can remove if not needed elsewhere

import controller.login.UserHelloController;
import data.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// Import all Data classes
import domain.service.AirportService; // Changed from AirportData to AirportsData for consistency
import domain.service.AirNetworkService;

// Import all Service classes
import domain.service.PassengerService;
import domain.service.FlightService;
import domain.service.AirplaneService;

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
    private static UserData userData;

    public static PassengerService getPassengerService() {
        return passengerService;
    }

    public static UserData getUserData() {
        return userData;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Initialize all Data layer instances
        passengerData = new PassengerData();
        flightData = new FlightData();
        airplaneData = new AirplaneData();
        airportsData = new AirportsData();
        routeData = new RouteData();
        userData = new UserData();

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

    public static void loadMainApplicationSceneAdmin(Stage stage, String name, String rol) throws IOException {
        FXMLLoader helloFxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/loginuser/admin-hello-view.fxml"));
        Scene helloScene = new Scene(helloFxmlLoader.load());

        AdminHelloController controller = helloFxmlLoader.getController();
        // Pass ALL initialized services to the HelloController
        controller.setServices(passengerService, flightService, airplaneService, airNetworkService, airportService);
        controller.recibirDatos(name, rol);
        String css = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/stylesheet.css").toExternalForm();
        helloScene.getStylesheets().add(css);

        String alertCss = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/alert_styles.css").toExternalForm();
        helloScene.getStylesheets().add(alertCss);

        stage.setTitle("Airport Operations System");
        stage.setScene(helloScene);
        stage.setResizable(false);
        stage.show();
    }

    public static void loadMainApplicationSceneUser(Stage stage, String name, String rol) throws IOException {
        FXMLLoader helloFxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/loginuser/user-hello-view.fxml"));
        Scene helloScene = new Scene(helloFxmlLoader.load());

        UserHelloController controller = helloFxmlLoader.getController();
        // Pass ALL initialized services to the HelloController
        controller.setServices(passengerService, flightService, airplaneService, airNetworkService, airportService);
        controller.recibirDatos(name, rol);
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
        if (userData != null) {
            userData.saveUsersToFile();
            System.out.println("Datos de usuarios guardados exitosamente.");
        }

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