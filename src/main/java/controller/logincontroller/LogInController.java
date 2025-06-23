package controller.logincontroller;

import controller.registrationcontroller.RegistrationController;
import data.UserData;
import domain.common.User;
import domain.linkedlist.ListException;
import domain.service.LogInService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import ucr.flightroutemanagementsystem.HelloApplication;
import util.FXUtility;

import java.io.IOException;
import java.util.Optional; //Para manejar el resultado de TextInputDialog

public class LogInController {

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private LogInService logInService;
    private UserData userData;

    public LogInController() {
        try {
            this.userData = new UserData(); //Inicializar UserData
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.logInService = new LogInService(userData); //Pasar UserData al LogInService
    }

    @FXML
    private void logInOnAction(ActionEvent event) {
        String email = emailTextField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            util.FXUtility.alert("Login Error", "Please enter both email and password.").showAndWait(); 
            return;
        }

        //validamos formato del email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            FXUtility.alert("Email Error", "Please enter a valid email format.").showAndWait(); 
            return;
        }

        try {
            User authenticatedUser = logInService.login(email, password);

            if (authenticatedUser != null) {
                //util.FXUtility.alertInfo("Inicio de Sesión Exitoso", "¡Bienvenido/a, " + authenticatedUser.getName());

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//                HelloApplication.loadMainApplicationScene(stage);
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/flightroutemanagementsystem/admin-hello-view.fxml"));

                //Se carga la vista dependiendo del rol
                String fxmlPath = "";
                switch (authenticatedUser.getRole()) {
                    case ADMINISTRATOR:
                        if (authenticatedUser != null) {
                            util.FXUtility.alertInfo("Login Successful", "Welcome, " + authenticatedUser.getName() + "!").showAndWait(); 

                            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            try {
                                HelloApplication.loadMainApplicationSceneAdmin(stage, authenticatedUser.getName(), String.valueOf(authenticatedUser.getRole()));
                            } catch (IOException e) {
                                util.FXUtility.alert("Navigation Error", "Could not load the next screen: " + e.getMessage()).showAndWait(); 
                                System.err.println("IOException during scene loading: " + e.getMessage());
                            }

                        } else {
                            util.FXUtility.alert("Login Failed", "Invalid email or password.").showAndWait(); 
                        }

                        break;

                    case USER:
                        if (authenticatedUser != null) {
                            util.FXUtility.alertInfo("Login Successful", "Welcome, " + authenticatedUser.getName() + "!").showAndWait(); 

                            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            try {
                                HelloApplication.loadMainApplicationSceneUser(stage, authenticatedUser.getName(), String.valueOf(authenticatedUser.getRole()));
                            } catch (IOException e) {
                                util.FXUtility.alert("Navigation Error", "Could not load the next screen: " + e.getMessage()).showAndWait(); 
                                System.err.println("IOException during scene loading: " + e.getMessage());
                            }

                        } else {
                            util.FXUtility.alert("Login Failed", "Invalid email or password.").showAndWait(); 
                        }

                        break;

                    default:
                        fxmlPath = "/view/GeneralDashboard.fxml";
                        break;
                }

                // OPCION 2
                //Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                //Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));  //ACA SE USA EL FXML PATH
                //Scene scene = new Scene(root);
                // stage.setScene(scene);
                //stage.show();

            } else {
                util.FXUtility.alert("Login Failed", "Invalid email or password.").showAndWait(); 
            }
        } catch (ListException e) {
            util.FXUtility.alert("System Error", "An internal system error occurred during login: " + e.getMessage()).showAndWait(); 
            System.err.println("ListException during login: " + e.getMessage());


        }
    }

    @FXML
    private void forgotPasswordOnAction(ActionEvent event) {
        //Solicitamos email del usuario
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recover Password"); 
        dialog.setHeaderText("Forgot your password?"); 
        dialog.setContentText("Please enter your email address:"); 

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().isEmpty()) {
            String email = result.get();
            //Buscamos por email al user
            User userToReset = userData.getUserByEmail(email);

            if (userToReset != null) {
                //Pedimos nueva contraseña directamente
                TextInputDialog newPassDialog = new TextInputDialog();
                newPassDialog.setTitle("Reset Password"); 
                newPassDialog.setHeaderText("User found: " + userToReset.getName()); 
                newPassDialog.setContentText("Enter your new password:"); 

                Optional<String> newPassResult = newPassDialog.showAndWait();

                if (newPassResult.isPresent() && !newPassResult.get().isEmpty()) {
                    String newPassword = newPassResult.get();

                    //Hashear nueva contraseña y actualizar usuario
                    String hashedNewPassword = userToReset.hashPassword(newPassword);
                    userToReset.setHashedPassword(hashedNewPassword); //Actualizamos contraseña

                    //Actualizamos el user en el archivo
                    if (userData.updateUser(userToReset)) {
                        util.FXUtility.alertInfo("Success", "Your password has been successfully reset. You can now log in with your new password.").showAndWait(); 
                        this.logInService = new LogInService(userData); //Recargamos la lsta circular con datos actualizados
                    } else {
                        util.FXUtility.alert("Error", "Could not update password. Please try again.").showAndWait(); 
                    }
                } else {
                    util.FXUtility.alertWarning("Canceled", "Password reset canceled.").showAndWait(); 
                }

            } else {
                util.FXUtility.alert("Error", "No user found with the email address '" + email + "'.").showAndWait(); 
            }
        } else {
            util.FXUtility.alertWarning("Canceled", "Password recovery operation canceled.").showAndWait(); 
        }
    }

    @FXML
    private void registerOnAction(ActionEvent event) {
        try {
            String fxmlPath = "/ucr/flightroutemanagementsystem/register.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load(); // Load the FXML

            //Obtener la instancia del controlador *después* de cargar el FXML
            RegistrationController registrationController = loader.getController();

            //Pasar las instancias de service a RegistrationController para acceder desde aca
            //Accedemos a los campos estáticos de HelloApplication
            if (registrationController != null) {
                registrationController.setServices(HelloApplication.getUserData(), HelloApplication.getPassengerService());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register New User"); 
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            FXUtility.alert("Navigation Error", "Could not load the registration screen: " + e.getMessage()).showAndWait(); 
            System.err.println("IOException during registration scene loading: " + e.getMessage());
        }
    }
}