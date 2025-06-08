package domain.common;

import domain.linkedlist.SinglyLinkedList;

public class Passenger {
    private int id;
    private String name;
    private String nationality;
    private SinglyLinkedList flightHistory;


    public Passenger(int id,String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFlightHistory(SinglyLinkedList flightHistory) {
        this.flightHistory = flightHistory;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public SinglyLinkedList getFlightHistory() {
        return flightHistory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }



    @Override
    public String toString() {
        return "Passenger [id=" + id + ", name=" + name + ", nationality=" + nationality + "]";
    }
}
