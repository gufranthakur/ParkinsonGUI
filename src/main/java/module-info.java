module com.parkinsongui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires webcam.capture;
    requires javafx.swing;
    requires atlantafx.base;
    requires jdk.httpserver;
    requires com.google.zxing;
    requires javax.jmdns;

    exports com.parkinsongui;
}