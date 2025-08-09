package com.example.demo;

import javafx.animation.*;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.Node;
import javafx.scene.paint.Stop;

import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeController {

    @FXML private StackPane rootPane;
    @FXML private Canvas starCanvas;

    @FXML private Button launchButton;
    @FXML private Button docButton;
    @FXML private Button startNowButton;
    @FXML private FlowPane featuresPane;

    private final List<Star> stars = new ArrayList<>();
    private final Random rng = new Random();

    @FXML
    public void initialize() {
        starCanvas.widthProperty().bind(rootPane.widthProperty());
        starCanvas.heightProperty().bind(rootPane.heightProperty());

        // Recreate stars whenever the canvas size changes
        starCanvas.widthProperty().addListener((obs, oldW, newW) -> resetStars());
        starCanvas.heightProperty().addListener((obs, oldH, newH) -> resetStars());

        setupStars();
        startStarAnimation();
    }

    private void resetStars() {
        stars.clear();
        setupStars();
    }

    private void setupStars() {
        double w = starCanvas.getWidth();
        double h = starCanvas.getHeight();

        // create layered stars, different speeds and sizes
        for (int i = 0; i < 220; i++) {
            double x = rng.nextDouble() * w;
            double y = rng.nextDouble() * h;
            double size = rng.nextDouble() * 2.2 + (rng.nextDouble() < 0.05 ? 1.8 : 0.6);
            double speed = 0.1 + rng.nextDouble() * 0.8 + (size > 1.8 ? 0.9 : 0);
            double alpha = 0.3 + rng.nextDouble() * 0.7;
            stars.add(new Star(x, y, size, speed, alpha));
        }
    }

    private void startStarAnimation() {
        GraphicsContext gc = starCanvas.getGraphicsContext2D();

        Timeline starTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            double w = starCanvas.getWidth();
            double h = starCanvas.getHeight();

            // subtle background gradient base
            gc.setFill(Color.web("#07080C"));
            gc.fillRect(0, 0, w, h);

            // faint purple haze
            Stop[] stops = new Stop[] { new Stop(0, Color.web("#0B0F1A")), new Stop(1, Color.web("#2D1B69", 0.18)) };
            gc.setFill(new javafx.scene.paint.LinearGradient(0,0,1,1,true, javafx.scene.paint.CycleMethod.NO_CYCLE, stops));
            gc.fillRect(0, 0, w, h);

            // draw + move stars
            for (Star s : stars) {
                // twinkle
                s.alpha += (rng.nextDouble() - 0.5) * 0.06;
                s.alpha = clamp(s.alpha, 0.15, 1.0);

                // draw
                gc.setGlobalAlpha(s.alpha);
                if (s.size > 2.0) {
                    gc.setFill(Color.web("#ffffff", s.alpha));
                    gc.fillOval(s.x, s.y, s.size * 1.6, s.size * 1.6);
                } else {
                    gc.setFill(Color.web("#ffffff", s.alpha * 0.9));
                    gc.fillOval(s.x, s.y, s.size, s.size);
                }
                gc.setGlobalAlpha(1.0);

                // move right-to-left slightly for parallax
                s.x -= s.speed;
                if (s.x < -10) s.x = w + 10;
                if (s.y < -10) s.y = h + 10;
                if (s.y > h + 10) s.y = -10;
            }
        }));
        starTimeline.setCycleCount(Animation.INDEFINITE);
        starTimeline.play();
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static class Star {
        double x, y, size, speed, alpha;
        Star(double x, double y, double size, double speed, double alpha) {
            this.x = x; this.y = y; this.size = size; this.speed = speed; this.alpha = alpha;
        }
    }

    @FXML
    private void onButtonEnter(MouseEvent e) {
        Node src = (Node) e.getSource();
        ScaleTransition s = new ScaleTransition(Duration.millis(160), src);
        s.setToX(1.04); s.setToY(1.04); s.play();

        // stronger glow
        DropShadow ds = new DropShadow(28, Color.web("#8B5CF6", 0.36));
        src.setEffect(ds);
    }

    @FXML
    private void onButtonExit(MouseEvent e) {
        Node src = (Node) e.getSource();
        ScaleTransition s = new ScaleTransition(Duration.millis(160), src);
        s.setToX(1.0); s.setToY(1.0); s.play();

        // restore subtle shadow depending on which button
        if (src == docButton) {
            src.setEffect(new DropShadow(6, Color.web("#7C3AED", 0.15)));
        } else {
            src.setEffect(new DropShadow(18, Color.web("#8B5CF6", 0.22)));
        }
    }

    @FXML
    private void onCardEnter(MouseEvent me) {
        Node card = (Node) me.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
        st.setToX(1.03); st.setToY(1.03); st.play();

        // pulse border via style toggle (add brighter border)
        String base = card.getStyle();
        if (!base.contains("-fx-border-color: rgba(200,200,255,0.25)")) {
            card.setStyle(base + "-fx-border-color: rgba(140,86,255,0.36); -fx-border-width: 1.4;");
        }
    }

    @FXML
    private void onCardExit(MouseEvent me) {
        Node card = (Node) me.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(160), card);
        st.setToX(1.0); st.setToY(1.0); st.play();

        // remove pulse border by restoring approximate original
        String s = card.getStyle();
        s = s.replace("-fx-border-color: rgba(140,86,255,0.36);", "-fx-border-color: rgba(140,86,255,0.18);");
        s = s.replace("-fx-border-width: 1.4;", "-fx-border-width: 1;");
        card.setStyle(s);
    }

    @FXML
    private void onLaunch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sim.fxml"));
            Node screen = loader.load();
            SimController simController = loader.getController();

            double sunMass = 1.989e30;
            double earthMass = 5.972e24;
            //double earthDist = Units.distUnitsToSimUnits(1.496e11); // 1 AU
            double earthVel = 29780;

            simController.initPlanets.add(new Body(50, sunMass, new double[]{0.0, 0.0}, Color.ORANGE));
            simController.initPlanets.add(new Body(10, earthMass, new double[]{1.0, 0.0}, Color.BLUE, new double[]{0.0, 29780}));

            rootPane.getChildren().setAll(screen);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDocs() {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/ribhav1/PlanetaryMotion"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
