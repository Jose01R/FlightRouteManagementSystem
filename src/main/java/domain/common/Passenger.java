package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import domain.linkedlist.SinglyLinkedList;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    private int id;
    private String name;
    private String nationality;
    @JsonIgnore
    private SinglyLinkedList flightHistory;

    public Passenger() {
    }

    public Passenger(int id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFlightHistory(SinglyLinkedList flightHistory) {
        this.flightHistory = flightHistory;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public SinglyLinkedList getFlightHistory() {
        return flightHistory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    // 🚀 Serializador auxiliar: convierte la lista personalizada en una lista simple
    @JsonGetter("flightHistory")
    public List<Object> getFlightHistoryAsList() {
        List<Object> list = new ArrayList<>();
        try {
            for (int i = 1; i <= flightHistory.size(); i++) {
                list.add(flightHistory.getNode(i));
            }
        } catch (Exception e) {
            e.printStackTrace(); // por seguridad
        }
        return list;
    }

    // 🔁 Deserializador auxiliar: convierte lista común a SinglyLinkedList
    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Object> flightList) {
        this.flightHistory = new SinglyLinkedList();
        for (Object item : flightList) {
            this.flightHistory.add(item);
        }
    }

    @Override
    public String toString() {
        // ¡IMPORTANTE! No incluyas 'flightHistory' directamente aquí para evitar recursión infinita
        return "Passenger{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}
