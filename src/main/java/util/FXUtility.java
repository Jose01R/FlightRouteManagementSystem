package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import ucr.flightroutemanagementsystem.HelloApplication;

import java.io.IOException;
import java.util.Optional;

public class FXUtility {

    public static void loadPage(String className, String page, BorderPane bp) {
        try {
            Class cl = Class.forName(className);
            FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource(page));
            cl.getResource("bp");
            bp.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Alert alert(String title, String header){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();

        return alert;
    }

    public static Alert alertInfo(String title, String header){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //no estoy seguro si es necesario
        alert.getDialogPane().setMinWidth(400); //no estoy seguro si es necesario
        alert.setHeaderText(header);
        alert.showAndWait();
        //alert.setContentText(header);
        return alert;
    }


    public static TextInputDialog dialog(String title, String header){
        TextInputDialog dialog = new TextInputDialog(title);
        dialog.setHeaderText(header);
        return dialog;
    }

    public static String alertYesNo(String title, String headerText, String contextText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getDialogPane().getButtonTypes().clear(); //quita los botones defaults
        alert.getDialogPane().getButtonTypes().add(buttonTypeYes);
        alert.getDialogPane().getButtonTypes().add(buttonTypeNo);
        //dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        DialogPane dialogPane = alert.getDialogPane();
        String css = HelloApplication.class.getResource("dialog.css").toExternalForm();
        dialogPane.getStylesheets().add(css);
        Optional<ButtonType> result = alert.showAndWait();
        //if((result.isPresent())&&(result.get()== ButtonType.OK)) {
        if((result.isPresent())&&(result.get()== buttonTypeYes))
            return "YES";
        else return "NO";
    }
}
