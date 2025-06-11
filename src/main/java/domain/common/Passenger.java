package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo; // Importar esta
import com.fasterxml.jackson.annotation.ObjectIdGenerators; // Importar esta
import domain.linkedlist.SinglyLinkedList;

import java.util.ArrayList;
import java.util.List;

// Añadir JsonIdentityInfo para manejar referencias circulares de Passenger
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Passenger{
    private int id;
    private String name;
    private String nationality;
    @JsonIgnore // Ignora el campo SinglyLinkedList directo para la serialización/deserialización
    private SinglyLinkedList flightHistory;

    public Passenger(int id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList();
    }

    public Passenger() {
        this.flightHistory = new SinglyLinkedList();
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }


    public SinglyLinkedList getFlightHistory() {
        return flightHistory;
    }


    public void setFlightHistory(SinglyLinkedList flightHistory) {
        this.flightHistory = flightHistory;
    }



    @JsonGetter("flightHistory")
    public List<Flight> getFlightHistoryAsList() {
        List<Flight> list = new ArrayList<>();
        try {
            if (flightHistory != null) {
                for (int i = 1; i <= flightHistory.size(); i++) {
                    Object data = flightHistory.getNode(i).data;
                    if (data instanceof Flight) { // Asegura que el objeto es de tipo Flight
                        list.add((Flight) data);
                    } else {
                        System.err.println("Advertencia: El objeto en SinglyLinkedList del historial de vuelos no es un Flight. Tipo: " + (data != null ? data.getClass().getName() : "null"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Flight> flightList) {
        this.flightHistory = new SinglyLinkedList();
        if (flightList != null) {
            for (Flight f : flightList) {
                this.flightHistory.add(f);
            }
        }
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}
