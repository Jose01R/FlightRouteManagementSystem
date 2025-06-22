package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedlist.ListException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//JsonIdentityInfo para manejar referencias circulares de Passenger
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Passenger {
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

    // --- JSON Getters y Setters para flightHistory ---
    @JsonGetter("flightHistory")
    public ArrayList<Object> getFlightHistoryAsList() {
        if (flightHistory != null) {
            return flightHistory.toList();
        }
        return new ArrayList<>();
    }

    // If flightHistory stores Flight objects:
    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Integer> flightNumberList) {
        this.flightHistory = new SinglyLinkedList();
        if (flightNumberList != null) {
            for (Integer flightNum : flightNumberList) {
                this.flightHistory.add(flightNum);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return id == passenger.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        try {
            return "Passenger{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", nationality='" + nationality + '\'' +
                    ", flightHistorySize=" + (flightHistory != null ? flightHistory.size() : 0) +
                    '}';
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }
}