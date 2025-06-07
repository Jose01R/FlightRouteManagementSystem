package domain.common;

import domain.linkedlist.SinglyLinkedList;

public class Airport {
    private int code;
    private String name;
    private String country;
    private String status; // "active = 1" o "inactive = 0"
    private SinglyLinkedList departuresBoard;

    public Airport(int code, String name, String country, String status) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = status;
        this.departuresBoard = new SinglyLinkedList();
    }


    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getStatus() {
        return status;
    }

    public SinglyLinkedList getDeparturesBoard() {
        return departuresBoard;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Airport [code=" + code + ", name=" + name + ", country=" + country + ", status=" + status + "]";
    }
}
