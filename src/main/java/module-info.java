module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    //requires com.fasterxml.jackson.datatype.jsr310;

    opens controller.logincontroller to javafx.fxml;
    opens controller to javafx.fxml;
    exports controller;
    exports controller.logincontroller;

    exports ucr.flightroutemanagementsystem;
    exports domain.common;

    //opens domain.common to com.fasterxml.jackson.databind;

    opens domain.common to com.fasterxml.jackson.databind, com.google.gson;

    opens ucr.flightroutemanagementsystem to javafx.fxml;

    opens domain.linkedlist to com.google.gson;

}
