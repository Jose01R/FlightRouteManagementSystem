package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ucr.flightroutemanagementsystem.HelloApplication;

import java.io.IOException;

public class HelloController {

    @FXML
    private BorderPane bp;
    @FXML
    private Text txtMessage;
    @FXML
    private AnchorPane ap;

    private void loadPage(String page) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(page));
        try {
            this.bp.setCenter(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void Exit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void Home(ActionEvent event) {
        this.txtMessage.setText("Airport Operations System");
        this.bp.setCenter(ap);
    }


    @FXML
    public void simulaciónVuelosOcupaciónOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }

    @FXML
    public void pasajerosOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }

    @FXML
    public void aeropuertosOnAction(ActionEvent actionEvent) {
        loadPage("airports.fxml");
    }


    @FXML
    public void reportesOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }

    @FXML
    public void vuelosOnAction(ActionEvent actionEvent) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/flightinterface/flight.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 1410, 900); // Tamaño deseado
                Stage stage = new Stage();
                stage.setTitle("Gestión de Vuelos");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @FXML
    public void rutasEntreAeropuertosOnAction(ActionEvent actionEvent) {
        loadPage("fxml.");
    }
}