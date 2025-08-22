package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class HomePanel extends VBox {
    private App app;
    private Button scanImageButton;
    private Button uploadImageButton;
    private Button settingsButton;
    private Button aboutButton;
    private Button powerOffButton;

    public HomePanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        scanImageButton = new Button("Scan Image");
        uploadImageButton = new Button("Upload Image");
        settingsButton = new Button("Settings");
        aboutButton = new Button("About");
        powerOffButton = new Button("Power Off");

        scanImageButton.setPrefWidth(200);
        uploadImageButton.setPrefWidth(200);
        settingsButton.setPrefWidth(200);
        aboutButton.setPrefWidth(200);
        powerOffButton.setPrefWidth(200);
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        getChildren().addAll(
                scanImageButton,
                uploadImageButton,
                settingsButton,
                aboutButton,
                powerOffButton
        );
    }

    private void setupEventHandlers() {
        scanImageButton.setOnAction(e -> app.showScanImagePanel());
        uploadImageButton.setOnAction(e -> app.showUploadImagePanel());
        settingsButton.setOnAction(e -> {});
        aboutButton.setOnAction(e -> {});
        powerOffButton.setOnAction(e -> Platform.exit());
    }
}