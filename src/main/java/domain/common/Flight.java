package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import domain.linkedlist.SinglyLinkedList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "number")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {
    private int number;

   //origen y destino derivados de route

    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy;

    @JsonIgnore //Ignora el campo SinglyLinkedList directo para la serialización/deserialización
    private SinglyLinkedList pasajeros; // Now generic for Passenger objects

    private boolean completed; // Corrected typo
    private Airplane assignedAirplane;
    private Route assignedRoute;


    // --- Constructors ---
    public Flight(int number, LocalDateTime departureTime, int capacity,
                  Airplane assignedAirplane, Route assignedRoute) {
        this(); // Call the no-arg constructor to initialize collections
        this.number = number;
        this.capacity = capacity;
        // this.occupancy = 0;
        this.departureTime = departureTime;
        // this.pasajeros = new SinglyLinkedList();
        // this.completed = false;
        this.assignedAirplane = assignedAirplane;
        this.assignedRoute = assignedRoute;
    }

    public Flight() {
        this.pasajeros = new SinglyLinkedList();
        this.occupancy = 0;
        this.completed = false;
    }


    // --- Getters and Setters for direct fields ---
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public boolean isCompleted() { return completed; } // Jackson expects 'is' for boolean getters
    public void setCompleted(boolean completed) { this.completed = completed; }

    // --- Derived Getters (from assignedRoute) ---
    // These are *not* direct fields in the Flight class, but expose route info.
    public String getOrigin() {
        return assignedRoute != null ? String.valueOf(assignedRoute.getOriginAirportCode()) : null;
    }
    public String getDestination() {
        return assignedRoute != null ? String.valueOf(assignedRoute.getDestinationAirportCode()) : null;
    }

    // --- JsonGetters para incluir el origen/destino derivado en JSON durante serialización ---
    @JsonGetter("origin")
    public String getJsonOrigin() { return getOrigin(); }

    @JsonGetter("destination")
    public String getJsonDestination() { return getDestination(); }


    public Airplane getAssignedAirplane() {
        return assignedAirplane;
    }
    public void setAssignedAirplane(Airplane assignedAirplane) {
        this.assignedAirplane = assignedAirplane;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }
    public void setAssignedRoute(Route assignedRoute) {
        this.assignedRoute = assignedRoute;
    }


    public SinglyLinkedList getPasajeros() { return pasajeros; }
    public void setPasajeros(SinglyLinkedList pasajeros) { this.pasajeros = pasajeros; }

    @JsonGetter("pasajeros")
    public ArrayList<Object> getPasajerosAsList() {
        if (pasajeros != null) {
            return pasajeros.toList();
        }
        return new ArrayList<>();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return number == flight.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "number=" + number +
                ", origin='" + getOrigin() + '\'' +
                ", destination='" + getDestination() + '\'' +
                ", departureTime=" + departureTime +
                ", capacity=" + capacity +
                ", occupancy=" + occupancy +
                ", completed=" + completed +
                ", assignedAirplaneSN='" + (assignedAirplane != null ? assignedAirplane.getSerialNumber() : "N/A") + '\'' +
                ", assignedRouteNum='" + (assignedRoute != null ? assignedRoute.getRouteId() : "N/A") + '\'' +
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