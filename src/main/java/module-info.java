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

    opens domain.common to com.fasterxml.jackson.databind;

    opens domain.linkedlist to com.fasterxml.jackson.databind;
    opens ucr.flightroutemanagementsystem to javafx.fxml;
}
