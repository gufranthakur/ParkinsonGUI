package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScanImagePanel extends VBox {
    private App app;
    private ImageView cameraFeed;
    private Button openCheeseButton;
    private Button stopCheeseButton;
    private Button selectImageButton;
    private Button goBackButton;
    private Label statusLabel;
    private Process cheeseProcess;
    private String picturesDirectory;

    public ScanImagePanel(App app) {
        this.app = app;
        this.picturesDirectory = System.getProperty("user.home") + "/Pictures";
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        cameraFeed = new ImageView();
        cameraFeed.setFitWidth(600);
        cameraFeed.setFitHeight(400);
        cameraFeed.setPreserveRatio(true);

        statusLabel = new Label("Click 'Open Cheese' to capture an image");
        openCheeseButton = new Button("Open Cheese Camera");
        stopCheeseButton = new Button("Stop Cheese");
        selectImageButton = new Button("Select Image");
        goBackButton = new Button("Go Back");

        openCheeseButton.setPrefWidth(150);
        stopCheeseButton.setPrefWidth(100);
        selectImageButton.setPrefWidth(150);
        goBackButton.setPrefWidth(150);

        stopCheeseButton.setDisable(true);
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(20);

        HBox cheeseControlBox = new HBox(10);
        cheeseControlBox.setAlignment(Pos.CENTER);
        cheeseControlBox.getChildren().addAll(openCheeseButton, stopCheeseButton);

        getChildren().addAll(
                cameraFeed,
                statusLabel,
                cheeseControlBox,
                selectImageButton,
                goBackButton
        );
    }

    private void setupEventHandlers() {
        openCheeseButton.setOnAction(e -> openCheese());
        stopCheeseButton.setOnAction(e -> stopCheese());
        selectImageButton.setOnAction(e -> selectImage());
        goBackButton.setOnAction(e -> {
            stopCheese();
            app.showHomePanel();
        });
    }

    private void openCheese() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cheese");
            cheeseProcess = pb.start();

            openCheeseButton.setDisable(true);
            stopCheeseButton.setDisable(false);
            statusLabel.setText("Cheese camera opened. Take a photo and click 'Select Image'");

        } catch (IOException e) {
            statusLabel.setText("Failed to open Cheese. Please install: sudo apt install cheese");
        }
    }

    private void stopCheese() {
        if (cheeseProcess != null && cheeseProcess.isAlive()) {
            cheeseProcess.destroyForcibly();
            cheeseProcess = null;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("pkill", "cheese");
            pb.start().waitFor();
        } catch (Exception e) {
            // Ignore if pkill fails
        }

        openCheeseButton.setDisable(false);
        stopCheeseButton.setDisable(true);
        statusLabel.setText("Cheese camera stopped");
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Captured Image");
        fileChooser.setInitialDirectory(new File(picturesDirectory));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(app.getPrimaryStage());
        if (selectedFile != null) {
            processSelectedImage(selectedFile);
        }
    }

    private void processSelectedImage(File selectedFile) {
        statusLabel.setText("Processing image...");

        Thread processThread = new Thread(() -> {
            try {
                File directory = new File(App.IMAGE_STORAGE_PATH);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = App.IMAGE_STORAGE_PATH + "captured_" + timestamp + ".png";
                File destinationFile = new File(filename);

                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                javafx.application.Platform.runLater(() -> {
                    try {
                        Image image = new Image(destinationFile.toURI().toString());
                        cameraFeed.setImage(image);
                        statusLabel.setText("Image processed successfully");

                        stopCheese();
                        app.showRunPanel();

                    } catch (Exception e) {
                        statusLabel.setText("Failed to display image: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Failed to process image: " + e.getMessage());
                });
            }
        });

        processThread.setDaemon(true);
        processThread.start();
    }
}