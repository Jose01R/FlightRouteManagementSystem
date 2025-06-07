package domain.common;

import java.time.LocalDateTime;

public class Flight {
    private int number;
    private String origin;
    private String destination;
    private LocalDateTime departureTime; // Usamos LocalDateTime para la fecha y hora
    private int capacity;
    private String occupancy;

    public Flight(int number, String origin, String destination, LocalDateTime departureTime, int capacity, String occupancy) {
        this.number = number;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.occupancy = occupancy;
    }

    // Getters
    public int getNumber() {
        return number;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getOccupancy() {
        return occupancy;
    }

    // Setters
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    @Override
    public String toString() {
        return "Flight [number=" + number + ", origin=" + origin + ", destination=" + destination +
                ", departureTime=" + departureTime + ", capacity=" + capacity + ", occupancy=" + occupancy + "]";
    }
}
