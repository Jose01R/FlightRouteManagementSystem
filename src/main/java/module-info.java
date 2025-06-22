module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires net.bytebuddy;
    requires java.desktop;
    requires kernel;
    requires layout;
    requires itextpdf;
    //requires com.fasterxml.jackson.datatype.jsr310;

    opens controller.logincontroller to javafx.fxml;
    opens controller to javafx.fxml;
    exports controller;
    exports controller.logincontroller;
    exports controller.flightcontroller;
    exports ucr.flightroutemanagementsystem;
    exports domain.common;
    exports  domain.linkedlist;
    opens domain.common to com.fasterxml.jackson.databind, com.google.gson;
    opens controller.flightcontroller to javafx.fxml;
    opens domain.linkedlist to com.fasterxml.jackson.databind, com.google.gson;
    opens ucr.flightroutemanagementsystem to javafx.fxml;
    opens controller.ticketscontroller;
    opens domain.linkedqueue to com.google.gson;
    opens data to com.google.gson;

}
