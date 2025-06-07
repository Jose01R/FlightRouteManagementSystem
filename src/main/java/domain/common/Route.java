package domain.common;

import domain.linkedlist.SinglyLinkedList;

public class Route {
    private int originAirportCode; // Usamos int para el código del aeropuerto de origen
    private SinglyLinkedList destinationList; // Lista de códigos de aeropuertos de destino

    public Route(int originAirportCode) {
        this.originAirportCode = originAirportCode;
        this.destinationList = new SinglyLinkedList();
    }

    // Getters
    public int getOriginAirportCode() {
        return originAirportCode;
    }

    public SinglyLinkedList getDestinationList() {
        return destinationList;
    }


    @Override
    public String toString() {
        return "Route [originAirportCode=" + originAirportCode + ", destinationList=" + destinationList + "]";
    }
}
