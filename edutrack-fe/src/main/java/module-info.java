module com.doamamah.edutrack.fe {
    // JavaFX modules
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires javafx.web;

    // HTTP client & JSON
    requires java.net.http;
    requires com.google.gson;

    // Untuk Desktop.browse() di VideoMaterial
    requires java.desktop;

    // Buka package ke JavaFX FXML (agar controller bisa di-load)
    opens com.doamamah.edutrack.fe to javafx.fxml;
    exports com.doamamah.edutrack.fe;

    opens com.doamamah.edutrack.fe.auth to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.auth;

    opens com.doamamah.edutrack.fe.dashboard to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.dashboard;

    opens com.doamamah.edutrack.fe.material to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.material;

    opens com.doamamah.edutrack.fe.quiz to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.quiz;

    opens com.doamamah.edutrack.fe.enrollment to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.enrollment;

    opens com.doamamah.edutrack.fe.user to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.user;

    opens com.doamamah.edutrack.fe.core to javafx.fxml;
    exports com.doamamah.edutrack.fe.core;
}
