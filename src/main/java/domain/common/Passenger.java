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

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Passenger {
    private int id;
    private String name;
    private String nationality;

    @JsonIgnore
    private SinglyLinkedList flightHistory; // Solo almacena números de vuelo (Integer)

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

    // Getter y Setter para la lista de historial de vuelos como lista de números
    @JsonGetter("flightHistory")
    public List<Integer> getFlightHistoryAsList() {
        List<Integer> result = new ArrayList<>();
        if (flightHistory != null) {
            try {
                for (int i = 1; i <= flightHistory.size(); i++) {
                    Object data = null;
                    try {
                        data = flightHistory.getNode(i) != null ? flightHistory.getNode(i).getData() : null;
                    } catch (ListException e) {
                        continue;
                    }
                    if (data instanceof Integer) {
                        result.add((Integer) data);
                    }
                }
            } catch (ListException e) {
                // maneja error de tamaño de lista si ocurre
            }
        }
        return result;
    }

    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Integer> flightNumberList) {
        this.flightHistory = new SinglyLinkedList();
        if (flightNumberList != null) {
            for (Integer flightNum : flightNumberList) {
                if (flightNum != null) {
                    this.flightHistory.add(flightNum);
                }
            }
        }
    }

    @JsonIgnore
    public SinglyLinkedList getFlightHistory() {
        return flightHistory;
    }

    @JsonIgnore
    public void setFlightHistory(SinglyLinkedList flightHistory) {
        this.flightHistory = flightHistory;
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