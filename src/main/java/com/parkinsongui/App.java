package com.parkinsongui;

import atlantafx.base.theme.CupertinoDark;
import com.parkinsongui.panels.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    private Stage primaryStage;
    private BorderPane rootPane;
    private HomePanel homePanel;
    private ScanImagePanel scanImagePanel;
    private ScanFromPhonePanel phonePanel;
    private RunPanel runPanel;
    private ResultPanel resultPanel;

    public static final String IMAGE_STORAGE_PATH = "captured_images/";
    private Image capturedImage;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        this.primaryStage = stage;
        this.rootPane = new BorderPane();

        initializePanels();
        showHomePanel();

        Scene scene = new Scene(rootPane, 840, 720);
        stage.setTitle("Parkinson Disease Detection");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> cleanup());
        stage.show();
    }

    private void initializePanels() {
        homePanel = new HomePanel(this);
        scanImagePanel = new ScanImagePanel(this);
        phonePanel = new ScanFromPhonePanel(this);
        runPanel = new RunPanel(this);
        resultPanel = new ResultPanel(this);
    }

    public void showHomePanel() {
        rootPane.setCenter(homePanel);
    }

    public void showScanImagePanel() {
        rootPane.setCenter(scanImagePanel);
       // scanImagePanel.startCamera();
    }

    public void showScanFromPhonePanel() {
        rootPane.setCenter(phonePanel);
    }

    public void showRunPanel(String imagePath) {
        if (runPanel == null) {
            runPanel = new RunPanel(this);
        }
        runPanel.setSelectedImage(imagePath);

        primaryStage.getScene().setRoot(runPanel);
        primaryStage.setTitle("Run Analysis - Parkinson's Detection");
    }

    // In your App.java, update the showResultPanel method:
    public void showResultPanel(int parkinsonProbability, int modelAccuracy, int inferenceTime, String fullOutput) {
        if (resultPanel == null) {
            resultPanel = new ResultPanel(this);
        }
        resultPanel.updateResults(parkinsonProbability, modelAccuracy, inferenceTime, fullOutput);

        primaryStage.getScene().setRoot(resultPanel);
        primaryStage.setTitle("Analysis Results - Parkinson's Detection");
    }

    public Image getCapturedImage() {
        return capturedImage;
    }

    public void setCapturedImage(Image image) {
        this.capturedImage = image;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void cleanup() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}