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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ScanFromPhonePanel extends HBox {

    private static final int PORT = 8080;
    private static final int QR_SIZE = 300;
    private App app;
    private HttpServer server;
    private String serverUrl;
    private volatile byte[] latestImage;
    private Button goToRunPanelButton;

    public ScanFromPhonePanel(App app) {
        this.app = app;
        try {
            startWebServer();
            setupLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLayout() {
        // Set background style for the entire panel
        setStyle("-fx-background-color: #1e1e1e;");
        setPadding(new Insets(30));

        // Left side - Steps panel
        VBox leftPanel = createStepsPanel();

        // Right side - QR code panel
        VBox rightPanel = createQRPanel();

        // Set the panels
        this.getChildren().addAll(leftPanel, rightPanel);
        this.setAlignment(Pos.CENTER);
    }

    private VBox createStepsPanel() {
        VBox stepsPanel = new VBox(25);
        stepsPanel.setAlignment(Pos.CENTER_LEFT);
        stepsPanel.setPrefWidth(400);
        stepsPanel.setPadding(new Insets(30));

        // Header
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

        // Steps
        VBox stepsContainer = new VBox(20);
        stepsContainer.setAlignment(Pos.CENTER_LEFT);

        // Step 1
        VBox step1 = createStepCard("1", "Network Connection",
                "Make sure your PC and mobile phone are connected to the same WiFi network");

        // Step 2
        VBox step2 = createStepCard("2", "Scan QR Code",
                "Use your phone's camera app to scan the QR code displayed on the right");

        // Step 3
        VBox step3 = createStepCard("3", "Take Photo",
                "Click the 'Take Photo' button on your phone and capture the image");

        // Step 4
        VBox step4 = createStepCard("4", "Complete",
                "Once uploaded, the 'Proceed to Analysis' button will be enabled");

        stepsContainer.getChildren().addAll(step1, step2, step3, step4);

        // Proceed button
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

        // Add hover effects for enabled state
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

        // Step number and title
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

        // Description
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

        // QR Code container
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

        // QR Code
        try {
            BufferedImage qrImage = generateQRCode(serverUrl);
            ImageView qrImageView = new ImageView(SwingFXUtils.toFXImage(qrImage, null));
            qrImageView.setStyle("""
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);
                """);

            // URL display
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

        // Status indicator
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

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void goToRunPanel() {
        if (latestImage != null) {
            String imagePath = saveImageLocally();
            if (imagePath != null) {
                System.out.println("Image saved at: " + imagePath);
                app.showRunPanel(imagePath);
                stopServer();
            } else {
                System.out.println("Failed to save image");
            }
        } else {
            System.out.println("No image data available");
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
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {

                BufferedImage bufferedImage = ImageIO.read(bis);
                ImageIO.write(bufferedImage, "jpg", outputFile);
                fos.flush();
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

    private void startWebServer() throws IOException {
        String localIP = getLocalIPAddress();
        serverUrl = "http://" + localIP + ":" + PORT;

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", new StaticPageHandler(this));
        server.setExecutor(null);
        server.start();

        System.out.println("Server started at: " + serverUrl);
    }

    private String getLocalIPAddress() throws IOException {
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();
            if (nic.isLoopback() || !nic.isUp()) continue;

            Enumeration<java.net.InetAddress> addrs = nic.getInetAddresses();
            while (addrs.hasMoreElements()) {
                java.net.InetAddress addr = addrs.nextElement();
                if (!addr.isLoopbackAddress() && addr instanceof java.net.InetAddress) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
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

    static class StaticPageHandler implements HttpHandler {
        private final ScanFromPhonePanel panel;

        public StaticPageHandler(ScanFromPhonePanel panel) {
            this.panel = panel;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                handleGetRequest(exchange);
            } else if ("POST".equals(method)) {
                handlePostRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            String response = """
       <!DOCTYPE html>
       <html>
       <head>
           <title>Camera Upload</title>
           <meta name="viewport" content="width=device-width, initial-scale=1">
           <style>
               @import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap');

               body {
                   font-family: 'Roboto', sans-serif;
                   background: #1e1e1e;
                   margin: 0;
                   padding: 20px;
                   text-align: center;
                   color: #f5f5f5;
               }
               h1 {
                   color: #ffffff;
                   margin-bottom: 20px;
               }
               button {
                   padding: 15px 30px;
                   font-size: 18px;
                   background: #2e2e2e;
                   color: #f5f5f5;
                   border: 3px solid transparent;
                   border-radius: 8px;
                   cursor: pointer;
                   background-clip: padding-box;
                   position: relative;
               }
               button::before {
                   content: "";
                   position: absolute;
                   top: -3px;
                   left: -3px;
                   right: -3px;
                   bottom: -3px;
                   border-radius: 10px;
                   background: linear-gradient(45deg, #00c6ff, #8e2de2);
                   z-index: -1;
               }
               button:hover {
                   background: #3a3a3a;
               }
               #preview {
                   margin-top: 20px;
               }
               #preview img {
                   border: 2px solid #00c6ff;
                   border-radius: 10px;
                   max-width: 300px;
                   max-height: 300px;
               }
           </style>
       </head>
       <body>
           <h1>Take a Photo</h1>
           <input type="file" id="camera" accept="image/*" capture="camera" style="display:none">
           <button onclick="document.getElementById('camera').click()">Click Photo</button>
           <div id="preview"></div>
           
           <script>
           document.getElementById('camera').addEventListener('change', function(e) {
               const file = e.target.files[0];
               if (file) {
                   const preview = document.getElementById('preview');
                   const img = document.createElement('img');
                   img.src = URL.createObjectURL(file);
                   preview.innerHTML = '<h3>Preview:</h3>';
                   preview.appendChild(img);
                   
                   const formData = new FormData();
                   formData.append('image', file);
                   
                   fetch('/', {
                       method: 'POST',
                       body: formData
                   }).then(response => {
                       if (response.ok) {
                           preview.innerHTML += '<p style="color:lightgreen;">Image uploaded successfully!</p>';
                       }
                   });
               }
           });
           </script>
       </body>
       </html>
       """;

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType != null && contentType.contains("multipart/form-data")) {
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                panel.latestImage = extractImageFromMultipart(requestBody);

                Platform.runLater(() -> {
                    panel.goToRunPanelButton.setDisable(false);
                });

                System.out.println("Image received and stored, size: " + panel.latestImage.length + " bytes");
            }

            String response = "Image uploaded successfully";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
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
    }
}