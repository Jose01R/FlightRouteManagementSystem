package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // IMPORT THIS!
import domain.linkedlist.SinglyLinkedList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For equals and hashCode

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "number")
@JsonIgnoreProperties(ignoreUnknown = true) // <--- ADD THIS LINE!
public class Flight {
    private int number;

    // --- ADJUSTMENT 1: Removed redundant origin/destination fields ---
    // private String origin; // These are now derived from assignedRoute
    // private String destination;

    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy;

    @JsonIgnore // Ignora el campo SinglyLinkedList directo para la serialización/deserialización
    private SinglyLinkedList pasajeros; // Now generic for Passenger objects

    private boolean completed; // Corrected typo
    private Airplane assignedAirplane; // Jackson will use @JsonIdentityInfo from Airplane
    private Route assignedRoute;       // Jackson will use @JsonIdentityInfo from Route


    // --- Constructors ---
    public Flight(int number, LocalDateTime departureTime, int capacity,
                  Airplane assignedAirplane, Route assignedRoute) {
        this(); // Call the no-arg constructor to initialize collections
        this.number = number;
        this.capacity = capacity;
        // this.occupancy = 0; // Handled by no-arg constructor
        this.departureTime = departureTime;
        // this.pasajeros = new SinglyLinkedList(); // Handled by no-arg constructor
        // this.completed = false; // Handled by no-arg constructor
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

    // --- JsonGetters to include derived origin/destination in JSON during serialization ---
    @JsonGetter("origin")
    public String getJsonOrigin() { return getOrigin(); }

    @JsonGetter("destination")
    public String getJsonDestination() { return getDestination(); }


    // --- Getters/Setters for assigned objects ---
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

    // --- Getters/Setters for 'pasajeros' (SinglyLinkedList) with custom JSON handling ---
    public SinglyLinkedList getPasajeros() {
        return pasajeros;
    }

    public void setPasajeros(SinglyLinkedList pasajeros) {
        this.pasajeros = pasajeros;
    }

    @JsonGetter("pasajeros")
    public ArrayList<Object> getPasajerosAsList() {
        if (pasajeros != null) {
            // IMPORTANT: SinglyLinkedList.toList() must exist and work correctly
            return pasajeros.toList();
        }
        return new ArrayList<>();
    }

    @JsonSetter("pasajeros")
    public void setPasajerosFromList(List<Passenger> passengerList) {
        this.pasajeros = new SinglyLinkedList(); // Initialize a new SLL
        if (passengerList != null) {
            for (Passenger p : passengerList) {
                this.pasajeros.add(p); // Assuming SinglyLinkedList.add(Object element) works
            }
        }
    }

    // --- Standard Overrides ---
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Flight other = (Flight) obj;
        return this.number == other.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }


    @Override
    public String toString() {
        return "Flight{" +
                "number=" + number +
                ", origin='" + getOrigin() + '\'' + // Use the getter
                ", destination='" + getDestination() + '\'' + // Use the getter
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