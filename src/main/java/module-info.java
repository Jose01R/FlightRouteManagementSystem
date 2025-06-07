module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    //requires com.fasterxml.jackson.datatype.jsr310;

    opens controller to javafx.fxml;
    exports controller;

    exports ucr.flightroutemanagementsystem;
    exports domain.common;

    opens ucr.flightroutemanagementsystem to javafx.fxml;
}
