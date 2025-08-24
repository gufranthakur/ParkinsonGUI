package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.collections.FXCollections;

public class ResultPanel extends VBox {
    private App app;
    private int parkinsonProbability;
    private int modelAccuracy;
    private int inferenceTime;
    private String fullOutput;

    public ResultPanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
    }

    public void updateResults(int parkinsonProbability, int modelAccuracy, int inferenceTime, String fullOutput) {
        this.parkinsonProbability = parkinsonProbability;
        this.modelAccuracy = modelAccuracy;
        this.inferenceTime = inferenceTime;
        this.fullOutput = fullOutput;
        refreshDisplay();
    }

    private void initializeComponents() {
        setAlignment(Pos.TOP_CENTER);
        setSpacing(20);
        setPadding(new Insets(20));
        // Removed background styling
    }

    private void setupLayout() {
        // Header
        Label headerLabel = new Label("Parkinson's Disease Analysis Results");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        getChildren().add(headerLabel);
    }

    private void refreshDisplay() {
        if (getChildren().size() > 1) {
            getChildren().subList(1, getChildren().size()).clear();
        }

        VBox dashboardBox = createDashboard();
        TextArea detailsArea = createDetailsArea();

        getChildren().addAll(dashboardBox, detailsArea);
    }

    private VBox createDashboard() {
        VBox dashboardBox = new VBox(30);
        dashboardBox.setAlignment(Pos.CENTER);
        dashboardBox.setPadding(new Insets(20));

        // Parkinson's Risk Card with gradient
        VBox riskCard = createMetricCardWithGradient("Parkinson's Risk", parkinsonProbability + "%",
                parkinsonProbability > 60 ? "linear-gradient(to bottom, #ff7675, #e74c3c)" :
                        parkinsonProbability > 40 ? "linear-gradient(to bottom, #fdcb6e, #f39c12)" :
                                "linear-gradient(to bottom, #55efc4, #27ae60)",
                parkinsonProbability > 60 ? "#e74c3c" :
                        parkinsonProbability > 40 ? "#f39c12" : "#27ae60");

        // Model Accuracy Card with gradient
        VBox accuracyCard = createMetricCardWithGradient("Model Accuracy", modelAccuracy + "%",
                "linear-gradient(to bottom, #74b9ff, #3498db)", "#3498db");

        // Inference Time Card with gradient
        VBox timeCard = createMetricCardWithGradient("Processing Time", inferenceTime + " ms",
                "linear-gradient(to bottom, #fd79a8, #9b59b6)", "#9b59b6");

        // Risk Level Indicator
        VBox riskIndicator = createRiskIndicator();

        // Force same size for all cards
        double cardWidth = 180;
        double cardHeight = 150;
        riskCard.setPrefSize(cardWidth, cardHeight);
        accuracyCard.setPrefSize(cardWidth, cardHeight);
        timeCard.setPrefSize(cardWidth, cardHeight);
        riskIndicator.setPrefSize(cardWidth, cardHeight);

        // Place cards in a row
        HBox cardsRow = new HBox(20);
        cardsRow.setAlignment(Pos.CENTER);
        cardsRow.getChildren().addAll(riskCard, accuracyCard);

        HBox cardsRow2 = new HBox(20);
        cardsRow2.setAlignment(Pos.CENTER);
        cardsRow2.getChildren().addAll(timeCard, riskIndicator);

        // Charts (only Model Performance now)
        VBox chartsBox = createCharts();

        dashboardBox.getChildren().addAll(cardsRow, cardsRow2, chartsBox);
        return dashboardBox;
    }


    private VBox createMetricCardWithGradient(String title, String value, String gradientBackground, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + gradientBackground + "; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Progress bar visualization with white colors for better visibility on gradient
        Rectangle progressBg = new Rectangle(80, 8);
        progressBg.setFill(Color.web("#ffffff", 0.3));
        progressBg.setArcWidth(4);
        progressBg.setArcHeight(4);

        Rectangle progressFill = new Rectangle(80, 8);
        progressFill.setFill(Color.web("#ffffff", 0.8));
        progressFill.setArcWidth(4);
        progressFill.setArcHeight(4);

        if (title.contains("Risk")) {
            progressFill.setWidth(80 * (parkinsonProbability / 100.0));
        } else if (title.contains("Accuracy")) {
            progressFill.setWidth(80 * (modelAccuracy / 100.0));
        } else {
            progressFill.setWidth(80 * Math.min(inferenceTime / 1000.0, 1.0));
        }

        VBox progressBox = new VBox(-8);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.getChildren().addAll(progressBg, progressFill);

        card.getChildren().addAll(titleLabel, valueLabel, progressBox);
        return card;
    }

    private VBox createRiskIndicator() {
        VBox indicator = new VBox(10);
        indicator.setAlignment(Pos.CENTER);
        indicator.setPadding(new Insets(20));
        indicator.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label titleLabel = new Label("Risk Level");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        String riskLevel;
        String riskColor;
        if (parkinsonProbability > 60) {
            riskLevel = "HIGH";
            riskColor = "#e74c3c";
        } else if (parkinsonProbability > 40) {
            riskLevel = "MODERATE";
            riskColor = "#f39c12";
        } else {
            riskLevel = "LOW";
            riskColor = "#27ae60";
        }

        Label riskLabel = new Label(riskLevel);
        riskLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + riskColor + ";");

        Rectangle statusBar = new Rectangle(60, 30);
        statusBar.setFill(Color.web(riskColor));
        statusBar.setArcWidth(15);
        statusBar.setArcHeight(15);

        indicator.getChildren().addAll(titleLabel, riskLabel, statusBar);
        return indicator;
    }

    private VBox createCharts() {
        VBox chartsBox = new VBox(15);
        chartsBox.setAlignment(Pos.CENTER);

        // Only Model Accuracy Pie Chart (Risk distribution removed)
        PieChart accuracyChart = new PieChart();
        accuracyChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Accuracy", modelAccuracy),
                new PieChart.Data("Error", 100 - modelAccuracy)
        ));
        accuracyChart.setTitle("Model Performance");
        accuracyChart.setPrefSize(250, 250);
        accuracyChart.setLegendVisible(true);

        chartsBox.getChildren().add(accuracyChart);
        return chartsBox;
    }

    private TextArea createDetailsArea() {
        TextArea detailsArea = new TextArea();
        detailsArea.setText(fullOutput != null ? fullOutput : "Detailed analysis will appear here...");
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(15);
        detailsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px; " +
                "-fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        VBox detailsContainer = new VBox(10);
        Label detailsTitle = new Label("Detailed Analysis");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        detailsContainer.getChildren().addAll(detailsTitle, detailsArea);

        return detailsArea;
    }
}