package domain.service;

import data.UserData;
import domain.common.User;
import domain.common.UserRole;
import domain.linkedlist.ListException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LogInServiceTest {

    @Test
     void test() {
        try {
            //Inicialización de UserData y LogInService
            UserData userData = null;

            userData = new UserData();


            //Limpiar archivo users
            System.out.println("\n--- Limpiando usuarios existentes ---");
            userData.getAllUsers().clear(); //Limpia el mapa en memoria
            userData.saveUsersToFile();     //Guarda un mapa vacío en el archivo
            System.out.println("users.json ha sido limpiado");

            //Reinicializar LogInService para que su CLL se cargue vacía inicialmente
            LogInService loginService = new LogInService(userData);


            // --- Registrar usuarios ---
            System.out.println("\n--- Registrando usuarios iniciales ---");

            User adminUser = new User(1, "Administrador Supremo", "admin123", "admin@sys.com", UserRole.ADMINISTRATOR);
            User regularUser = new User(2, "Usuario Normal", "user123", "user@sys.com", UserRole.USER);
            User guestUser = new User(3, "Invitado", "guestpass", "guest@sys.com", UserRole.USER);

            boolean registeredAdmin = userData.registerUser(adminUser);
            System.out.println("Registro de Admin: " + (registeredAdmin ? "Exitoso" : "Fallido"));

            boolean registeredRegular = userData.registerUser(regularUser);
            System.out.println("Registro de Usuario Normal: " + (registeredRegular ? "Exitoso" : "Fallido"));

            boolean registeredGuest = userData.registerUser(guestUser);
            System.out.println("Registro de Invitado: " + (registeredGuest ? "Exitoso" : "Fallido"));

            System.out.println("\n--- Usuarios registrados y guardados en users.json ---");
            System.out.println("Contenido actual:");
            userData.getAllUsers().values().forEach(System.out::println);


            // --- Actualizar LogInService con los usuarios registrados ---

            loginService = new LogInService(userData);
            System.out.println("\n--- LogInService re-inicializado con los usuarios registrados ---");


            /**
             * PRUEBAS DE REGISTRO DE USUARIO (usando UserData)
             */

            //Intentar registrar un usuario duplicado por ID
            System.out.println("\n--- Intentando registrar un usuario con ID duplicado (ID 1) ---");
            User duplicateIdUser = new User(1, "Duplicado ID", "pass123", "duplicateid@sys.com", UserRole.USER);
            boolean registeredDuplicateId = userData.registerUser(duplicateIdUser);
            System.out.println("Registro con ID duplicado (ID 1): " + (registeredDuplicateId ? "Exitoso (ERROR)" : "Fallido (CORRECTO)"));

            //Intentar registrar un usuario duplicado por email
            System.out.println("\n--- Intentando registrar un usuario con email duplicado (admin@sys.com) ---");
            User duplicateEmailUser = new User(4, "Duplicado Email", "pass456", "admin@sys.com", UserRole.USER);
            boolean registeredDuplicateEmail = userData.registerUser(duplicateEmailUser);
            System.out.println("Registro con email duplicado (admin@sys.com): " + (registeredDuplicateEmail ? "Exitoso (ERROR)" : "Fallido (CORRECTO)"));

            /**
             * PRUEBAS DE LOGIN
             */

            System.out.println("\n\n--- INICIANDO PRUEBAS DE LOGIN ---");
            System.out.println("------------------------------------");

            // Test 1: Login exitoso con administrador
            System.out.println("\n--- Prueba 1: Login de 'admin@sys.com' con contraseña CORRECTA ---");
            User loggedInAdmin = null;
            try {
                loggedInAdmin = loginService.login("admin@sys.com", "admin123");
                if (loggedInAdmin != null) {
                    System.out.println("Resultado: Login exitoso. Usuario: " + loggedInAdmin.getName() + ", Rol: " + loggedInAdmin.getRole());
                } else {
                    System.out.println("Resultado: Login fallido (INESPERADO).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login de admin: " + e.getMessage());
            }


            // Test 2: Login exitoso con user
            System.out.println("\n--- Prueba 2: Login de 'user@sys.com' con contraseña CORRECTA ---");
            User loggedInRegular = null;
            try {
                loggedInRegular = loginService.login("user@sys.com", "user123");
                if (loggedInRegular != null) {
                    System.out.println("Resultado: Login exitoso. Usuario: " + loggedInRegular.getName() + ", Rol: " + loggedInRegular.getRole());
                } else {
                    System.out.println("Resultado: Login fallido (INESPERADO).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login de usuario normal: " + e.getMessage());
            }


            // Test 3: Contraseña incorrecta
            System.out.println("\n--- Prueba 3: Login de 'guest@sys.com' con contraseña INCORRECTA ---");
            User failedLoginWrongPass = null;
            try {
                failedLoginWrongPass = loginService.login("guest@sys.com", "wrongpassword");
                if (failedLoginWrongPass == null) {
                    System.out.println("Resultado: Login fallido  (contraseña incorrecta).");
                } else {
                    System.out.println("Resultado: Login exitoso (ERROR - contraseña incorrecta aceptada).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login con contraseña incorrecta: " + e.getMessage());
            }


            // Test 4: Email no registrado
            System.out.println("\n--- Prueba 4: Login con email NO REGISTRADO ('nonexistent@sys.com') ---");
            User failedLoginNonExistent = null;
            try {
                failedLoginNonExistent = loginService.login("nonexistent@sys.com", "anypass");
                if (failedLoginNonExistent == null) {
                    System.out.println("Resultado: Login fallido (usuario no encontrado).");
                } else {
                    System.out.println("Resultado: Login exitoso (ERROR - usuario no existente aceptado).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login de usuario no existente: " + e.getMessage());
            }


            // Test 5: Login con email correcto, mezclando mayúsculas/minúsculas
            System.out.println("\n--- Prueba 5: Login de 'ADMIN@sys.com' (email con mayúsculas) ---");
            User loggedInCaseInsensitive = null;
            try {
                loggedInCaseInsensitive = loginService.login("ADMIN@sys.com", "admin123");
                if (loggedInCaseInsensitive != null) {
                    System.out.println("Resultado: Login exitoso (sensibilidad a mayúsculas/minúsculas manejada). Usuario: " + loggedInCaseInsensitive.getName());
                } else {
                    System.out.println("Resultado: Login fallido (ERROR - email no reconocido por case-insensitivity).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login case-insensitive: " + e.getMessage());
            }


            // Test 6: Login cuando la lista está vacía
            System.out.println("\n--- Prueba 6: Login con la Circular Linked List vacía ---");
            //Forzar la LISTA a estar vacía
            userData.getAllUsers().clear(); //Limpiamos datos
            userData.saveUsersToFile();
            loginService = new LogInService(userData); //Recargamos una nueva lista (vacía)

            User resultEmptyCLL = null;
            try {
                resultEmptyCLL = loginService.login("any@email.com", "anypass");
                if (resultEmptyCLL == null) {
                    System.out.println("Resultado: Login fallido (lista de usuarios vacía)."); //EXPECTED
                } else {
                    System.out.println("Resultado: Login exitoso (ERROR - lista vacía).");
                }
            } catch (ListException e) {
                System.out.println("Resultado: Se capturó ListException como se esperaba: " + e.getMessage());
            }


            // --- PRUEBAS LOGIN POR ID ---
            System.out.println("\n\n--- PRUEBAS DE LOGIN POR ID ---");
            System.out.println("------------------------------------");

            // Test 7: Login por ID exitoso
            //CARGAMOS USERS POR SI SE ELIMINARON EN TEST PASADO
            userData.registerUser(new User(1, "Administrador Supremo", "admin123", "admin@sys.com", UserRole.ADMINISTRATOR));
            userData.registerUser(new User(2, "Usuario Normal", "user123", "user@sys.com", UserRole.USER));
            loginService = new LogInService(userData);

            System.out.println("\n--- Prueba 7: Login por ID 2 con contraseña CORRECTA ---");
            User loggedInById = null;
            try {
                loggedInById = loginService.loginById(2, "user123");
                if (loggedInById != null) {
                    System.out.println("Resultado: Login por ID exitoso. Usuario: " + loggedInById.getName() + ", Rol: " + loggedInById.getRole());
                } else {
                    System.out.println("Resultado: Login por ID fallido (INESPERADO).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login por ID: " + e.getMessage());
            }

            // Test 8: Login por ID con contraseña incorrecta
            System.out.println("\n--- Prueba 8: Login por ID 3 con contraseña INCORRECTA ---");
            User failedLoginByIdWrongPass = null;
            try {
                failedLoginByIdWrongPass = loginService.loginById(3, "wrongpass");
                if (failedLoginByIdWrongPass == null) {
                    System.out.println("Resultado: Login por ID fallido (contraseña incorrecta)."); //EXPECTED
                } else {
                    System.out.println("Resultado: Login por ID exitoso (ERROR - contraseña incorrecta aceptada).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login por ID con contraseña incorrecta: " + e.getMessage());
            }

            // Test 9: Login por ID no existente
            System.out.println("\n--- Prueba 9: Login por ID NO EXISTENTE (99) ---");
            User failedLoginByIdNonExistent = null;
            try {
                failedLoginByIdNonExistent = loginService.loginById(99, "anypass");
                if (failedLoginByIdNonExistent == null) {
                    System.out.println("Resultado: Login por ID fallido (ID no encontrado).");
                } else {
                    System.out.println("Resultado: Login por ID exitoso (ERROR - ID no existente aceptado).");
                }
            } catch (ListException e) {
                System.out.println("ERROR: Se capturó ListException durante el login por ID no existente: " + e.getMessage());
            }

        } catch (IOException e) {
        throw new RuntimeException(e);
        }

    }
}