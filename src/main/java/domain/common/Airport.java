package domain.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import domain.linkedlist.SinglyLinkedList;
import domain.linkedqueue.LinkedQueue;
import domain.linkedqueue.QueueException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Airport {
    private int code;
    private String name;
    private String country;
    private String status; // "active = 1" o "inactive = 0"

    // @JsonIgnore tells Jackson to ignore this field during default serialization/deserialization
    @JsonIgnore
    private SinglyLinkedList departuresBoard; // For departure information

    @JsonIgnore
    private LinkedQueue boardingQueue; // Assumed to hold Passenger objects

    // --- Constructors ---
    public Airport(int code, String name, String country, String status) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = status;
        this.departuresBoard = new SinglyLinkedList(); // Initialize
        this.boardingQueue = new LinkedQueue();       // Initialize
    }

    public Airport(int code) {
        this.code = code;
        this.departuresBoard = new SinglyLinkedList(); // Initialize
        this.boardingQueue = new LinkedQueue();       // Initialize
    }

    public Airport() {
        // Initialize your custom data structures here to prevent NullPointerExceptions
        this.departuresBoard = new SinglyLinkedList();
        this.boardingQueue = new LinkedQueue();
    }

    // --- Getters and Setters for standard fields ---
    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- Custom JSON Handling for departuresBoard (if needed) ---
    // If you need to persist departuresBoard, you'll need similar @JsonGetter/@JsonSetter
    // for SinglyLinkedList and a toList() method in SinglyLinkedList.
    // For now, it's @JsonIgnore, meaning it won't be serialized/deserialized by default.
    public SinglyLinkedList getDeparturesBoard() {
        return departuresBoard;
    }
    // No direct setter if its content is managed through the list methods.

    // --- Custom JSON Handling for boardingQueue ---
    // This is the core of the fix for your error.

    // This is the standard getter that returns the LinkedQueue object directly.
    // Marked @JsonIgnore above, so it's not directly serialized by default.
    public LinkedQueue getBoardingQueue() {
        return boardingQueue;
    }

    // This setter is for internal use to replace the queue.
    public void setBoardingQueue(LinkedQueue boardingQueue) {
        this.boardingQueue = boardingQueue;
    }

    /**
     * Custom getter for Jackson to serialize the 'boardingQueue' as a List of Passengers.
     * Jackson will call this method when serializing, and use its return value for the JSON field "boardingQueue".
     *
     * IMPORTANT: This requires LinkedQueue.java to have a public `toList()` method
     * that returns an `ArrayList<Object>` or `ArrayList<Passenger>`.
     * If `toList()` throws `QueueException`, you need to handle it or let it propagate.
     * I've added a try-catch for robustness.
     */
    @JsonGetter("boardingQueue") // Name the JSON field "boardingQueue"
    public ArrayList<Object> getBoardingQueueForJson() {
        if (boardingQueue != null) {
            // Assuming toList() returns ArrayList<Object>
            return boardingQueue.toList();
        }
        return new ArrayList<>(); // If queue is null, return empty list
    }

    /**
     * Custom setter for Jackson to deserialize a JSON array into the 'boardingQueue'.
     * Jackson will call this method when deserializing the "boardingQueue" JSON array.
     * It expects a List of objects that can be enqueued into the LinkedQueue.
     *
     * IMPORTANT: This requires LinkedQueue.java to have a public `enQueue(Object)` method.
     */
    @JsonSetter("boardingQueue") // Map the JSON field "boardingQueue" to this setter
    public void setBoardingQueueFromJson(List<Object> elements) { // Use List<Object> to be flexible
        this.boardingQueue = new LinkedQueue(); // Initialize a new queue
        if (elements != null) {
            for (Object element : elements) {
                try {
                    // Assuming elements are Passenger, or some other object type stored in queue.
                    // Jackson will attempt to deserialize each element based on its type.
                    this.boardingQueue.enQueue(element);
                } catch (QueueException e) {
                    System.err.println("Error enqueuing element during boardingQueue deserialization: " + e.getMessage());
                    // You might want to throw a RuntimeException here if a critical element fails to enqueue
                }
            }
        }
    }

    // --- Standard Overrides ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return code == airport.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Airport [code=" + code + ", name=" + name + ", country=" + country + ", status=" + status + "]";
    }
}