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
            util.FXUtility.alert("Error de Inicio de Sesión", "Por favor, ingresa tanto el correo electrónico como la contraseña.");
            return;
        }

        //validamos formato del email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            FXUtility.alert("Error de Email", "Por favor, ingrese un formato de correo electrónico válido.");
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
                            util.FXUtility.alertInfo("Inicio de Sesión Exitoso", "¡Bienvenido/a, " + authenticatedUser.getName());

                            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            try {
                                HelloApplication.loadMainApplicationSceneAdmin(stage, authenticatedUser.getName(), String.valueOf(authenticatedUser.getRole()));
                            } catch (IOException e) {
                                util.FXUtility.alert("Error de Navegación", "No se pudo cargar la siguiente pantalla: " + e.getMessage());
                                System.err.println("IOException durante la carga de la escena: " + e.getMessage());
                            }

                        } else {
                            util.FXUtility.alert("Inicio de Sesión Fallido", "Correo electrónico o contraseña inválidos.");
                        }

                        break;

                    case USER:
                        if (authenticatedUser != null) {
                            util.FXUtility.alertInfo("Inicio de Sesión Exitoso", "¡Bienvenido/a, " + authenticatedUser.getName());

                            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            try {
                                HelloApplication.loadMainApplicationSceneUser(stage, authenticatedUser.getName(), String.valueOf(authenticatedUser.getRole()));
                            } catch (IOException e) {
                                util.FXUtility.alert("Error de Navegación", "No se pudo cargar la siguiente pantalla: " + e.getMessage());
                                System.err.println("IOException durante la carga de la escena: " + e.getMessage());
                            }

                        } else {
                            util.FXUtility.alert("Inicio de Sesión Fallido", "Correo electrónico o contraseña inválidos.");
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
                util.FXUtility.alert("Inicio de Sesión Fallido", "Correo electrónico o contraseña inválidos.");
            }
        } catch (ListException e) {
            util.FXUtility.alert("Error del Sistema", "Ocurrió un error interno del sistema durante el inicio de sesión: " + e.getMessage());
            System.err.println("ListException durante el inicio de sesión: " + e.getMessage());


        } 
    }

    @FXML
    private void forgotPasswordOnAction(ActionEvent event) {
        //Solicitamos email del usuario
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recuperar Contraseña");
        dialog.setHeaderText("¿Olvidaste tu contraseña?");
        dialog.setContentText("Por favor, introduce tu correo electrónico:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().isEmpty()) {
            String email = result.get();
            //Buscamos por email al user
            User userToReset = userData.getUserByEmail(email);

            if (userToReset != null) {
                //Pedimos nueva contraseña directamente
                TextInputDialog newPassDialog = new TextInputDialog();
                newPassDialog.setTitle("Restablecer Contraseña");
                newPassDialog.setHeaderText("Usuario encontrado: " + userToReset.getName());
                newPassDialog.setContentText("Introduce tu nueva contraseña:");

                Optional<String> newPassResult = newPassDialog.showAndWait();

                if (newPassResult.isPresent() && !newPassResult.get().isEmpty()) {
                    String newPassword = newPassResult.get();

                    //Hashear nueva contraseña y actualizar usuario
                    String hashedNewPassword = userToReset.hashPassword(newPassword);
                    userToReset.setHashedPassword(hashedNewPassword); //Actualizamos contraseña

                    //Actualizamos el user en el archivo
                    if (userData.updateUser(userToReset)) {
                        util.FXUtility.alertInfo("Éxito", "Tu contraseña ha sido restablecida exitosamente. Ahora puedes iniciar sesión con tu nueva contraseña.");
                        this.logInService = new LogInService(userData); //Recargamos la lsta circular con datos actualizados
                    } else {
                        util.FXUtility.alert("Error", "No se pudo actualizar la contraseña. Intenta de nuevo.");
                    }
                } else {
                    util.FXUtility.alertWarning("Cancelado", "Restablecimiento de contraseña cancelado.");
                }

            } else {
                util.FXUtility.alert("Error", "No se encontró ningún usuario con el correo electrónico '" + email + "'.");
            }
        } else {
            util.FXUtility.alertWarning("Cancelado", "Operación de recuperación de contraseña cancelada.");
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
            stage.setTitle("Registrar nuevo usuario");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            FXUtility.alert("Error de Navegación", "No se pudo cargar la pantalla de registro: " + e.getMessage());
            System.err.println("IOException durante la carga de la escena de registro: " + e.getMessage());
        }
    }


}