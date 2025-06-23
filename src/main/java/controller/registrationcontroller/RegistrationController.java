package controller.registrationcontroller;

import data.UserData;
import domain.btree.TreeException;
import domain.common.Passenger;
import domain.common.User;
import domain.common.UserRole;
import domain.service.PassengerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.FXUtility;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField idCedulaTextField;
    @FXML
    private TextField nationalityTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    // Instancias de servicios que serán inyectadas
    private UserData userService;
    private PassengerService passengerService;

    // El constructor ya no inicializa servicios.
    public RegistrationController() {
        // Constructor vacío o para configuración simple.
    }

    /**
     * Establece las instancias compartidas de los servicios para este controlador.
     * Este método debe ser llamado por el FXMLLoader después de cargar el FXML.
     * @param userService La instancia principal del servicio de usuarios de la aplicación.
     * @param passengerService La instancia principal del servicio de pasajeros de la aplicación.
     */
    public void setServices(UserData userService, PassengerService passengerService) {
        this.userService = userService;
        this.passengerService = passengerService;
    }

    @FXML
    private void registerOnAction(ActionEvent event) {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String idCedula = idCedulaTextField.getText();
        String nationality = nationalityTextField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validar que los servicios estén inicializados antes de proceder.
        if (userService == null || passengerService == null) {
            FXUtility.alert("Initialization Error", "User and passenger services were not loaded correctly. Please contact the administrator.").showAndWait();
            System.err.println("CRITICAL: UserService or PassengerService is null in RegistrationController.setServices was not called correctly.");
            return;
        }

        // Validación de campos vacíos
        if (name.isEmpty() || email.isEmpty() || idCedula.isEmpty() || nationality.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            FXUtility.alert("Registration Error", "Please complete all fields, including ID/Cedula.").showAndWait();
            return;
        }

        // Validación de coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            FXUtility.alert("Password Error", "Passwords do not match.").showAndWait();
            return;
        }

        // Validación de formato de email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            FXUtility.alert("Email Error", "Please enter a valid email format.").showAndWait();
            return;
        }

        // Verificar si el email ya existe usando el servicio de usuario
        if (userService.getUserByEmail(email) != null) {
            FXUtility.alert("Registration Error", "A user with this email already exists.").showAndWait();
            return;
        }

        int cedulaPassenger;
        try {
            cedulaPassenger = Integer.parseInt(idCedula);
        } catch (NumberFormatException e) {
            FXUtility.alert("ID Error", "The ID/Cedula must be a valid number (numbers only).").showAndWait();
            return;
        }

        // Validar si ya existe un pasajero con ese ID de cédula usando el servicio de pasajero
        if (passengerService.findPassengerById(cedulaPassenger) != null) {
            FXUtility.alert("Registration Error", "A passenger with this ID/Cedula is already registered.").showAndWait();
            return;
        }

        try {
            // Obtener un ID único para el nuevo usuario usando el servicio de usuario
            int newUserId = userService.getNextAvailableId();

            // Crear y registrar el nuevo usuario
            User newUser = new User(newUserId, name, password, email, UserRole.USER);

            if (!userService.registerUser(newUser)) {
                FXUtility.alert("User Registration Error", "Could not register the user. Please try again.").showAndWait();
                return;
            }

            // Crear y registrar el perfil del pasajero
            Passenger newPassenger = new Passenger(cedulaPassenger, name, nationality);

            if (passengerService.registerPassenger(newPassenger)) {
                FXUtility.alertInfo("Registration Successful", "Welcome, " + name + "! Your account and passenger profile have been created.").showAndWait();
                backToLoginOnAction(event); // Navegar de vuelta a la pantalla de inicio de sesión
            } else {
                // Si falla el registro del pasajero, intentar revertir el registro del usuario
                FXUtility.alert("Passenger Registration Error", "Could not create the passenger profile. User registration will be canceled.").showAndWait();
                userService.deleteUser(newUserId);
            }
        } catch (TreeException e) {
            FXUtility.alert("Passenger Service Error", "An error occurred while registering the passenger: " + e.getMessage()).showAndWait();
            e.printStackTrace();

            try {
                // Revertir el registro del usuario si el registro del pasajero falló
                // Se asume que getNextAvailableId() devuelve el *siguiente* ID disponible,
                // por lo que el ID usado fue (siguiente disponible - 1).
                int userIdToRollback = userService.getNextAvailableId() - 1;
                userService.deleteUser(userIdToRollback); // Si esto lanza una excepción, será capturada por el catch externo.
                System.err.println("Rolled back user with ID: " + userIdToRollback);
            } catch (Exception ex) {
                System.err.println("CRITICAL: Failed to rollback user after passenger registration error: " + ex.getMessage());
            }
        } catch (Exception e) { // Captura general para otros errores inesperados
            e.printStackTrace();
            FXUtility.alert("System Error", "An unexpected error occurred during registration: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void backToLoginOnAction(ActionEvent event) {
        try {
            String fxmlPath = "/ucr/flightroutemanagementsystem/logininterface/login.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Log In");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            FXUtility.alert("Navigation Error", "Could not load the login screen: " + e.getMessage()).showAndWait();
            System.err.println("IOException during login scene loading: " + e.getMessage());
        }
    }
}