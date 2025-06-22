package domain.common;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.time.LocalTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "routeId")
public class Route {
    private String routeId; // Ejm: AA123
    private String airline;
    private double durationHours; // Costo de duración
    private double distanceKm;    // Costo de distancia
    private double price;         // Costo de precio
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    private int originAirportCode;
    private int destinationAirportCode;

    // CONSTRUCTOR VACIO PARA JSON
    public Route() {}

    public Route(String routeId, int originAirportCode, int destinationAirportCode,
                 String airline, double durationHours, double distanceKm, double price,
                 LocalTime departureTime, LocalTime arrivalTime) {
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be null or empty.");
        }
        if (durationHours <= 0 || distanceKm <= 0 || price <= 0) {
            throw new IllegalArgumentException("Duration, distance, and price must be positive.");
        }
        this.routeId = routeId;

        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;

        this.airline = airline;
        this.durationHours = durationHours;
        this.distanceKm = distanceKm;
        this.price = price;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    // GETTERS (no setters, assuming immutability after creation)
    public String getRouteId() { return routeId; }
    public String getAirline() { return airline; }
    public double getDurationHours() { return durationHours; }
    public double getDistanceKm() { return distanceKm; }
    public double getPrice() { return price; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }

    public int getOriginAirportCode() { return originAirportCode; }
    public int getDestinationAirportCode() { return destinationAirportCode; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(routeId, route.routeId); // Ruta es única por su ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", airline='" + airline + '\'' +
                ", durationHours=" + durationHours +
                ", distanceKm=" + distanceKm +
                ", price=" + String.format("%.2f", price) +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", originAirportCode=" + originAirportCode +
                ", destinationAirportCode=" + destinationAirportCode +
                '}';
    }
}