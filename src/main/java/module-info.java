module com.desk.classmesh {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.poi.ooxml;
    requires kernel;
    requires layout;
    requires io;


    opens com.desk.classmesh to javafx.fxml;
    exports com.desk.classmesh;
}