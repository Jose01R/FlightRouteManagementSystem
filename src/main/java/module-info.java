module ucr.flightroutemanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires net.bytebuddy;
    requires java.desktop;
    requires kernel;       // iText PDF library dependency
    requires layout;      // iText PDF library dependency
    requires itextpdf;    // iText PDF library dependency

    // --- Exports (making packages visible to other modules) ---
    // These exports are generally for public API that other *code modules* might use.
    // If a package contains a controller referenced in FXML, it also often needs to be exported.
    exports ucr.flightroutemanagementsystem;
    exports controller; // If this package contains public classes used directly by other modules
    exports controller.logincontroller;
    exports controller.flightcontroller;
    exports controller.registrationcontroller; // <--- THIS IS THE MISSING EXPORT
    exports controller.ticketscontroller;
    exports data; // Your data access objects
    exports domain.common; // Your common domain models (User, Passenger, etc.)
    exports domain.linkedlist;
    exports domain.linkedqueue;
    exports domain.service; // Your service layer classes
    exports domain.btree; // Your AVL tree and related classes
    exports util; // Your utility classes
    exports controller.login;
    // --- Opens (allowing reflection for FXML and other frameworks like Jackson/Gson) ---
    // These allow specific modules (like javafx.fxml, com.fasterxml.jackson.databind, com.google.gson)
    // to access types and their *private members* within these packages via reflection.
    opens controller.login to javafx.fxml;
    opens ucr.flightroutemanagementsystem to javafx.fxml; // For your main application
    opens controller to javafx.fxml; // For generic controllers or common controller base classes
    opens controller.logincontroller to javafx.fxml;
    opens controller.flightcontroller to javafx.fxml;
    opens controller.registrationcontroller to javafx.fxml; // <--- THIS IS THE MISSING OPEN
    opens controller.ticketscontroller to javafx.fxml; // Ensure this is opened to javafx.fxml
    // If your domain/data objects are used in FXML (e.g., as TableColumn cell value factories)
    // or need reflective access by Jackson/Gson for serialization/deserialization:
    opens data to com.fasterxml.jackson.databind, com.google.gson;
    opens domain.common to com.fasterxml.jackson.databind, com.google.gson;
    opens domain.linkedlist to com.fasterxml.jackson.databind, com.google.gson;
    opens domain.linkedqueue to com.google.gson, com.fasterxml.jackson.databind; // Added jackson.databind here too for consistency
    // If service or util classes are ever accessed reflectively by FXML or other frameworks
    opens domain.service; // Opens to all modules if no 'to' clause.
    opens domain.btree;   // Opens to all modules.
    opens util;           // Opens to all modules.

    // Removed the duplicated and commented out requires com.fasterxml.jackson.datatype.jsr310;
}