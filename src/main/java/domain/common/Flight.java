package domain.common;

import com.fasterxml.jackson.annotation.*;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedlist.ListException;
import domain.service.PassengerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "number")
public class Flight {
    private int number;
    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy;
    @JsonIgnore
    private SinglyLinkedList pasajeros; // Contendrá objetos Passenger

    private boolean completed;
    private Airplane assignedAirplane;
    private Route assignedRoute;

    public Flight(int number, LocalDateTime departureTime, int capacity,
                  Airplane assignedAirplane, Route assignedRoute) {
        this();
        this.number = number;
        this.capacity = capacity;
        this.departureTime = departureTime;
        this.assignedAirplane = assignedAirplane;
        this.assignedRoute = assignedRoute;
    }

    public Flight() {
        this.pasajeros = new SinglyLinkedList();
        this.occupancy = 0;
        this.completed = false;
    }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getOrigin() {
        return assignedRoute != null ? String.valueOf(assignedRoute.getOriginAirportCode()) : null;
    }
    public String getDestination() {
        return assignedRoute != null ? String.valueOf(assignedRoute.getDestinationAirportCode()) : null;
    }

    @JsonGetter("origin")
    public String getJsonOrigin() { return getOrigin(); }

    @JsonGetter("destination")
    public String getJsonDestination() { return getDestination(); }

    public Airplane getAssignedAirplane() { return assignedAirplane; }
    public void setAssignedAirplane(Airplane assignedAirplane) { this.assignedAirplane = assignedAirplane; }
    public Route getAssignedRoute() { return assignedRoute; }
    public void setAssignedRoute(Route assignedRoute) { this.assignedRoute = assignedRoute; }

    @JsonIgnore
    public SinglyLinkedList getPasajeros() {
        return pasajeros;
    }

    @JsonIgnore
    public void setPasajeros(SinglyLinkedList pasajeros) {
        this.pasajeros = pasajeros;
    }

    // SERIALIZACIÓN: solo los ids de los pasajeros
    @JsonGetter("pasajeros")
    public List<Integer> getPasajerosIds() {
        List<Integer> ids = new ArrayList<>();
        if (pasajeros != null) {
            try {
                for (int i = 1; i <= pasajeros.size(); i++) {
                    Object obj = pasajeros.getNode(i).getData();
                    if (obj instanceof Passenger) {
                        ids.add(((Passenger) obj).getId());
                    }
                }
            } catch (ListException e) {
                System.err.println("Error (Flight.getPasajerosIds): " + e.getMessage());
            }
        }
        return ids;
    }

    // DESERIALIZACIÓN: requiere el PassengerService para buscar los pasajeros por id
    // Se recomienda llamar a este método MANUALMENTE después de deserializar y cargar los pasajeros globales.
    @JsonSetter("pasajeros")
    public void setPasajerosFromIds(List<Integer> ids) {
        // REQUIERE que PassengerService esté accesible aquí, así que omite la carga automática
        // y utiliza un método helper después de deserializar para rellenar la SLL.
        this.pasajeros = new SinglyLinkedList();
        // Aquí solo almacena temporalmente los IDs, luego usa setPasajerosByService()
        if (ids != null) {
            for (Integer id : ids) {
                this.pasajeros.add(id); // Temporal, luego se reemplazará por Passenger real
            }
        }
    }

    // MÉTODO HELPER: llama esto después de deserializar para reemplazar los ids por los Passenger reales
    public void replaceIdsWithPassengers(PassengerService passengerService) {
        if (this.pasajeros != null) {
            SinglyLinkedList nuevaLista = new SinglyLinkedList();
            try {
                for (int i = 1; i <= this.pasajeros.size(); i++) {
                    Object obj = this.pasajeros.getNode(i).getData();
                    if (obj instanceof Integer) {
                        Passenger p = passengerService.findPassengerById((Integer) obj);
                        if (p != null) nuevaLista.add(p);
                    } else if (obj instanceof Passenger) {
                        nuevaLista.add(obj); // por si ya es Passenger
                    }
                }
                this.pasajeros = nuevaLista;
            } catch (ListException e) {
                System.err.println("Error (Flight.replaceIdsWithPassengers): " + e.getMessage());
            }
        }
    }

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
        try {
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
                    ", numPasajeros=" + (pasajeros != null ? pasajeros.size() : 0) +
                    '}';
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
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