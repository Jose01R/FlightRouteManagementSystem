package domain.passengers;

import domain.linkedlist.SinglyLinkedList;

public class Passenger {


    private  int id;
    private String name;
    private String nationality;
    private SinglyLinkedList flight_history;
    private static int currentId=1;

    public Passenger(String name, String nationality, SinglyLinkedList flight_history) {
        id=currentId++;  //Incrementar el valor del id por cada nuevo pasajero
        this.name = name;
        this.nationality = nationality;
        this.flight_history = flight_history;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public static int getCurrentId() {
        return currentId;
    }

    public static void setCurrentId(int currentId) {
        Passenger.currentId = currentId;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public SinglyLinkedList getFlight_history() {
        return flight_history;
    }

    public void setFlight_history(SinglyLinkedList flight_history) {
        this.flight_history = flight_history;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                ", flight_history=" + flight_history +
                '}';
    }
}
