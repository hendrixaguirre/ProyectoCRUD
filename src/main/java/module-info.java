module ni.edu.uam.proyectocrud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.proyectocrud to javafx.fxml;
    opens ni.edu.uam.proyectocrud.controller to javafx.fxml;

    exports ni.edu.uam.proyectocrud;
    exports ni.edu.uam.proyectocrud.controller;
    exports ni.edu.uam.proyectocrud.model;
    exports ni.edu.uam.proyectocrud.database;
}