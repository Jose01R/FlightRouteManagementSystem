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
// in your JSON data (e.g., by multiple Flights) to avoid duplication and loops.
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "serialNumber")
public class Airplane {
    private String serialNumber;
    private String model;
    private int totalCapacity;
    private String currentStatus;

    // --- ADJUSTMENT: Make LinkedStack generic for better type safety ---
    @JsonIgnore
    private LinkedStack flightHistory; // Stack for completed flights

    public Airplane(String serialNumber, String model, int totalCapacity) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.totalCapacity = totalCapacity;
        this.currentStatus = "In Service";
        // --- ADJUSTMENT: Initialize generic LinkedStack ---
        this.flightHistory = new LinkedStack();
    }

    public Airplane() {
        this.currentStatus = "In Service";
        // --- ADJUSTMENT: Initialize generic LinkedStack ---
        this.flightHistory = new LinkedStack();
    }

    public Airplane(String model) {
        this.model = model;
    }

    // --- Getters and Setters ---

    // --- ADJUSTMENT: Make getter generic ---
    public LinkedStack getFlightHistory() {
        return flightHistory;
    }

    // --- ADJUSTMENT: Make setter generic ---
    public void setFlightHistory(LinkedStack flightHistory) {
        this.flightHistory = flightHistory;
    }

    // --- Updated JSON methods for flightHistory ---
    // @JsonGetter will be used by Jackson to serialize the flightHistory stack
    // into a List for JSON.
    @JsonGetter("flightHistory")
    public ArrayList<Object> getFlightHistoryAsList() {
        if (flightHistory != null) {
            // --- CRITICAL ASSUMPTION: LinkedStack.toList() exists and works ---
            // This method in your LinkedStack class should iterate through the stack
            // and return an ArrayList of its elements (type T).
            return flightHistory.toList(); // Assuming toList() returns ArrayList<Flight> if generic
        }
        return new ArrayList<>(); // Return empty list if stack is null
    }

    // @JsonSetter will be used by Jackson to deserialize a List from JSON
    // back into a LinkedStack.
    @JsonSetter("flightHistory")
    public void setFlightHistoryFromList(List<Flight> flightList) {
        this.flightHistory = new LinkedStack();
        if (flightList != null) {
            // Iterate through the list and push elements onto the stack.
            // The order depends on how you want them stacked from the list.
            // If the list is [oldest, ..., most_recent], pushing them in order
            // will put 'most_recent' at the top of the stack.
            for (Flight f : flightList) {
                try {
                    this.flightHistory.push(f);
                } catch (StackException e) {
                    // Handle or rethrow StackException (e.g., if stack capacity was exceeded)
                    // For typical unbounded LinkedStacks, this might not occur for 'push'.
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
                ", flightHistorySize=" + historySize + // Conciser way to show history
                '}';
    }
}