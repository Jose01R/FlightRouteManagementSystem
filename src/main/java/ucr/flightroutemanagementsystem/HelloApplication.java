package ucr.flightroutemanagementsystem;

import controller.HelloController;
import controller.flightcontroller.FlightController;
import domain.service.FlightService;
import domain.service.PassengerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import data.PassengerData;
import data.FlightData;

public class HelloApplication extends Application {


    private static PassengerData passengerData;
    private static FlightData flightData;

    private static PassengerService passengerService;
    private static FlightService flightService;



    @Override
    public void start(Stage stage) throws IOException {

        passengerData = new PassengerData();
        flightData = new FlightData();

         passengerService = new PassengerService(passengerData);
         flightService = new FlightService(flightData);



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
        controller.setServices(passengerService, flightService);

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
        if (passengerService != null) { // Usa los servicios estáticos que tienen los datos
            passengerService.saveData();
            System.out.println("Datos de pasajeros guardados exitosamente.");
        }
        if (flightService != null) { // Usa los servicios estáticos que tienen los datos
            flightService.saveData();
            System.out.println("Datos de vuelos guardados exitosamente.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}