package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import domain.linkedstack.LinkedStack;
import domain.linkedstack.StackException; // Keep if push/pop can throw this

import java.util.ArrayList;
import java.util.List;

// @JsonIdentityInfo is important if Airplane objects are referenced multiple times
// in your JSON data to avoid duplication and loops.
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "serialNumber")
public class Airplane {
    private String serialNumber;
    private String model;
    private int totalCapacity;
    private String currentStatus;

    @JsonIgnore
    private LinkedStack flightHistory; //Pila de vuelos completados

    public Airplane(String serialNumber, String model, int totalCapacity) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.totalCapacity = totalCapacity;
        this.currentStatus = "In Service";
        this.flightHistory = new LinkedStack();
    }

    public Airplane() {
        this.currentStatus = "In Service";
        this.flightHistory = new LinkedStack();
    }

    public Airplane(String model) {
        this.model = model;
    }

    // --- Getters and Setters ---

    public LinkedStack getFlightHistory() {
        return flightHistory;
    }

    public void setFlightHistory(LinkedStack flightHistory) {
        this.flightHistory = flightHistory;
    }


    //Jackson usará @JsonGetter para serializar la pila del historial de vuelo en una lista para JSON
    @JsonGetter("flightHistory")
    public ArrayList<Object> getFlightHistoryAsList() {
        if (flightHistory != null) {
            return flightHistory.toList();
        }
        return new ArrayList<>(); //Devuelve una lista vacía si la pila es nula
    }

    //Jackson usará @JsonSetter para deserializar una lista desde JSON y volver a un LinkedStack
    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Flight> flightList) {
        this.flightHistory = new LinkedStack();
        if (flightList != null) {
            //Iterar a través de la lista y hacer push de elementos a la pila
            for (Flight f : flightList) {
                try {
                    this.flightHistory.push(f);
                } catch (StackException e) {
                    //volver a generar Exception (por ejemplo, si se excedió la capacidad de la pila)
                    throw new RuntimeException("Error pushing flight to history during deserialization: " + e.getMessage(), e);
                }
            }
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public String toString() {
        String historySize = "0";
        if (flightHistory != null) {
            historySize = String.valueOf(flightHistory.size());
        }
        return "Airplane{" +
                "serialNumber='" + serialNumber + '\'' +
                ", model='" + model + '\'' +
                ", totalCapacity=" + totalCapacity +
                ", currentStatus='" + currentStatus + '\'' +
                ", flightHistorySize=" + historySize +
                '}';
    }
}