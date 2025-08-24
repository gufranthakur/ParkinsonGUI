    package com.parkinsongui.panels;

    import com.parkinsongui.App;
    import javafx.application.Platform;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.StackPane;
    import javafx.scene.layout.VBox;

    public class HomePanel extends StackPane {
        private App app;
        private Button scanImageButton;
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
            // Load icons
            Image scanIcon = new Image(getClass().getResourceAsStream("/scan.png"));
            Image settingsIcon = new Image(getClass().getResourceAsStream("/settings.png"));
            Image aboutIcon = new Image(getClass().getResourceAsStream("/about.png"));
            Image powerIcon = new Image(getClass().getResourceAsStream("/power.png"));

            scanImageButton = new Button("Scan Image", new ImageView(scanIcon));
            settingsButton = new Button("Settings", new ImageView(settingsIcon));
            aboutButton = new Button("About", new ImageView(aboutIcon));
            powerOffButton = new Button("Power Off", new ImageView(powerIcon));

            resizeButtonIcon(scanImageButton);
            resizeButtonIcon(settingsButton);
            resizeButtonIcon(aboutButton);
            resizeButtonIcon(powerOffButton);

            // Modern button style with gradient border
            String baseButtonStyle = """
                -fx-pref-width: 240;
                -fx-pref-height: 50;
                -fx-font-size: 15px;
                -fx-font-weight: 600;
                -fx-background-radius: 14;
                -fx-border-radius: 14;
                -fx-background-color: #2a2d31;
                -fx-border-width: 2;
                -fx-border-color: linear-gradient(to right, #4dabf7, #9775fa);
                -fx-text-fill: #f1f3f5;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 4);
            """;

            scanImageButton.setStyle(baseButtonStyle);
            settingsButton.setStyle(baseButtonStyle);
            aboutButton.setStyle(baseButtonStyle);
            powerOffButton.setStyle(baseButtonStyle.replace("#4dabf7, #9775fa", "#ff6b6b, #f06595")
                    + "-fx-background-color: #3a2a2d;");

            // Hover effects
            scanImageButton.setOnMouseEntered(e -> scanImageButton.setStyle(baseButtonStyle +
                    "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
            scanImageButton.setOnMouseExited(e -> scanImageButton.setStyle(baseButtonStyle));

            settingsButton.setOnMouseEntered(e -> settingsButton.setStyle(baseButtonStyle +
                    "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
            settingsButton.setOnMouseExited(e -> settingsButton.setStyle(baseButtonStyle));

            aboutButton.setOnMouseEntered(e -> aboutButton.setStyle(baseButtonStyle +
                    "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
            aboutButton.setOnMouseExited(e -> aboutButton.setStyle(baseButtonStyle));

            powerOffButton.setOnMouseEntered(e -> powerOffButton.setStyle(baseButtonStyle.replace("#4dabf7, #9775fa", "#ff6b6b, #f06595") +
                    "-fx-background-color: linear-gradient(to right, #4a2a2d, #3a1e22);"));
            powerOffButton.setOnMouseExited(e -> powerOffButton.setStyle(baseButtonStyle.replace("#4dabf7, #9775fa", "#ff6b6b, #f06595")
                    + "-fx-background-color: #3a2a2d;"));
        }

        private void setupLayout() {
            VBox contentBox = new VBox(25);
            contentBox.setAlignment(Pos.CENTER);

            // Header
            Image brainIcon = new Image(getClass().getResourceAsStream("/brain.png"));
            ImageView brainView = new ImageView(brainIcon);
            brainView.setFitWidth(50);
            brainView.setFitHeight(50);

            Label titleLabel = new Label("Parkinson's Detection System");
            titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #f8f9fa;");

            HBox titleBox = new HBox(12, brainView, titleLabel);
            titleBox.setAlignment(Pos.CENTER);

            Label subtitleLabel = new Label("Medical Analysis Tool");
            subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #adb5bd;");

            VBox headerBox = new VBox(6, titleBox, subtitleLabel);
            headerBox.setAlignment(Pos.CENTER);

            VBox buttonBox = new VBox(18, scanImageButton, settingsButton, aboutButton, powerOffButton);
            buttonBox.setAlignment(Pos.CENTER);

            // Card container (refined)
            VBox card = new VBox(30, headerBox, buttonBox);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(40));
            card.setStyle("""
                -fx-background-color: rgba(20, 20, 20, 1);
                -fx-background-radius: 22;
                -fx-border-radius: 22;
                -fx-border-width: 2;
                -fx-border-color: linear-gradient(to right, #495057, #343a40);
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 30, 0.3, 0, 10);
            """);

            contentBox.getChildren().add(card);

            // Root container padding (as requested)
            setPadding(new Insets(20, 45, 20, 45));
            getChildren().add(contentBox);
        }

        private void setupEventHandlers() {
            scanImageButton.setOnAction(e -> app.showScanImagePanel());
            settingsButton.setOnAction(e -> {});
            aboutButton.setOnAction(e -> {});
            powerOffButton.setOnAction(e -> Platform.exit());
        }

        private void resizeButtonIcon(Button button) {
            ImageView iv = (ImageView) button.getGraphic();
            if (iv != null) {
                iv.setFitWidth(20);
                iv.setFitHeight(20);
            }
        }
    }
