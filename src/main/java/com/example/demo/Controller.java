package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;


import java.util.ArrayList;

public class Controller {
    @FXML private Pane canvasContainer; // Parent container for Canvas
    @FXML private Button pauseButton;
    @FXML private Slider timelapseSlider;
    @FXML private TextField newPlanetMass;
    @FXML private TextField newPlanetRadius;
    @FXML private Button addPlanetButton;
    @FXML private Label errorText;
    @FXML private Button dayTimeRatioButton;
    @FXML private Button monthTimeRatioButton;
    @FXML private Button yearTimeRatioButton;

    private double newPlanetMassValue;
    private double newPlanetRadiusValue;

    private Canvas canvas;
    private GraphicsContext gc;

    private ArrayList<Body> planets = new ArrayList<>();
    private static final double PIXELS_PER_UNIT = 250; // Scale factor for visualization
    //private static final double TIME_STEP = 0.01; // Simulation step in seconds
    private static double timelapse = TimeUnits.SECONDS_PER_MONTH;
    private static double sliderTimelapse = 1;

    private double initialCenterX;
    private double initialCenterY;
    private boolean firstDraw = true;

    public double cursorX;
    public double cursorY;

    @FXML
    public void initialize() {
        canvas = new Canvas(400, 400);
        gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas); // Add canvas to the FXML Pane

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());
        canvas.setOnMouseMoved((MouseEvent event) -> {
            cursorX = event.getX();
            cursorY = event.getY();
        });
        canvas.setOnMouseClicked((MouseEvent event) -> {
            if (inPlaceMode)
            {
                //convert screen/pixel space coords to simulation coords
                double simX = (cursorX - initialCenterX) / PIXELS_PER_UNIT;
                double simY = (cursorY - initialCenterY) / PIXELS_PER_UNIT;

                System.out.println(newPlanetMassValue);
                planets.add(new Body((int)newPlanetRadiusValue, newPlanetMassValue, new double[]{simX, simY}, Color.BLACK));
                addPlanetButton.setText("Place");
                inPlaceMode = false;
            }
        });

        dayTimeRatioButton.setOnAction(event -> handleTimeLapseButtonClick("day"));
        monthTimeRatioButton.setOnAction(event -> handleTimeLapseButtonClick("month"));
        yearTimeRatioButton.setOnAction(event -> handleTimeLapseButtonClick("year"));

        timelapseSlider.valueProperty().set(1);
        timelapseSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderTimelapse = newValue.doubleValue();
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
            pauseButton.styleProperty().set("-fx-background-color: green");
        } else {
            pauseButton.setText("Pause");
            System.out.println("Simulation resumed.");
            pauseButton.styleProperty().set("-fx-background-color: red");
        }
    }

    private boolean inPlaceMode = false;

    @FXML
    private void handleAddPlanetClick() {
        if (!newPlanetRadius.getText().isEmpty() && !newPlanetMass.getText().isEmpty()) {
            if (errorText.isVisible()) {
                errorText.setVisible(false);
            }
            newPlanetMassValue = Double.parseDouble(newPlanetMass.getText());
            newPlanetRadiusValue = Double.parseDouble(newPlanetRadius.getText());
            addPlanetButton.setText("Update params");
            inPlaceMode = true;
        } else {
            errorText.setText("Please enter parameters");
            errorText.setTextFill(Color.RED);
            errorText.setVisible(true);
        }
    }

    @FXML
    private void handleTimeLapseButtonClick(String timeRatio) {
        switch (timeRatio) {
            case "day":
                timelapse = TimeUnits.SECONDS_PER_DAY;
                break;
            case "month":
                timelapse = TimeUnits.SECONDS_PER_MONTH;
                break;
            case "year":
                timelapse = TimeUnits.SECONDS_PER_YEAR;
                break;
        }
    }

    private void setupPlanets() {

        /*
        // BLACK HOLES COLLIDING
        planets.add(new Body(15, 1e45, new double[]{-1.0, -1.0}, Color.BLACK, new double[]{0.0, 2.0}));
        planets.add(new Body(15, 1e45, new double[]{1.0, 1.0}, Color.BLACK, new double[]{0.0, -2.0}));
        */

        double sunMass = 1.989e30;
        double earthMass = 5.972e24;
        //double earthDist = Units.distUnitsToSimUnits(1.496e11); // 1 AU
        double earthVel = 29780;


        planets.add(new Body(50, sunMass, new double[]{0.0, 0.0}, Color.ORANGE));
        planets.add(new Body(10, earthMass, new double[]{1, 0.0}, Color.BLUE, new double[]{0.0, 29780}));

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
            Body.updateTransform(planets, deltaTime * timelapse * sliderTimelapse);
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
            double x = initialCenterX + planet.transform[0] * PIXELS_PER_UNIT;
            double y = initialCenterY + planet.transform[1] * PIXELS_PER_UNIT;

            gc.fillText((Math.round(planet.transform[0] * 100.0) / 100.0 + ", " + (Math.round(planet.transform[1]*100.0)/100.0)), x + 0.25 * PIXELS_PER_UNIT, y - 0.25 * PIXELS_PER_UNIT);

            gc.setFill(planet.color);
            gc.fillOval(x - planet.radius, y - planet.radius, planet.radius * 2, planet.radius * 2);
        }

        if (inPlaceMode) {
            gc.fillText((cursorX - initialCenterX)/ PIXELS_PER_UNIT + ", " + (cursorY - initialCenterY)/ PIXELS_PER_UNIT, cursorX, cursorY);
            gc.fillOval(cursorX - newPlanetRadiusValue, cursorY - newPlanetRadiusValue, newPlanetRadiusValue * 2, newPlanetRadiusValue * 2);
        }
    }
}
