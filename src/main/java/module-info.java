module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    //requires com.fasterxml.jackson.datatype.jsr310;

    opens controller to javafx.fxml;
    exports controller;

    exports ucr.flightroutemanagementsystem;
    exports domain.common;


    opens domain.common to com.google.gson;
    opens domain.btree to com.google.gson;
    opens domain.linkedlist to com.google.gson;

    opens ucr.flightroutemanagementsystem to javafx.fxml;
}
