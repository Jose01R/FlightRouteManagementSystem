package domain.persistencemanager;

import com.google.gson.reflect.TypeToken;
import domain.btree.AVL;
import domain.btree.TreeException;
import domain.common.Passenger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonHandlerTest {

    @Test
    void test() {

        AVL avl = new AVL();
        avl.add(new Passenger(83937596,"Maria", "Costarricense"));
        avl.add(new Passenger(305630386,"Miguel", "Costarricense"));

        //Convertir a lista para guardar
        List<Passenger> pasajeros = avl.toList();

        JsonHandler handler = new JsonHandler("passengers.json");
        try {
            handler.save(pasajeros);

        System.out.println("Pasajeros guardados con éxito.");

        Type listType = new TypeToken<List<Passenger>>(){}.getType();
        List<Passenger> pasajerosCargados = handler.load(listType);

        AVL nuevoAvl = AVL.fromList(pasajerosCargados);
        System.out.println("Árbol AVL reconstruido con " + nuevoAvl.size() + " pasajeros.");
           Passenger passenger=(Passenger) nuevoAvl.getRoot().right.data;
           System.out.println(passenger);

            System.out.println(nuevoAvl.getPassengerById(305630386));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TreeException e) {
            throw new RuntimeException(e);
        }
    }
}