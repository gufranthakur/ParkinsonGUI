package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class ScanImagePanel extends ScrollPane {
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
        cameraFeed.setFitWidth(520);
        cameraFeed.setFitHeight(280);
        cameraFeed.setPreserveRatio(true);

        statusLabel = new Label("Click 'Open Cheese' to capture an image");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #adb5bd; -fx-font-weight: 600;");

        // Modern button style
        String baseButtonStyle = """
            -fx-pref-width: 180;
            -fx-pref-height: 45;
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            -fx-background-radius: 12;
            -fx-text-fill: #f1f3f5;
            -fx-background-color: #2a2d31;
            -fx-border-color: linear-gradient(to right, #4dabf7, #228be6);
            -fx-border-width: 1.5;
            -fx-border-radius: 12;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);
        """;

        openCheeseButton = new Button("Open Cheese Camera");
        stopCheeseButton = new Button("Stop Cheese");
        selectImageButton = new Button("Select Image");
        goBackButton = new Button("Go Back");

        openCheeseButton.setStyle(baseButtonStyle);
        stopCheeseButton.setStyle(baseButtonStyle + "-fx-border-color: linear-gradient(to right, #fa5252, #e03131);");
        selectImageButton.setStyle(baseButtonStyle + "-fx-border-color: linear-gradient(to right, #51cf66, #2f9e44);");
        goBackButton.setStyle(baseButtonStyle + "-fx-border-color: linear-gradient(to right, #868e96, #495057);");

        stopCheeseButton.setDisable(true);

        // Hover animations (color change only)
        addHoverEffect(openCheeseButton, "#3a3f44");
        addHoverEffect(stopCheeseButton, "#3a3f44");
        addHoverEffect(selectImageButton, "#3a3f44");
        addHoverEffect(goBackButton, "#3a3f44");
    }

    private void setupLayout() {
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setSpacing(25);
        mainContent.setPadding(new Insets(20, 45, 20, 45));

        // Card wrapper
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setStyle("""
            -fx-background-color: rgba(25,25,25,1);
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: linear-gradient(to right, #4dabf7, #228be6);
            -fx-border-width: 1.5;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 18, 0.2, 0, 8);
        """);

        // Header
        Label titleLabel = new Label("Image Capture & Selection");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #f8f9fa;");

        // Camera container
        VBox imageContainer = new VBox(15, cameraFeed, statusLabel);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("""
            -fx-background-color: rgba(255,255,255,0.03);
            -fx-background-radius: 12;
            -fx-border-color: #495057;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-padding: 20;
        """);

        // Buttons
        HBox cheeseControlBox = new HBox(15, openCheeseButton, stopCheeseButton);
        cheeseControlBox.setAlignment(Pos.CENTER);

        VBox buttonContainer = new VBox(15, cheeseControlBox, selectImageButton, goBackButton);
        buttonContainer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleLabel, imageContainer, buttonContainer);
        mainContent.getChildren().add(card);

        // ScrollPane config
        setContent(mainContent);
        setFitToWidth(true);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setStyle("-fx-background-color: transparent;");
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

    private void addHoverEffect(Button button, String hoverBg) {
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: " + hoverBg + ";"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: " + hoverBg + ";", "")));
    }

    private void openCheese() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cheese");
            cheeseProcess = pb.start();

            openCheeseButton.setDisable(true);
            stopCheeseButton.setDisable(false);
            statusLabel.setText("Cheese camera opened. Take a photo and click 'Select Image'");
            statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #51cf66; -fx-font-weight: 600;");

        } catch (IOException e) {
            statusLabel.setText("Failed to open Cheese. Please install: sudo apt install cheese");
            statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #fa5252; -fx-font-weight: 600;");
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
        } catch (Exception ignored) {}

        openCheeseButton.setDisable(false);
        stopCheeseButton.setDisable(true);
        statusLabel.setText("Cheese camera stopped");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #adb5bd; -fx-font-weight: 600;");
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
        statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ffd43b; -fx-font-weight: 600;");

        Thread processThread = new Thread(() -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    Image image = new Image(selectedFile.toURI().toString());
                    cameraFeed.setImage(image);
                    statusLabel.setText("Image selected successfully");
                    statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #51cf66; -fx-font-weight: 600;");

                    stopCheese();
                    app.showRunPanel(selectedFile.getAbsolutePath());

                } catch (Exception e) {
                    statusLabel.setText("Failed to display image: " + e.getMessage());
                    statusLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #fa5252; -fx-font-weight: 600;");
                }
            });
        });

        processThread.setDaemon(true);
        processThread.start();
    }
}
