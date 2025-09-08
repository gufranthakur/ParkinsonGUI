package com.parkinsongui.panels;

import com.parkinsongui.App;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

public class ScanFromPhonePanel extends HBox {

    private static final int PORT = 8080;
    private static final int QR_SIZE = 250;

    private App app;
    private HttpServer server;
    private String serverUrl;
    private volatile byte[] latestImage;
    private Button goToRunPanelButton;

    public ScanFromPhonePanel(App app) {
        this.app = app;
        try {
            startServer();
            setupLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        String localIP = getLocalIP();
        server = HttpServer.create(new InetSocketAddress(localIP, PORT), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equals(method)) {
                    exchange.sendResponseHeaders(200, -1);
                    return;
                }

                if ("GET".equals(method)) {
                    handleGetRequest(exchange);
                } else if ("POST".equals(method)) {
                    handlePostRequest(exchange);
                }
            }
        });
        server.start();

        serverUrl = "http://" + localIP + ":" + PORT;
        System.out.println("Server started: " + serverUrl);
    }

    private String getLocalIP() throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isLoopback() || !ni.isUp()) continue;

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        String response = getWebPage();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes("UTF-8"));
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

        if (contentType != null && contentType.contains("multipart/form-data")) {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            latestImage = extractImageFromMultipart(requestBody);

            Platform.runLater(() -> {
                goToRunPanelButton.setDisable(false);
            });
        }

        String response = "{\"status\":\"success\",\"message\":\"Image uploaded successfully\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private byte[] extractImageFromMultipart(byte[] data) {
        String dataStr = new String(data);
        int imageStart = dataStr.indexOf("\r\n\r\n") + 4;
        int imageEnd = dataStr.lastIndexOf("\r\n--");

        if (imageStart > 3 && imageEnd > imageStart) {
            return Arrays.copyOfRange(data, imageStart, imageEnd);
        }
        return data;
    }

    private void setupLayout() {
        setStyle("-fx-background-color: #1e1e1e;");
        setPadding(new Insets(30));

        VBox leftPanel = createStepsPanel();
        VBox rightPanel = createQRPanel();

        this.getChildren().addAll(leftPanel, rightPanel);
        this.setAlignment(Pos.CENTER);
    }

    private VBox createStepsPanel() {
        VBox stepsPanel = new VBox(25);
        stepsPanel.setAlignment(Pos.CENTER_LEFT);
        stepsPanel.setPrefWidth(400);
        stepsPanel.setPadding(new Insets(30));

        Label headerLabel = new Label("Phone Camera Setup");
        headerLabel.setStyle("""
            -fx-font-size: 28px; 
            -fx-font-weight: 700; 
            -fx-text-fill: #f8f9fa;
            """);

        Label subHeaderLabel = new Label("Follow these simple steps to capture your image");
        subHeaderLabel.setStyle("""
            -fx-font-size: 14px; 
            -fx-text-fill: #adb5bd;
            """);

        VBox headerBox = new VBox(8, headerLabel, subHeaderLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox stepsContainer = new VBox(20);
        stepsContainer.setAlignment(Pos.CENTER_LEFT);

        VBox step1 = createStepCard("1", "Network Connection",
                "Make sure your PC and mobile phone are connected to the same WiFi network");

        VBox step2 = createStepCard("2", "Scan QR Code",
                "Use your phone's camera app to scan the QR code displayed on the right");

        VBox step3 = createStepCard("3", "Take Photo",
                "Click the 'Take Photo' button on your phone and capture the image");

        VBox step4 = createStepCard("4", "Complete",
                "Once uploaded, the 'Proceed to Analysis' button will be enabled");

        stepsContainer.getChildren().addAll(step1, step2, step3, step4);

        goToRunPanelButton = new Button("Proceed to Analysis");
        goToRunPanelButton.setDisable(true);
        goToRunPanelButton.setOnAction(e -> goToRunPanel());

        String buttonStyle = """
            -fx-pref-width: 280;
            -fx-pref-height: 45;
            -fx-font-size: 16px;
            -fx-font-weight: 600;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-background-color: #2a2d31;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, #4dabf7, #9775fa);
            -fx-text-fill: #f1f3f5;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);
            """;

        String disabledStyle = """
            -fx-pref-width: 280;
            -fx-pref-height: 45;
            -fx-font-size: 16px;
            -fx-font-weight: 600;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-background-color: #1a1a1a;
            -fx-border-width: 2;
            -fx-border-color: #404040;
            -fx-text-fill: #666666;
            """;

        goToRunPanelButton.setStyle(disabledStyle);

        goToRunPanelButton.disabledProperty().addListener((obs, wasDisabled, isNowDisabled) -> {
            if (!isNowDisabled) {
                goToRunPanelButton.setStyle(buttonStyle);
                goToRunPanelButton.setOnMouseEntered(e ->
                        goToRunPanelButton.setStyle(buttonStyle + "-fx-background-color: linear-gradient(to right, #36404a, #2f343a);"));
                goToRunPanelButton.setOnMouseExited(e ->
                        goToRunPanelButton.setStyle(buttonStyle));
            }
        });

        VBox buttonContainer = new VBox(20);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.getChildren().add(goToRunPanelButton);

        stepsPanel.getChildren().addAll(headerBox, stepsContainer, buttonContainer);

        return stepsPanel;
    }

    private VBox createStepCard(String stepNumber, String title, String description) {
        VBox stepCard = new VBox(8);
        stepCard.setAlignment(Pos.CENTER_LEFT);
        stepCard.setPadding(new Insets(20));
        stepCard.setStyle("""
            -fx-background-color: rgba(42, 45, 49, 0.8);
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-border-color: rgba(73, 171, 247, 0.3);
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
            """);

        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label numberLabel = new Label(stepNumber);
        numberLabel.setStyle("""
            -fx-background-color: linear-gradient(to right, #4dabf7, #9775fa);
            -fx-background-radius: 20;
            -fx-text-fill: white;
            -fx-font-weight: 700;
            -fx-font-size: 14px;
            -fx-min-width: 28;
            -fx-min-height: 28;
            -fx-alignment: center;
            """);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: 600;
            -fx-text-fill: #f8f9fa;
            """);

        titleBox.getChildren().addAll(numberLabel, titleLabel);

        Label descLabel = new Label(description);
        descLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #adb5bd;
            -fx-wrap-text: true;
            """);
        descLabel.setMaxWidth(320);

        stepCard.getChildren().addAll(titleBox, descLabel);

        return stepCard;
    }

    private VBox createQRPanel() {
        VBox qrPanel = new VBox(20);
        qrPanel.setAlignment(Pos.CENTER);
        qrPanel.setPrefWidth(400);
        qrPanel.setPadding(new Insets(30));

        VBox qrContainer = new VBox(15);
        qrContainer.setAlignment(Pos.CENTER);
        qrContainer.setPadding(new Insets(30));
        qrContainer.setStyle("""
            -fx-background-color: rgba(42, 45, 49, 0.9);
            -fx-background-radius: 16;
            -fx-border-radius: 16;
            -fx-border-width: 2;
            -fx-border-color: linear-gradient(to right, #4dabf7, #9775fa);
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 8);
            """);

        Label qrTitle = new Label("Scan with Phone Camera");
        qrTitle.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: 600;
            -fx-text-fill: #f8f9fa;
            """);

        try {
            BufferedImage qrImage = generateQRCode(serverUrl);
            ImageView qrImageView = new ImageView(SwingFXUtils.toFXImage(qrImage, null));
            qrImageView.setStyle("""
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);
                """);

            Label urlLabel = new Label(serverUrl);
            urlLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-text-fill: #6c757d;
                -fx-font-family: 'Courier New', monospace;
                """);

            qrContainer.getChildren().addAll(qrTitle, qrImageView, urlLabel);

        } catch (Exception e) {
            Label errorLabel = new Label("QR Code generation failed");
            errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
            qrContainer.getChildren().addAll(qrTitle, errorLabel);
        }

        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(20));
        statusBox.setStyle("""
            -fx-background-color: rgba(32, 32, 32, 0.8);
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            -fx-border-color: rgba(108, 117, 125, 0.3);
            """);

        Label statusTitle = new Label("Connection Status");
        statusTitle.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: 600;
            -fx-text-fill: #f8f9fa;
            """);

        Label statusIndicator = new Label("● Server Running");
        statusIndicator.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #51cf66;
            """);

        Label waitingLabel = new Label("Waiting for image upload...");
        waitingLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #adb5bd;
            """);

        statusBox.getChildren().addAll(statusTitle, statusIndicator, waitingLabel);

        qrPanel.getChildren().addAll(qrContainer, statusBox);

        return qrPanel;
    }

    private BufferedImage generateQRCode(String text) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        BufferedImage image = new BufferedImage(QR_SIZE, QR_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < QR_SIZE; x++) {
            for (int y = 0; y < QR_SIZE; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    private void goToRunPanel() {
        if (latestImage != null) {
            String imagePath = saveImageLocally();
            if (imagePath != null) {
                app.showRunPanel(imagePath);
                stopServer();
            }
        }
    }

    private String saveImageLocally() {
        try {
            File dir = new File("captured_images");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "captured_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(dir, fileName);

            try (ByteArrayInputStream bis = new ByteArrayInputStream(latestImage);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                BufferedImage bufferedImage = ImageIO.read(bis);
                ImageIO.write(bufferedImage, "jpg", outputFile);
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                app.setCapturedImage(new Image(outputFile.toURI().toString()));
                return outputFile.getAbsolutePath();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String getWebPage() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Parkinson Detection - Photo Capture</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: Arial, sans-serif;
                    background: linear-gradient(135deg, #1c1c1e 0%, #2a2d32 100%);
                    color: #f0f0f0; 
                    min-height: 100vh; 
                    padding: 20px;
                }
                .container {
                    max-width: 420px; 
                    margin: 0 auto; 
                    background: #222428;
                    border-radius: 14px; 
                    padding: 28px;
                    border: 1px solid;
                    border-image: linear-gradient(135deg, #3f51b5, #673ab7) 1;
                    box-shadow: 0 12px 25px rgba(0,0,0,0.4);
                }
                h1 { 
                    color: #3f8efc; 
                    text-align: center; 
                    margin-bottom: 20px; 
                    font-size: 20px;
                }
                .btn {
                    background: #1e1f24;
                    border: 1px solid;
                    border-image: linear-gradient(135deg, #3f51b5, #673ab7) 1;
                    border-radius: 10px; 
                    color: white;
                    padding: 14px 28px; 
                    font-size: 15px; 
                    font-weight: 600;
                    cursor: pointer; 
                    width: 100%; 
                    margin-bottom: 15px;
                    transition: 0.3s;
                }
                .btn:hover {
                    background: linear-gradient(135deg, #3f51b5, #673ab7);
                }
                .upload-area {
                    border: 1px dashed;
                    border-image: linear-gradient(135deg, #3f51b5, #673ab7) 1;
                    border-radius: 10px; 
                    padding: 25px; 
                    text-align: center;
                    margin: 20px 0; 
                    cursor: pointer;
                    color: #bbb;
                    transition: 0.3s;
                }
                .upload-area:hover {
                    background: rgba(63, 81, 181, 0.1);
                }
                #preview { margin-top: 20px; text-align: center; }
                #preview img { 
                    max-width: 100%; 
                    max-height: 200px; 
                    border-radius: 8px; 
                }
                .status { 
                    text-align: center; 
                    margin: 15px 0; 
                    padding: 10px;
                    border-radius: 6px; 
                    font-weight: 500;
                }
                .status.success { background: rgba(76, 175, 80, 0.15); color: #4caf50; }
                .status.error { background: rgba(244, 67, 54, 0.15); color: #f44336; }
                input[type="file"] { display: none; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>Parkinson Detection - Photo Capture</h1>
                
                <button class="btn" onclick="openCamera()">
                    Parkinson Detection - Photo Capture
                </button>
                
                <div class="upload-area" onclick="document.getElementById('gallery').click()">
                    <div>Choose from Gallery</div>
                </div>
                
                <input type="file" id="camera" accept="image/*" capture="environment" style="aspect-ratio: 16/9;">
                <input type="file" id="gallery" accept="image/*">
                
                <div id="preview"></div>
                <div id="status"></div>
            </div>
        
            <script>
                let selectedFile = null;
                
                function openCamera() {
                    document.getElementById('camera').click();
                }
                
                document.getElementById('camera').onchange = handleFile;
                document.getElementById('gallery').onchange = handleFile;
                
                function handleFile(e) {
                    const file = e.target.files[0];
                    if (file && file.type.startsWith('image/')) {
                        selectedFile = file;
                        showPreview(file);
                        uploadImage();
                    }
                }
                
                function showPreview(file) {
                    const reader = new FileReader();
                    reader.onload = e => {
                        document.getElementById('preview').innerHTML = 
                            '<img src="' + e.target.result + '" alt="Preview">';
                    };
                    reader.readAsDataURL(file);
                }
                
                async function uploadImage() {
                    if (!selectedFile) return;
                    
                    showStatus('Uploading...', 'info');
                    
                    const formData = new FormData();
                    formData.append('image', selectedFile);
                    
                    try {
                        const response = await fetch('/', {
                            method: 'POST',
                            body: formData
                        });
                        
                        if (response.ok) {
                            showStatus('Upload successful!', 'success');
                        } else {
                            throw new Error('Upload failed');
                        }
                    } catch (error) {
                        showStatus('Upload failed: ' + error.message, 'error');
                    }
                }
                
                function showStatus(message, type) {
                    document.getElementById('status').innerHTML = 
                        '<div class="status ' + type + '">' + message + '</div>';
                }
            </script>
        </body>
        </html>
        """;
    }

}