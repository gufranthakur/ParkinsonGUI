package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class UploadImagePanel extends VBox {
    private App app;
    private ImageView imagePreview;
    private Button selectImageButton;
    private Button goBackButton;
    private File selectedFile;

    public UploadImagePanel(App app) {
        this.app = app;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        imagePreview = new ImageView();
        imagePreview.setFitWidth(600);
        imagePreview.setFitHeight(400);
        imagePreview.setPreserveRatio(true);

        selectImageButton = new Button("Select Image");
        goBackButton = new Button("Go Back");

        selectImageButton.setPrefWidth(150);
        goBackButton.setPrefWidth(150);
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        getChildren().addAll(imagePreview, selectImageButton, goBackButton);
    }

    private void setupEventHandlers() {
        selectImageButton.setOnAction(e -> selectImage());
        goBackButton.setOnAction(e -> app.showHomePanel());
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        selectedFile = fileChooser.showOpenDialog(app.getPrimaryStage());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            imagePreview.setImage(image);
            app.showRunPanel();
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }
}