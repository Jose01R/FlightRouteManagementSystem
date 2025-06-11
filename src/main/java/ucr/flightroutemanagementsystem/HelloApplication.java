package ucr.flightroutemanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

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

        String css = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/stylesheet.css").toExternalForm();
        helloScene.getStylesheets().add(css);

        String alertCss = HelloApplication.class.getResource("/ucr/flightroutemanagementsystem/alert_styles.css").toExternalForm();
        helloScene.getStylesheets().add(alertCss);

        stage.setTitle("Airport Operations System");
        stage.setScene(helloScene);
       stage.setResizable(true);

        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}