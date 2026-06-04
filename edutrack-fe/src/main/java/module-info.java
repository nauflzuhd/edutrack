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

    opens com.doamamah.edutrack.fe.controller to javafx.fxml;
    exports com.doamamah.edutrack.fe.controller;

    // Buka model ke Gson juga (agar JSON parsing bisa akses field via reflection)
    opens com.doamamah.edutrack.fe.model to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.model;

    opens com.doamamah.edutrack.fe.service to javafx.fxml, com.google.gson;
    exports com.doamamah.edutrack.fe.service;

    opens com.doamamah.edutrack.fe.util to javafx.fxml;
    exports com.doamamah.edutrack.fe.util;
}
