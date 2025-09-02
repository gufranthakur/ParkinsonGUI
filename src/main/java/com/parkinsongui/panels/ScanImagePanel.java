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

public class ScanImagePanel extends ScrollPane {
    private App app;
    private Button scanFromPhoneButton;
    private Button selectImageButton;
    private Button goBackButton;
    private String picturesDirectory;

    public ScanImagePanel(App app) {
        this.app = app;
        this.picturesDirectory = System.getProperty("user.home") + "/Pictures";
        setupLayout();
        setupEventHandlers();
    }

    private void setupLayout() {
        VBox mainContent = new VBox();
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setSpacing(30);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #1e1e1e;");

        // Header section
        VBox headerBox = createHeaderSection();

        // Cards container
        HBox cardsContainer = createCardsSection();

        // Back button section
        VBox backButtonSection = createBackButtonSection();

        mainContent.getChildren().addAll(headerBox, cardsContainer, backButtonSection);

        // ScrollPane config
        setContent(mainContent);
        setFitToWidth(true);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setStyle("-fx-background-color: #1e1e1e;");
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Select Capture Method");
        titleLabel.setStyle("""
            -fx-font-size: 28px; 
            -fx-font-weight: 700; 
            -fx-text-fill: #f8f9fa;
            """);

        Label subtitleLabel = new Label("Choose how you'd like to capture or select your image");
        subtitleLabel.setStyle("""
            -fx-font-size: 15px; 
            -fx-text-fill: #adb5bd;
            """);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        return headerBox;
    }

    private HBox createCardsSection() {
        HBox cardsContainer = new HBox(40);
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.setPadding(new Insets(20, 0, 20, 0));

        // Card 1: Scan from Phone
        VBox phoneCard = createCard(
                "Scan from Phone",
                "/qr.png", // You'll need to add this icon
                "Use your mobile phone camera to capture images remotely. Generate a QR code and scan it with your phone to take photos wirelessly.",
                "Start Phone Scan",
                "#9775fa", "#7950f2",
                e -> scanFromPhone()
        );

        // Card 2: Upload Image
        VBox uploadCard = createCard(
                "Upload Image",
                "/upload.png", // You'll need to add this icon
                "Select an existing image file from your computer. Browse through your local files and choose a previously captured image for analysis.",
                "Browse Files",
                "#51cf66", "#2f9e44",
                e -> selectImage()
        );

        cardsContainer.getChildren().addAll(phoneCard, uploadCard);
        return cardsContainer;
    }

    private VBox createCard(String title, String iconPath, String description,
                            String buttonText, String gradientStart, String gradientEnd,
                            javafx.event.EventHandler<javafx.event.ActionEvent> buttonAction) {

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(320);
        card.setPrefHeight(400);
        card.setPadding(new Insets(30));
        card.setStyle(String.format("""
            -fx-background-color: rgba(42, 45, 49, 0.9);
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, %s, %s);
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 8);
            """, gradientStart, gradientEnd));

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace("rgba(42, 45, 49, 0.9)", "rgba(52, 55, 59, 0.95)"));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("rgba(52, 55, 59, 0.95)", "rgba(42, 45, 49, 0.9)"));
        });

        // Card title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: 700;
            -fx-text-fill: #f8f9fa;
            -fx-text-alignment: center;
            """);

        // Icon
        ImageView iconView = new ImageView();
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            iconView.setImage(icon);
        } catch (Exception ex) {
            // Fallback to a colored rectangle if icon not found
            iconView = createFallbackIcon(gradientStart, gradientEnd);
        }
        iconView.setFitWidth(100);
        iconView.setFitHeight(100);
        iconView.setPreserveRatio(true);
        iconView.setSmooth(true);
        iconView.setStyle("""
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);
            """);

        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #adb5bd;
            -fx-text-alignment: center;
            -fx-wrap-text: true;
            """);
        descLabel.setMaxWidth(260);

        // Button
        Button cardButton = new Button(buttonText);
        cardButton.setOnAction(buttonAction);

        String buttonStyle = String.format("""
            -fx-pref-width: 200;
            -fx-pref-height: 40;
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-background-color: #2a2d31;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, %s, %s);
            -fx-text-fill: #f1f3f5;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 3);
            """, gradientStart, gradientEnd);

        cardButton.setStyle(buttonStyle);

        // Button hover effects
        cardButton.setOnMouseEntered(e ->
                cardButton.setStyle(buttonStyle + "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
        cardButton.setOnMouseExited(e ->
                cardButton.setStyle(buttonStyle));

        card.getChildren().addAll(titleLabel, iconView, descLabel, cardButton);
        return card;
    }

    private ImageView createFallbackIcon(String gradientStart, String gradientEnd) {
        // Create a simple colored rectangle as fallback
        VBox fallbackBox = new VBox();
        fallbackBox.setPrefSize(100, 100);
        fallbackBox.setStyle(String.format("""
            -fx-background-color: linear-gradient(to bottom right, %s, %s);
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            """, gradientStart, gradientEnd));

        // This is a simple fallback - ideally you'd want proper icons
        ImageView fallbackView = new ImageView();
        fallbackView.setFitWidth(100);
        fallbackView.setFitHeight(100);
        return fallbackView;
    }

    private VBox createBackButtonSection() {
        VBox backSection = new VBox();
        backSection.setAlignment(Pos.CENTER);
        backSection.setPadding(new Insets(20, 0, 0, 0));

        goBackButton = new Button("← Back to Home");

        String backButtonStyle = """
            -fx-pref-width: 200;
            -fx-pref-height: 40;
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-background-color: #2a2d31;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, #868e96, #495057);
            -fx-text-fill: #f1f3f5;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 3);
            """;

        goBackButton.setStyle(backButtonStyle);

        // Hover effect for back button
        goBackButton.setOnMouseEntered(e ->
                goBackButton.setStyle(backButtonStyle + "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
        goBackButton.setOnMouseExited(e ->
                goBackButton.setStyle(backButtonStyle));

        backSection.getChildren().add(goBackButton);
        return backSection;
    }

    private void setupEventHandlers() {
        goBackButton.setOnAction(e -> app.showHomePanel());
    }

    private void scanFromPhone() {
        app.showScanFromPhonePanel();
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.setInitialDirectory(new File(picturesDirectory));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(app.getPrimaryStage());
        if (selectedFile != null) {
            processSelectedImage(selectedFile);
        }
    }

    private void processSelectedImage(File selectedFile) {
        Thread processThread = new Thread(() -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    // Directly navigate to run panel with selected image
                    app.showRunPanel(selectedFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to process image: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        processThread.setDaemon(true);
        processThread.start();
    }
}