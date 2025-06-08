package domain.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    private int id;
    private String name;
    private String hashedPassword;
    private String email;
    private UserRole role;

    public User() {}

    public User(int id, String name, String password, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.hashedPassword = hashPassword(password); //Hashea la contraseña de entrada
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(UserRole role) { // El setter acepta el Enum
        this.role = role;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", role=" + role + "]";
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash); // Codificar a Base64 para almacenar como String
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al hashear la contraseña (algoritmo no encontrado): " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si la contraseña en sin hasheo proporcionada coincide
     * con contraseña hasheada guardada
     * Se pasa la password en sin hasheo para verificar.
     */
    public boolean checkPassword(String password) {
        //HashedPassword no debe ser null antes de comparar
        if (this.hashedPassword == null || password == null) {
            return false; //false passwords no coinciden
        }
        return this.hashedPassword.equals(hashPassword(password)); //true si passwords coinciden
    }



}
