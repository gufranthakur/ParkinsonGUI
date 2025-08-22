package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class RunPanel extends VBox {
    private App app;
    private ImageView imagePreview;
    private RadioButton spiralRadio;
    private RadioButton waveRadio;
    private ToggleGroup radioGroup;
    private Slider accuracySlider;
    private Label accuracyLabel;
    private Button executeButton;
    private ProgressBar loadingBar;
    private String currentImagePath;

    public RunPanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        imagePreview = new ImageView();
        imagePreview.setFitWidth(400);
        imagePreview.setFitHeight(300);
        imagePreview.setPreserveRatio(true);
        imagePreview.setSmooth(false);
        imagePreview.setCache(true);

        radioGroup = new ToggleGroup();
        spiralRadio = new RadioButton("Spiral");
        waveRadio = new RadioButton("Wave");
        spiralRadio.setToggleGroup(radioGroup);
        waveRadio.setToggleGroup(radioGroup);
        spiralRadio.setSelected(true);

        accuracySlider = new Slider(0, 100, 50);
        accuracySlider.setShowTickLabels(true);
        accuracySlider.setShowTickMarks(true);
        accuracyLabel = new Label("Accuracy Threshold: 50%");

        executeButton = new Button("Execute");
        executeButton.setPrefWidth(150);

        loadingBar = new ProgressBar();
        loadingBar.setVisible(false);
        loadingBar.setPrefWidth(400);
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(20);

        HBox radioBox = new HBox(20);
        radioBox.setAlignment(Pos.CENTER);
        radioBox.getChildren().addAll(spiralRadio, waveRadio);

        VBox sliderBox = new VBox(10);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.getChildren().addAll(accuracySlider, accuracyLabel);

        getChildren().addAll(
                imagePreview,
                radioBox,
                sliderBox,
                executeButton,
                loadingBar
        );

        loadLatestCapturedImage();
    }

    private void setupEventHandlers() {
        accuracySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                accuracyLabel.setText("Accuracy Threshold: " + Math.round(newVal.doubleValue()) + "%"));

        executeButton.setOnAction(e -> executePythonScript());
    }

    private void loadLatestCapturedImage() {
        Thread loadThread = new Thread(() -> {
            try {
                File directory = new File(App.IMAGE_STORAGE_PATH);
                if (directory.exists() && directory.isDirectory()) {
                    File latestFile = Files.list(Paths.get(App.IMAGE_STORAGE_PATH))
                            .map(Path::toFile)
                            .filter(File::isFile)
                            .filter(f -> f.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg)"))
                            .max(Comparator.comparingLong(File::lastModified))
                            .orElse(null);

                    if (latestFile != null) {
                        currentImagePath = latestFile.getAbsolutePath();

                        javafx.application.Platform.runLater(() -> {
                            try {
                                Image image = new Image(latestFile.toURI().toString(), 400, 300, true, false);
                                imagePreview.setImage(image);
                            } catch (Exception e) {
                                System.err.println("Error loading image: " + e.getMessage());
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("Error finding latest image: " + e.getMessage());
            }
        });

        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void executePythonScript() {
        loadingBar.setVisible(true);
        executeButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String testType = spiralRadio.isSelected() ? "spiral" : "wave";
                double accuracy = accuracySlider.getValue();

                ProcessBuilder pb = new ProcessBuilder("python3", "/scripts/test.py",
                        currentImagePath, testType, String.valueOf(accuracy));
                Process process = pb.start();
                process.waitFor();

                return null;
            }

            @Override
            protected void succeeded() {
                loadingBar.setVisible(false);
                executeButton.setDisable(false);
                app.showResultPanel();
            }

            @Override
            protected void failed() {
                loadingBar.setVisible(false);
                executeButton.setDisable(false);
            }
        };

        new Thread(task).start();
    }
}