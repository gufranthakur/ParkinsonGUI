package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;

public class ResultPanel extends VBox {
    private App app;
    private Label stateLabel;
    private VBox predictionCard;

    private Arc confidenceArc;
    private Arc probabilityArc;
    private Label confidenceValueLabel;
    private Label probabilityValueLabel;

    private Label inferenceLabel;
    private Label modelSizeLabel;
    private Label layersLabel;
    private Label parametersLabel;

    public ResultPanel(App app) {
        this.app = app;
        setupLayout();
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(30));

        VBox card = new VBox(30);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle("""
           -fx-background-color: #1e1e1e;
           -fx-background-radius: 20;
           -fx-border-radius: 20;
           -fx-border-width: 2;
           -fx-border-color: linear-gradient(to right, #495057, #343a40);
           -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 25, 0.3, 0, 8);
       """);

        Label resultsLabel = new Label("📊 Diagnostic Dashboard");
        resultsLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #f8f9fa;");

        // Prediction card
        predictionCard = new VBox();
        predictionCard.setAlignment(Pos.CENTER);
        predictionCard.setPadding(new Insets(20));
        predictionCard.setPrefWidth(350);

        stateLabel = new Label("--");
        stateLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: white;");
        predictionCard.getChildren().add(stateLabel);

        // Progress rings
        VBox confidenceContainer = createProgressRing("Confidence", "--", 0);
        VBox probabilityContainer = createProgressRing("Raw Probability", "--", 0);

        HBox chartsBox = new HBox(40, confidenceContainer, probabilityContainer);
        chartsBox.setAlignment(Pos.CENTER);

        // Stats cards with gradient borders
        HBox statsBox = new HBox(20,
                createStatCard("⚡ Inference Time", "-- ms", "#4facfe"),
                createStatCard("💾 Model Size", "-- MB", "#a18cd1"),
                createStatCard("🧩 Layers", "--", "#ff9a9e"),
                createStatCard("🔢 Parameters", "--", "#ff6a88")
        );
        statsBox.setAlignment(Pos.CENTER);

        // Back button
        Button backButton = new Button("⬅ Back to Home");
        backButton.setStyle("""
           -fx-background-color: linear-gradient(to right, #495057, #343a40);
           -fx-text-fill: white;
           -fx-font-size: 14px;
           -fx-font-weight: 600;
           -fx-background-radius: 10;
           -fx-padding: 8 20 8 20;
       """);
        backButton.setOnAction(e -> app.showHomePanel());

        card.getChildren().addAll(resultsLabel, predictionCard, chartsBox, statsBox, backButton);
        getChildren().add(card);
    }

    private VBox createProgressRing(String label, String value, double percent) {
        Arc arc = new Arc(0, 0, 70, 70, 90, 0);
        arc.setType(ArcType.OPEN);
        arc.setStrokeWidth(10);
        arc.setFill(null);
        arc.setStroke(Color.web("#ff6b6b"));
        arc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Remove or comment out the infinite animation
        // RotateTransition rt = new RotateTransition(Duration.seconds(2), arc);
        // rt.setByAngle(360);
        // rt.setCycleCount(RotateTransition.INDEFINITE);
        // rt.setInterpolator(Interpolator.LINEAR);
        // rt.play();

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ced4da;");

        if (label.contains("Confidence")) {
            confidenceValueLabel = valueLabel;
        } else if (label.contains("Raw Probability")) {
            probabilityValueLabel = valueLabel;
        }

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #adb5bd;");

        VBox box = new VBox(6, arc, titleLabel, valueLabel);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private VBox createStatCard(String title, String value, String borderColor) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #adb5bd;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #f8f9fa;");

        VBox box = new VBox(6, titleLabel, valueLabel);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(150);
        box.setPadding(new Insets(15));
        box.setStyle(
                "-fx-background-color: #2a2d31;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-color: " + borderColor + ";"
        );

        if (title.contains("Inference")) inferenceLabel = valueLabel;
        else if (title.contains("Model Size")) modelSizeLabel = valueLabel;
        else if (title.contains("Layers")) layersLabel = valueLabel;
        else if (title.contains("Parameters")) parametersLabel = valueLabel;

        return box;
    }

    public void displayResults(String state, float confidence, double rawProbability,
                               float inferenceTime, float modelSize, int modelLayers, int totalParameters) {
        // Prediction box styling
        if (state.equalsIgnoreCase("Healthy")) {
            predictionCard.setStyle("""
               -fx-background-color: linear-gradient(to right, #38b000, #70e000);
               -fx-background-radius: 16;
               -fx-border-radius: 16;
               -fx-border-width: 2;
               -fx-border-color: #2d6a4f;
           """);
        } else {
            predictionCard.setStyle("""
               -fx-background-color: linear-gradient(to right, #d00000, #ff4d6d);
               -fx-background-radius: 16;
               -fx-border-radius: 16;
               -fx-border-width: 2;
               -fx-border-color: #9d0208;
           """);
        }
        stateLabel.setText(state);

        confidenceValueLabel.setText(String.format("%.2f%%", confidence));
        probabilityValueLabel.setText(String.format("%.4f", rawProbability));
        inferenceLabel.setText(String.format("%.2f ms", inferenceTime));
        modelSizeLabel.setText(String.format("%.2f MB", modelSize));
        layersLabel.setText(String.valueOf(modelLayers));
        parametersLabel.setText(String.valueOf(totalParameters));
    }
}