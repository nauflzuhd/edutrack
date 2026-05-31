module com.doamamah.edutrack.fe {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;

    opens com.doamamah.edutrack.fe to javafx.fxml;
    exports com.doamamah.edutrack.fe;
    opens com.doamamah.edutrack.fe.controller to javafx.fxml;
    exports com.doamamah.edutrack.fe.controller;
}
