module com.desk.classmesh {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.desk.classmesh to javafx.fxml;
    exports com.desk.classmesh;
}