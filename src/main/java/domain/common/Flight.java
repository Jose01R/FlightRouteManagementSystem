package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import domain.linkedlist.SinglyLinkedList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "number")
public class Flight {
    private int number;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy;
    @JsonIgnore
    private SinglyLinkedList pasajeros;
    private boolean compleated;

    public Flight(int number, String origin, String destination, LocalDateTime departureTime, int capacity) {
        this.number = number;
        this.origin = origin;
        this.destination = destination;
        this.capacity = capacity;
        this.occupancy = 0;
        this.departureTime = departureTime;
        this.pasajeros = new SinglyLinkedList();
        this.compleated = false;
    }

    public Flight() {
        this.pasajeros = new SinglyLinkedList();
        this.occupancy = 0;
        this.compleated = false;
    }


    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public boolean isCompleated() { return compleated; }
    public void setCompleated(boolean compleated) { this.compleated = compleated; }


    public SinglyLinkedList getPasajeros() {
        return pasajeros;
    }


    public void setPasajeros(SinglyLinkedList pasajeros) {
        this.pasajeros = pasajeros;
    }


    @JsonGetter("pasajeros")
    public List<Passenger> getPasajerosAsList() {
        List<Passenger> list = new ArrayList<>();
        try {
            if (pasajeros != null) {
                for (int i = 1; i <= pasajeros.size(); i++) {
                    Object data = pasajeros.getNode(i).data;
                    if (data instanceof Passenger) {
                        list.add((Passenger) data);
                    } else {
                        System.err.println("Advertencia: El objeto en SinglyLinkedList de pasajeros no es un Pasajero. Tipo: " + (data != null ? data.getClass().getName() : "null"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    @JsonSetter("pasajeros")
    public void setPasajerosFromList(List<Passenger> passengerList) {
        this.pasajeros = new SinglyLinkedList();
        if (passengerList != null) {
            for (Passenger p : passengerList) {
                this.pasajeros.add(p);
            }
        }
    }

    @Override
    public String toString() {
        return "Flight{" +
                "number=" + number +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", departureTime=" + departureTime +
                ", capacity=" + capacity +
                ", occupancy=" + occupancy +
                ", completed=" + compleated +
                '}';
    }

    public void incrementOccupancy() {
        if (this.occupancy < this.capacity) {
            this.occupancy++;
        } else {
            System.out.println("Advertencia: El vuelo " + this.number + " ya está a su máxima capacidad.");
        }
    }

    public void decrementOccupancy() {
        if (this.occupancy > 0) {
            this.occupancy--;
        } else {
            System.out.println("Advertencia: La ocupación del vuelo " + this.number + " ya es 0.");
        }
    }
}