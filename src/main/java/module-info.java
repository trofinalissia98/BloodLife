module com.bloodlife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires com.github.librepdf.openpdf;
    requires javafx.swing;
    requires okhttp3;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    //requires com.bloodlife;

    // Permitem JavaFX să acceseze pachetele pentru a încărca interfața
    opens com.bloodlife to javafx.fxml;
    opens com.bloodlife.controller to javafx.fxml;

    // Exportăm pachetele pentru a fi vizibile în restul aplicației
    exports com.bloodlife;
    exports com.bloodlife.controller;
    exports com.bloodlife.domain;
    exports com.bloodlife.service;
}