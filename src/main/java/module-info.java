module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens controller to javafx.fxml;
    exports controller;

    exports ucr.flightroutemanagementsystem;
    opens ucr.flightroutemanagementsystem to javafx.fxml;
}
