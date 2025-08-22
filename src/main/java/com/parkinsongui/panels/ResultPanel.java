package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ResultPanel extends VBox {
    private App app;
    private Label resultLabel;

    public ResultPanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        resultLabel = new Label("Results will be displayed here");
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        getChildren().add(resultLabel);
    }
}