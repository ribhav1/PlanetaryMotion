package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;



import java.util.ArrayList;

public class Controller {
    @FXML
    private Pane canvasContainer; // Parent container for Canvas
    @FXML
    private Button pauseButton;

    @FXML Slider timelapseSlider;

    private Canvas canvas;
    private GraphicsContext gc;

    private ArrayList<Body> planets = new ArrayList<>();
    private static final double SCALE = 50; // Scale factor for visualization
    private static final double TIME_STEP = 0.01; // Simulation step in seconds
    private static double timelapse = 2.5;

    private double initialCenterX;
    private double initialCenterY;
    private boolean firstDraw = true;

    @FXML
    public void initialize() {
        canvas = new Canvas(400, 400);
        gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas); // Add canvas to the FXML Pane

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        timelapseSlider.valueProperty().set(2.5);
        timelapseSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            timelapse = newValue.doubleValue();
        });

        setupPlanets();
        startSimulation();
    }

    private boolean paused = true;

    @FXML
    private void handlePauseResumeClick() {
        paused = !paused;

        if (paused) {
            pauseButton.setText("Resume");
            System.out.println("Simulation paused.");

        } else {
            pauseButton.setText("Pause");
            System.out.println("Simulation resumed.");
        }
    }
    private void setupPlanets() {
        // BLACK HOLES COLLIDING
        planets.add(new Body(15, 1e12, new double[]{-3.0, -3.0}, Color.BLACK, new double[]{0.0, 2.0}));
        planets.add(new Body(15, 1e12, new double[]{3.0, 3.0}, Color.BLACK, new double[]{0.0, -2.0}));


        /*
        // SOLAR SYSTEM ESQUE ORBIT
        planets.add(new Body(15, 1e12, new double[]{0.0, 0.0}, Color.RED, new double[]{0.5, 0.0}));
        planets.add(new Body(10, 1e10, new double[]{-5.5, 2.0}, Color.GREEN, new double[]{0.0, -2.5}));
        planets.add(new Body(5, 1e9, new double[]{3.5, 0.0}, Color.BLUE, new double[]{0.0, -4.5}));
        */
    }

    private void startSimulation() {
        new AnimationTimer() {
            private long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTime = (now - lastTime) / 1e9; // Convert to seconds
                lastTime = now;

                updatePhysics(deltaTime);
                drawPlanets();
            }
        }.start();
    }

    private void updatePhysics(double deltaTime) {
        if(!paused) {
            Body.updateTransform(planets, deltaTime * timelapse);
        }
    }


    private void drawPlanets() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (firstDraw) {
            initialCenterX = canvas.getWidth() / 2;
            initialCenterY = canvas.getHeight() / 2;
            firstDraw = false; // Only set this once
        }

        for (Body planet : planets) {
            double x = initialCenterX + planet.transform[0] * SCALE;
            double y = initialCenterY + planet.transform[1] * SCALE;

            gc.setFill(planet.color);
            gc.fillOval(x - planet.radius, y - planet.radius, planet.radius * 2, planet.radius * 2);
        }
    }
}
