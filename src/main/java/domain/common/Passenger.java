package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import domain.linkedlist.SinglyLinkedList; // Import your SinglyLinkedList
import domain.linkedlist.ListException; // If SinglyLinkedList methods can throw this

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For equals and hashCode

// Añadir JsonIdentityInfo para manejar referencias circulares de Passenger
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Passenger {
    private int id;
    private String name;
    private String nationality;

    // --- ADJUSTMENT 1: Make SinglyLinkedList generic ---
    @JsonIgnore // Ignora el campo SinglyLinkedList directo para la serialización/deserialización
    private SinglyLinkedList flightHistory; // Assuming flightHistory stores Flight numbers (Integers) or Flight objects (Flight)

    public Passenger(int id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        // --- ADJUSTMENT 1: Initialize generic SinglyLinkedList ---
        this.flightHistory = new SinglyLinkedList();
    }

    public Passenger() {
        // --- ADJUSTMENT 1: Initialize generic SinglyLinkedList ---
        this.flightHistory = new SinglyLinkedList();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    // --- ADJUSTMENT 1: Make getter generic ---
    public SinglyLinkedList getFlightHistory() { // Or SinglyLinkedList<Flight> if it stores full objects
        return flightHistory;
    }

    // --- ADJUSTMENT 1: Make setter generic ---
    public void setFlightHistory(SinglyLinkedList flightHistory) { // Or SinglyLinkedList<Flight>
        this.flightHistory = flightHistory;
    }

    // --- JSON Getters and Setters for flightHistory ---
    // If flightHistory stores Flight objects:
    @JsonGetter("flightHistory")
    public ArrayList<Object> getFlightHistoryAsList() { // Return List<Integer> if you store flight numbers
        if (flightHistory != null) {
            // --- CRITICAL ASSUMPTION: SinglyLinkedList.toList() exists and works ---
            // It should return ArrayList<Integer> if your SinglyLinkedList is SinglyLinkedList<Integer>
            return flightHistory.toList();
        }
        return new ArrayList<>();
    }

    // If flightHistory stores Flight objects:
    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Integer> flightNumberList) { // Take List<Integer>
        this.flightHistory = new SinglyLinkedList();
        if (flightNumberList != null) {
            for (Integer flightNum : flightNumberList) {
                this.flightHistory.add(flightNum); // Assuming add method works with T
            }
        }
    }

    // --- ADJUSTMENT 2: Add equals() and hashCode() ---
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