module com.example.proyecto3trimestre {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires java.sql;


    opens com.example.proyecto3trimestre to javafx.fxml;
    exports com.example.proyecto3trimestre;
}