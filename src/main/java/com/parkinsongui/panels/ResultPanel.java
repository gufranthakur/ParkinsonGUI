package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ResultPanel extends VBox {
    private App app;
    private String state;
    private float confidence;
    private double rawProbability;
    private float inferenceTime;
    private float modelSize;
    private int modelLayers;
    private int totalParameters;

    private Label stateLabel;
    private PieChart confidenceChart;
    private PieChart probabilityChart;
    private Label confidenceValueLabel;
    private Label probabilityValueLabel;

    public ResultPanel(App app) {
        this.app = app;
        setupLayout();
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(30));

        // Outer card
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
        VBox predictionCard = new VBox();
        predictionCard.setAlignment(Pos.CENTER);
        predictionCard.setPadding(new Insets(20));
        predictionCard.setPrefWidth(300);
        predictionCard.setStyle("""
            -fx-background-color: #2a2d31;
            -fx-background-radius: 16;
            -fx-border-radius: 16;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, #868e96, #495057);
        """);

        stateLabel = new Label("--");
        stateLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #dee2e6;");
        predictionCard.getChildren().add(stateLabel);

        // Charts
        confidenceChart = new PieChart();
        confidenceChart.setLegendVisible(false);
        confidenceChart.setLabelsVisible(false);
        confidenceChart.setPrefSize(160, 160);
        confidenceValueLabel = new Label("Confidence: --");
        confidenceValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ced4da;");

        VBox confidenceContainer = new VBox(8, confidenceChart, confidenceValueLabel);
        confidenceContainer.setAlignment(Pos.CENTER);

        probabilityChart = new PieChart();
        probabilityChart.setLegendVisible(false);
        probabilityChart.setLabelsVisible(false);
        probabilityChart.setPrefSize(160, 160);
        probabilityValueLabel = new Label("Raw Probability: --");
        probabilityValueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ced4da;");

        VBox probabilityContainer = new VBox(8, probabilityChart, probabilityValueLabel);
        probabilityContainer.setAlignment(Pos.CENTER);

        HBox chartsBox = new HBox(40, confidenceContainer, probabilityContainer);
        chartsBox.setAlignment(Pos.CENTER);

        // Stats cards
        HBox statsBox = new HBox(20,
                createStatCard("⚡ Inference Time", "-- ms"),
                createStatCard("💾 Model Size", "-- MB"),
                createStatCard("🧩 Layers", "--"),
                createStatCard("🔢 Parameters", "--")
        );
        statsBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(resultsLabel, predictionCard, chartsBox, statsBox);
        getChildren().add(card);
    }

    private VBox createStatCard(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #adb5bd;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #f8f9fa;");

        VBox box = new VBox(6, titleLabel, valueLabel);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(150);
        box.setPadding(new Insets(15));
        box.setStyle("""
            -fx-background-color: #2a2d31;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-width: 1.5;
            -fx-border-color: #495057;
        """);
        return box;
    }

    public void displayResults(String state, float confidence, double rawProbability,
                               float inferenceTime, float modelSize, int modelLayers, int totalParameters) {
        this.state = state;
        this.confidence = confidence;
        this.rawProbability = rawProbability;
        this.inferenceTime = inferenceTime;
        this.modelSize = modelSize;
        this.modelLayers = modelLayers;
        this.totalParameters = totalParameters;

        stateLabel.setText(state);
        if (state.equalsIgnoreCase("Healthy")) {
            stateLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #51cf66;");
        } else {
            stateLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #ff6b6b;");
        }

        updateConfidenceChart();
        updateProbabilityChart();

        confidenceValueLabel.setText("Confidence: " + confidence + "%");
        probabilityValueLabel.setText("Raw Probability: " + rawProbability);

        // Update stat cards dynamically
        ((Label)((VBox)((HBox)((VBox)getChildren().get(0)).getChildren().get(3)).getChildren().get(0)).getChildren().get(1))
                .setText(String.format("%.2f ms", inferenceTime));
        ((Label)((VBox)((HBox)((VBox)getChildren().get(0)).getChildren().get(3)).getChildren().get(1)).getChildren().get(1))
                .setText(String.format("%.2f MB", modelSize));
        ((Label)((VBox)((HBox)((VBox)getChildren().get(0)).getChildren().get(3)).getChildren().get(2)).getChildren().get(1))
                .setText(String.valueOf(modelLayers));
        ((Label)((VBox)((HBox)((VBox)getChildren().get(0)).getChildren().get(3)).getChildren().get(3)).getChildren().get(1))
                .setText(String.valueOf(totalParameters));
    }

    private void updateConfidenceChart() {
        ObservableList<PieChart.Data> confidenceData = FXCollections.observableArrayList(
                new PieChart.Data("Confidence", confidence),
                new PieChart.Data("Remaining", 100 - confidence)
        );
        confidenceChart.setData(confidenceData);
    }

    private void updateProbabilityChart() {
        double probabilityPercent = rawProbability * 100;
        ObservableList<PieChart.Data> probabilityData = FXCollections.observableArrayList(
                new PieChart.Data("Raw Probability", probabilityPercent),
                new PieChart.Data("Remaining", 100 - probabilityPercent)
        );
        probabilityChart.setData(probabilityData);
    }
}
