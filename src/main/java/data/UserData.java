package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.common.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class UserData {
    //Nombre del archivo
    private static final String USERS_FILE = "users.json";

    private static final String DATA_DIRECTORY = "JSON_FILES_DATA"; // Nombre directorio

    //Almacenar los usuarios asociados, usando el ID como key
    private Map<Integer, User> users;
    //Para la serialización y deserialización de JSON
    private ObjectMapper objectMapper;

    /**
     * Constructor de UserData
     * Intenta cargar los usuarios existentes
     * desde el archivo JSON al iniciar
     */
    public UserData() throws IOException {
        this.users = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        //Para que el JSON sea legible
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);// le dice a Jackson que formatee la salida JSON con una indentación legible
        this.objectMapper.findAndRegisterModules();//para manejar tiposde datos time

        //Carga usuarios al inicializar la clase
        loadUsersFromFile();
    }

    /**
     * Carga los usuarios desde el archivo
     * Si el archivo no existe o está vacío
     * el map de usuarios se queda vacío
     */
    private void loadUsersFromFile() throws IOException {
        File file = util.Utility.getFilePath(DATA_DIRECTORY, USERS_FILE).toFile();
        // Verifica si el archivo existe y no está vacío antes de intentar leerlo
        if (file.exists() && file.length() > 0) {
            try {
                // Jackson necesita un TypeReference para deserializar Maps correctamente
                this.users = objectMapper.readValue(file, objectMapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, User.class));
                System.out.println("Usuarios cargados exitosamente desde " + USERS_FILE);
            } catch (IOException e) {
                System.err.println("Error al cargar usuarios desde " + USERS_FILE + ": " + e.getMessage());
            }
        } else {
            System.out.println("Archivo " + USERS_FILE + " no encontrado o vacío. Se iniciará con una lista de usuarios nueva.");
        }
    }

    /**
     * Guarda el estado actual
     * en el archivo JSON
     */
    public void saveUsersToFile() {
        try {
            objectMapper.writeValue(util.Utility.getFilePath(DATA_DIRECTORY, USERS_FILE).toFile(), users);
            System.out.println("Usuarios guardados exitosamente en " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios en " + USERS_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Registra un nuevo usuario en el sistema
     * User a registrar
     * True si el usuario fue registrado - False si ya existía
     */
    public boolean registerUser(User user) {
        //VALDACION POR ID SI YA EXISTE
        if (users.containsKey(user.getId())) {
            System.out.println("Error: Ya existe un usuario con el ID " + user.getId());
            return false;
        }
        // VALIDA SI HAY UN EMAIL EXISTENTE PARA EVITAR DUPLCADOS
        boolean emailExists = users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            System.out.println("Error: Ya existe un usuario con el email " + user.getEmail());
            return false;
        }

        users.put(user.getId(), user);
        saveUsersToFile(); //Guardamos los cambios
        return true;
    }

    /**
     * Actualiza datos de un usuario
     * Para actualizar password debe usarse método hashPassword de User
     * al crear el nuevo objeto User
     *True si fue actualizado
     *False si el usuario no existe.
     */
    public boolean updateUser(User updatedUser) {
        // Verifica si el usuario existe antes de intentar actualizarlo
        if (!users.containsKey(updatedUser.getId())) {
            System.out.println("Error al actualizar: No se encontró un usuario con el ID " + updatedUser.getId());
            return false;
        }

        //Validar el email actualizado no duplique otro
        // email (excepto el propio)
        boolean emailConflict = users.values().stream()
                .filter(u -> u.getId() != updatedUser.getId()) // Excluye al propio usuario que se está actualizando
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(updatedUser.getEmail()));

        if (emailConflict) {
            System.out.println("Error al actualizar: El email '" + updatedUser.getEmail() + "' ya está en uso por otro usuario.");
            return false;
        }

        //Si el usuario existe y no hay problema
        // con email se sobrescribe
        users.put(updatedUser.getId(), updatedUser);
        saveUsersToFile(); //Guardamos cambios
        System.out.println("Usuario con ID " + updatedUser.getId() + " actualizado exitosamente.");
        return true;
    }


    /**
     * Elimina un usuario por ID
     * True usuario fue eliminado
     * False usuario no existe
     */
    public boolean deleteUser(int userId) {
        //Validacion si usuario existe antes de intentar eliminarlo
        if (!users.containsKey(userId)) {
            System.out.println("Error al eliminar: No se encontró un usuario con el ID " + userId);
            return false;
        }

        users.remove(userId); //Elimina el usuario
        saveUsersToFile();    //Guardamos cambios
        System.out.println("Usuario con ID " + userId + " eliminado exitosamente.");
        return true;
    }

    /**
     * Busca un usuario por su email
     * email del usuario a buscar
     * Null si no existe
     */
    public User getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    public User getUserById(int id) {
        return users.get(id); //ACCESO POR LA KEY
    }

    /**
     * Obtiene el map completo de usuarios
     *Todos los usuarios registrados
     */
    public Map<Integer, User> getAllUsers() {
        return new HashMap<>(users); //Devuelve una copia para evitar modificaciones externas directas
    }

    /**
     * Genera el siguiente ID único disponible para un nuevo usuario
     * Encuentra el ID máximo en uso y lo incrementa en uno
     * Si no existen usuarios, empieza desde 1
     */
    public int getNextAvailableId() {
        if (users.isEmpty()) {
            return 1; // empeiza en 1 si no existe ninguno
        } else {
            OptionalInt maxId = users.keySet().stream()
                    .mapToInt(Integer::intValue) // Convertimos a intStream
                    .max(); //encontramos el max id

            //incrementamos en 1 si existen users
            return maxId.orElse(0) + 1;
        }
    }
}
