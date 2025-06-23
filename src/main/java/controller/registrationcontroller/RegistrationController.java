package controller.registrationcontroller;

// Removed direct data imports as services will manage them
// import data.PassengerData;
// import data.UserData;

import data.UserData;
import domain.btree.TreeException;
import domain.common.Passenger;
import domain.common.User;
import domain.common.UserRole;
import domain.service.PassengerService;
import domain.service.LogInService; // NEW: Import UserService
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

    // These will now be injected instances
    private UserData userService; // NEW: Holds the shared UserService instance
    private PassengerService passengerService; // Holds the shared PassengerService instance

    // The constructor no longer initializes services.
    // Services will be set via the setServices method.
    public RegistrationController() {
        // No longer initializing services directly here.
        // This constructor might be empty or used for other simple setup.
    }

    /**
     * Sets the shared service instances for this controller.
     * This method must be called by the FXMLLoader after loading the FXML.
     * @param userService The application's main UserService instance.
     * @param passengerService The application's main PassengerService instance.
     */
    public void setServices(UserData userService, PassengerService passengerService) {
        this.userService = userService;
        this.passengerService = passengerService;
        // You can add validation here if needed, e.g., Objects.requireNonNull(userService);
    }

    @FXML
    private void registerOnAction(ActionEvent event) {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String idCedula = idCedulaTextField.getText();
        String nationality = nationalityTextField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // **IMPORTANT: Ensure services are set before proceeding**
        if (userService == null || passengerService == null) {
            FXUtility.alert("Error de Inicialización", "Los servicios de usuario y pasajero no se han cargado correctamente. Contacte al administrador.");
            System.err.println("CRITICAL: UserService or PassengerService is null in RegistrationController.setServices was not called correctly.");
            return;
        }

        // validacion campos vacios
        if (name.isEmpty() || email.isEmpty() || idCedula.isEmpty() || nationality.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            FXUtility.alert("Error de Registro", "Por favor, complete todos los campos, incluyendo el ID/Cédula.");
            return;
        }

        // validacion si contraseñas coinciden
        if (!password.equals(confirmPassword)) {
            FXUtility.alert("Error de Contraseña", "Las contraseñas no coinciden.");
            return;
        }

        // validamos formato del email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            FXUtility.alert("Error de Email", "Por favor, ingrese un formato de correo electrónico válido.");
            return;
        }

        // verificamos si ya el email existe usando el servicio de usuario
        if (userService.getUserByEmail(email) != null) { // Uses injected userService
            FXUtility.alert("Error de Registro", "Ya existe un usuario con este correo electrónico.");
            return;
        }

        int cedulaPassenger;
        try {
            cedulaPassenger = Integer.parseInt(idCedula);
        } catch (NumberFormatException e) {
            FXUtility.alert("Error de ID", "El ID/Cédula debe ser un número válido (solo números).");
            return;
        }

        // validamos si ya existe pasajero con ese ID de cédula usando el servicio de pasajero
        if (passengerService.findPassengerById(cedulaPassenger) != null) { // Uses injected passengerService
            FXUtility.alert("Error de Registro", "Ya existe un pasajero registrado con este ID/Cédula.");
            return;
        }

        try {
            // obtenemos id unico para el User usando el servicio de usuario
            int newUserId = userService.getNextAvailableId(); // Uses injected userService

            // Crear y registrar user
            User newUser = new User(newUserId, name, password, email, UserRole.USER);

            if (!userService.registerUser(newUser)) { // Uses injected userService
                FXUtility.alert("Error de Registro de Usuario", "No se pudo registrar el usuario. Intente de nuevo.");
                return;
            }

            // Crear y registrar passenger
            Passenger newPassenger = new Passenger(cedulaPassenger, name, nationality);

            if (passengerService.registerPassenger(newPassenger)) { // Uses injected passengerService
                FXUtility.alertInfo("Registro Exitoso", "¡Bienvenido/a, " + name + "! Tu cuenta y perfil de pasajero han sido creados.");
                backToLoginOnAction(event); // Navigate back to login
            } else {
                FXUtility.alert("Error de Registro de Pasajero", "No se pudo crear el perfil de pasajero. Se cancelará el registro de usuario.");
                userService.deleteUser(newUserId); // Rollback user via userService
            }
        } catch (TreeException e) {
            FXUtility.alert("Error en el Servicio de Pasajeros", "Ocurrió un error al registrar el pasajero: " + e.getMessage());
            e.printStackTrace();

            try {
                // Rollback user registration if passenger registration failed
                // Attempt to delete the user whose ID was just created.
                // Assuming getNextAvailableId() gives the *next* available,
                // so the one just used was (current next - 1).
                int userIdToRollback = userService.getNextAvailableId() - 1;
                // If userService.deleteUser() throws an exception, it will be caught by the outer catch.
                userService.deleteUser(userIdToRollback);
                System.err.println("Rolled back user with ID: " + userIdToRollback);
            } catch (Exception ex) {
                System.err.println("CRITICAL: Failed to rollback user after passenger registration error: " + ex.getMessage());
            }
        } catch (Exception e) { // General catch for any other unexpected errors
            e.printStackTrace();
            FXUtility.alert("Error del Sistema", "Ocurrió un error inesperado durante el registro: " + e.getMessage());
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
            FXUtility.alert("Error de Navegación", "No se pudo cargar la pantalla de inicio de sesión: " + e.getMessage());
            System.err.println("IOException durante la carga de la escena de inicio de sesión: " + e.getMessage());
        }
    }
}