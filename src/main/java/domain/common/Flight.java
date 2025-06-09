package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import domain.linkedlist.SinglyLinkedList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private int number;
    private String origin;
    private String destination;
    private LocalDateTime departureTime; // Usamos LocalDateTime para la fecha y hora
    private int capacity;
    private int occupancy;
    @JsonIgnore
    private SinglyLinkedList pasajeros;
    private boolean compleated;
    public Flight(int number, String origin, String destination, LocalDateTime departureTime, int capacity) {
        this.number = number;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.occupancy = 0;
        pasajeros= new SinglyLinkedList();
        this.compleated=false;

    }

    public Flight() {
        this.pasajeros= new SinglyLinkedList();
        this.occupancy=0;
        this.compleated=false;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SinglyLinkedList getPasajeros() {
        return pasajeros;
    }

    public void setPasajeros(SinglyLinkedList pasajeros) {
        this.pasajeros = pasajeros;
    }


    public boolean isCompleated() {
        return compleated;
    }

    public void setCompleated(boolean compleated) {
        this.compleated = compleated;
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

    public int getOccupancy() {
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

    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }


    // 🚀 Serializador auxiliar: convierte la lista personalizada en una lista simple
    @JsonGetter("pasajeros")
    public List<Object> getFlightHistoryAsList() {
        List<Object> list = new ArrayList<>();
        try {
            for (int i = 1; i <= pasajeros.size(); i++) {
                list.add(pasajeros.getNode(i).data);
            }
        } catch (Exception e) {
            e.printStackTrace(); // por seguridad
        }
        return list;
    }

    // 🔁 Deserializador auxiliar: convierte lista común a SinglyLinkedList
    @JsonSetter("pasajeros")
    public void setFlightHistoryFromList(List<Object> passengerList) {
        this.pasajeros = new SinglyLinkedList();
        for (Object p : passengerList) {
            this.pasajeros.add(p); // o castear si sabes que es Passenger
        }
    }

    @Override
    public String toString() {
        // ¡IMPORTANTE! No incluyas 'pasajeros' directamente aquí para evitar recursión infinita
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
            // Opcional: manejar el caso de que el vuelo ya esté lleno
            System.out.println("Advertencia: El vuelo " + this.number + " ya está a su máxima capacidad.");
        }
    }

    // Método para decrementar la ocupación (si un pasajero cancela, etc.)
    public void decrementOccupancy() {
        if (this.occupancy > 0) {
            this.occupancy--;
        } else {
            // Opcional: manejar el caso de que la ocupación ya sea 0
            System.out.println("Advertencia: La ocupación del vuelo " + this.number + " ya es 0.");
        }
    }
}
