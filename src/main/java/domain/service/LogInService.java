package domain.service;

import data.UserData;
import domain.common.User;
import domain.linkedlist.CircularLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.Node;

public class LogInService {
    private UserData userData;
    private CircularLinkedList userCLL;

    public LogInService(UserData userData) {
        this.userData = userData;
        this.userCLL = new CircularLinkedList();
        loadUsersIntoCLL(); //Cargamos los users a la lista
    }

    private void loadUsersIntoCLL() {
        //LIMPIAMOS PARA EVITAR DUPLICADOS AL CARGARLOS DE NUEVO
        userCLL.clear();
        //Obtenemos todos los users
        for (User user : userData.getAllUsers().values()) {
            userCLL.add(user); //Agregamos a la Circular Linked List
        }
        System.out.println("Users loaded into Circular Linked List for login operations.");
    }

    public User login(String email, String password) throws ListException {
        //Verificamos si se encontró
        if (userCLL.isEmpty()) {
            System.out.println("Inicio de sesión fallido: No hay usuarios cargados en el sistema.");
            return null;
        }

        int size = userCLL.size();
        Node current = userCLL.getNode(1);

        for (int i = 0; i < size; i++) {
            User currentUser = (User) current.data;

            if (currentUser.getEmail().equalsIgnoreCase(email)) {
                if (currentUser.checkPassword(password)) {
                    System.out.println("Inicio de sesión exitoso para el usuario: " + currentUser.getName() + " (ID: " + currentUser.getId() + ")");
                    return currentUser;
                } else {
                    System.out.println("Inicio de sesión fallido: Contraseña incorrecta para el usuario '" + email + "'.");
                    return null;
                }
            }

            current = current.next;
        }

        System.out.println("Inicio de sesión fallido: Usuario con email '" + email + "' no encontrado.");
        return null;
    }

    public User loginById(int id, String password) throws ListException {
        if (userCLL.isEmpty()) {
            System.out.println("Inicio de sesión fallido: No hay usuarios cargados en el sistema.");
            return null;
        }

        int size = userCLL.size();
        Node current = userCLL.getNode(1);

        for (int i = 0; i < size; i++) {
            User currentUser = (User) current.data;

            if (currentUser.getId() == id) {
                if (currentUser.checkPassword(password)) {
                    System.out.println("Inicio de sesión exitoso para el usuario: " + currentUser.getName() + " (ID: " + currentUser.getId() + ")");
                    return currentUser;
                } else {
                    System.out.println("Inicio de sesión fallido: Contraseña incorrecta para el usuario ID '" + id + "'.");
                    return null;
                }
            }
            current = current.next;
        }

        System.out.println("Inicio de sesión fallido: Usuario con ID '" + id + "' no encontrado.");
        return null;
    }
}


