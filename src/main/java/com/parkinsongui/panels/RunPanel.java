package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RunPanel extends VBox {
    private App app;
    private ImageView imagePreview;
    private RadioButton spiralRadio;
    private RadioButton waveRadio;
    private ToggleGroup radioGroup;
    private Button executeButton;
    private Button goBackButton;
    private ProgressBar loadingBar;
    private String selectedImagePath;

    private int parkinsonProbability;
    private int modelAccuracy;
    private int inferenceTime;
    private String fullOutput;

    public RunPanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    public void setSelectedImage(String imagePath) {
        this.selectedImagePath = imagePath;
        System.out.println("RunPanel received image path: " + imagePath); // Debug line
        loadSelectedImage();
    }

    private void loadSelectedImage() {
        if (selectedImagePath != null) {
            try {
                File imageFile = new File(selectedImagePath);
                System.out.println("Loading image - File exists: " + imageFile.exists() + ", Size: " + imageFile.length()); // Debug
                Image image = new Image(imageFile.toURI().toString(), 400, 300, true, false);
                imagePreview.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading selected image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("selectedImagePath is null"); // Debug
        }
    }

    private void initializeComponents() {
        imagePreview = new ImageView();
        imagePreview.setFitWidth(400);
        imagePreview.setFitHeight(300);
        imagePreview.setPreserveRatio(true);
        imagePreview.setSmooth(false);
        imagePreview.setCache(true);
        imagePreview.setStyle("-fx-border-color: linear-gradient(to right, #495057, #868e96);" +
                "-fx-border-width: 2; -fx-border-radius: 10; " +
                "-fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 8, 0, 0, 3);");

        radioGroup = new ToggleGroup();
        spiralRadio = new RadioButton("Spiral Test");
        spiralRadio.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e9ecef;");
        waveRadio = new RadioButton("Wave Test");
        waveRadio.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e9ecef;");
        spiralRadio.setToggleGroup(radioGroup);
        waveRadio.setToggleGroup(radioGroup);
        spiralRadio.setSelected(true);

        String buttonStyle = "-fx-pref-height: 42; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);";

        executeButton = new Button("Execute Analysis");
        executeButton.setPrefWidth(200);
        executeButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #28a745, #218838); -fx-text-fill: white;");

        goBackButton = new Button("Go Back");
        goBackButton.setPrefWidth(150);
        goBackButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #6c757d, #495057); -fx-text-fill: white;");

        loadingBar = new ProgressBar();
        loadingBar.setVisible(false);
        loadingBar.setPrefWidth(400);

        // Hover effects
        executeButton.setOnMouseEntered(e -> executeButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #34ce57, #28a745); -fx-text-fill: white;"));
        executeButton.setOnMouseExited(e -> executeButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #28a745, #218838); -fx-text-fill: white;"));

        goBackButton.setOnMouseEntered(e -> goBackButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #868e96, #596166); -fx-text-fill: white;"));
        goBackButton.setOnMouseExited(e -> goBackButton.setStyle(buttonStyle +
                "-fx-background-color: linear-gradient(to right, #6c757d, #495057); -fx-text-fill: white;"));
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(30);

        // Card container (same style as ScanImagePanel)
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new javafx.geometry.Insets(30));
        card.setStyle(
                "-fx-background-color: rgba(25, 25, 25, 1);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: linear-gradient(to right, #6a11cb, #2575fc);" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0, 0, 5);"
        );

        // Title
        Label titleLabel = new Label("Analysis Configuration");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Radio buttons container
        VBox radioContainer = new VBox(15);
        radioContainer.setAlignment(Pos.CENTER);
        radioContainer.setPadding(new javafx.geometry.Insets(20));
        radioContainer.setStyle(
                "-fx-background-color: rgba(60, 60, 60, 0.8);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: linear-gradient(to right, #43cea2, #185a9d);" +
                        "-fx-border-width: 1;"
        );

        Label testTypeLabel = new Label("Select Test Type");
        testTypeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox radioBox = new HBox(30);
        radioBox.setAlignment(Pos.CENTER);
        radioBox.getChildren().addAll(spiralRadio, waveRadio);

        radioContainer.getChildren().addAll(testTypeLabel, radioBox);

        // Button box
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(goBackButton, executeButton);

        // Add to card
        card.getChildren().addAll(
                titleLabel,
                imagePreview,
                radioContainer,
                buttonBox,
                loadingBar
        );

        // Root container padding
        setPadding(new javafx.geometry.Insets(20, 45, 20, 45));
        getChildren().add(card);
    }


    private void setupEventHandlers() {
        executeButton.setOnAction(e -> executePythonScript());
        goBackButton.setOnAction(e -> app.showScanImagePanel());
    }

    private void executePythonScript() {
        if (selectedImagePath == null) {
            System.err.println("No image selected");
            return;
        }

        loadingBar.setVisible(true);
        executeButton.setDisable(true);
        goBackButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String testType = spiralRadio.isSelected() ? "s" : "w";

                ProcessBuilder pb = new ProcessBuilder(
                        "venv/bin/python",
                        "detection.py",
                        selectedImagePath,
                        testType
                );

                pb.directory(new File("python"));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder outputBuilder = new StringBuilder();
                String[] firstThreeLines = new String[3];
                int lineCount = 0;

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (lineCount < 3) {
                            firstThreeLines[lineCount] = line;
                            lineCount++;
                        } else {
                            outputBuilder.append(line).append("\n");
                        }
                        System.out.println(line);
                    }
                }

                if (lineCount >= 3) {
                    parkinsonProbability = Integer.parseInt(firstThreeLines[0]);
                    modelAccuracy = Integer.parseInt(firstThreeLines[1]);
                    inferenceTime = Integer.parseInt(firstThreeLines[2]);
                    fullOutput = outputBuilder.toString();
                }

                process.waitFor();
                return null;
            }

            @Override
            protected void succeeded() {
                loadingBar.setVisible(false);
                executeButton.setDisable(false);
                goBackButton.setDisable(false);
                app.showResultPanel(parkinsonProbability, modelAccuracy, inferenceTime, fullOutput);
            }

            @Override
            protected void failed() {
                loadingBar.setVisible(false);
                executeButton.setDisable(false);
                goBackButton.setDisable(false);
            }
        };

        new Thread(task).start();
    }
}
