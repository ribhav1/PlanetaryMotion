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

public class SimController {
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
    @FXML private TextField initVelocity;

    private double newPlanetMassValue;
    private double newPlanetRadiusValue;

    @FXML private Canvas canvas;
    private GraphicsContext gc;

    public ArrayList<Body> initPlanets = new ArrayList<>();
    private ArrayList<Body> planets = new ArrayList<>();
    private Body newPlanet;
    private static final double PIXELS_PER_UNIT = 250; // Scale factor for visualization
    //private static final double TIME_STEP = 0.01; // Simulation step in seconds
    private static double timelapse = TimeUnits.SECONDS_PER_MONTH;
    private static double sliderTimelapse = 1;

    private double initialCenterX;
    private double initialCenterY;
    private boolean firstDraw = true;

    public double cursorX;
    public double cursorY;

    private boolean inArrowMode = false;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        timelapse = TimeUnits.SECONDS_PER_MONTH;

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

                newPlanet = new Body((int)newPlanetRadiusValue, newPlanetMassValue, new double[]{simX, simY}, Color.BLACK);
                addPlanetButton.setText("Place");
                addPlanetButton.setDisable(true);

                inArrowMode = true;
                initVelocity.setDisable(false);
                inPlaceMode = false;
            } else if (inArrowMode) {
                if (!initVelocity.getText().isEmpty()) {
                    // convert simulation coords to screen coords
                    double bodyX = initialCenterX + newPlanet.transform[0] * PIXELS_PER_UNIT;
                    double bodyY = initialCenterY + newPlanet.transform[1] * PIXELS_PER_UNIT;

                    double[] arrowInfo = calculateArrowInfo(bodyX, bodyY);
                    double arrowAngle = arrowInfo[2];
                    double[] initV = new double[]{ Double.parseDouble(initVelocity.getText()) * Math.cos(arrowAngle), Double.parseDouble(initVelocity.getText()) * Math.sin(arrowAngle)};

                    planets.add(new Body(newPlanet.radius, newPlanet.mass, newPlanet.transform, newPlanet.color, initV));

                    pauseButton.setDisable(false);
                    addPlanetButton.setDisable(false);
                    initVelocity.setDisable(true);
                    paused = prePlacePause;
                    inArrowMode = false;
                } else {
                    errorText.setText("Please enter parameters");
                    errorText.setTextFill(Color.RED);
                    errorText.setVisible(true);
                }

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
    private boolean prePlacePause = paused;

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

    @FXML
    private void handleResetClick() {
        copyInitPlanets();
        setVarsToInit();
    }

    private void setVarsToInit(){
        firstDraw = true;

        pauseButton.setDisable(false);
        pauseButton.setText("Start");
        pauseButton.styleProperty().set("-fx-background-color: green");

        timelapse = TimeUnits.SECONDS_PER_MONTH;
        timelapseSlider.valueProperty().set(1);

        newPlanetMass.setText("");
        newPlanetRadius.setText("");

        initVelocity.setText("");
        initVelocity.setDisable(true);

        addPlanetButton.setText("Place");
        addPlanetButton.setDisable(false);

        inPlaceMode = false;
        inArrowMode = false;

        paused = true;
        prePlacePause = paused;
    }

    private void copyInitPlanets()
    {
        planets.clear();
        for(int i = 0; i<initPlanets.size(); i++)
        {
            // nightmare bug:
            // this is necessary because the velocity of the initPlanet
            // has already been converted to sim units
            // however, the velocity is copied from initPlanets to planets
            // the construction of this new body redoes the conversion factor,
            // leading to the velocity being much smaller than desired
            // thus, the initPlanet velocity must be converted to distUnits first

            // tl;dr : body constructor expects velocity in real-world units (m/s) and converts it to sim units.
            // so, initPlanets already have velocity in sim units, so passing it directly would double-scale.
            double[] vel = initPlanets.get(i).velocity.clone();
            double realUnitVelX = Units.simUnitsToDistUnits(vel[0]);
            double realUnitVelY = Units.simUnitsToDistUnits(vel[1]);
            Body newCopyPlanet = new Body(
                    initPlanets.get(i).radius,
                    initPlanets.get(i).mass,
                    initPlanets.get(i).transform.clone(),
                    initPlanets.get(i).color,
                    new double[]{realUnitVelX, realUnitVelY});
            planets.add(newCopyPlanet);
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
            prePlacePause = paused;
            paused = true;
            pauseButton.setDisable(true);
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

        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            if (firstDraw) {
                copyInitPlanets();

                initialCenterX = canvas.getWidth() / 2;
                initialCenterY = canvas.getHeight() / 2;
                firstDraw = false;
            }
        } else {
            return;
        }

        for (Body planet : planets) {
            double x = initialCenterX + planet.transform[0] * PIXELS_PER_UNIT;
            double y = initialCenterY + planet.transform[1] * PIXELS_PER_UNIT;

            gc.setFill(Color.BLACK);
            gc.fillText((Math.round(planet.transform[0] * 100.0) / 100.0 + ", " + (Math.round(planet.transform[1]*100.0)/100.0)), x + 0.25 * PIXELS_PER_UNIT, y - 0.25 * PIXELS_PER_UNIT);

            gc.setFill(planet.color);
            gc.fillOval(x - planet.radius, y - planet.radius, planet.radius * 2, planet.radius * 2);
        }

        if (inPlaceMode) {
            gc.setFill(Color.BLACK);
            gc.fillText((cursorX - initialCenterX)/ PIXELS_PER_UNIT + ", " + (cursorY - initialCenterY)/ PIXELS_PER_UNIT, cursorX + 0.25 * PIXELS_PER_UNIT, cursorY - 0.25 * PIXELS_PER_UNIT);
            gc.fillOval(cursorX - newPlanetRadiusValue, cursorY - newPlanetRadiusValue, newPlanetRadiusValue * 2, newPlanetRadiusValue * 2);
        }
        if (inArrowMode && newPlanet != null) {

            // render temp planet while placing arrow for its initial velocity
            double x = initialCenterX + newPlanet.transform[0] * PIXELS_PER_UNIT;
            double y = initialCenterY + newPlanet.transform[1] * PIXELS_PER_UNIT;

            gc.setFill(Color.BLACK);
            gc.fillText((Math.round(newPlanet.transform[0] * 100.0) / 100.0 + ", " + (Math.round(newPlanet.transform[1]*100.0)/100.0)), x + 0.25 * PIXELS_PER_UNIT, y - 0.25 * PIXELS_PER_UNIT);
            gc.setFill(newPlanet.color);
            gc.fillOval(x - newPlanet.radius, y - newPlanet.radius, newPlanet.radius * 2, newPlanet.radius * 2);

            // convert simulation coords to screen coords
            double bodyX = initialCenterX + newPlanet.transform[0] * PIXELS_PER_UNIT;
            double bodyY = initialCenterY + newPlanet.transform[1] * PIXELS_PER_UNIT;

            double[] arrowInfo = calculateArrowInfo(bodyX, bodyY);

            // scale them to screen size using PIXELS_PER_UNIT - screen coords back to sim coords
            // make their origins at the planet's body
            double endX = bodyX + arrowInfo[0] * PIXELS_PER_UNIT/2;
            double endY = bodyY + arrowInfo[1] * PIXELS_PER_UNIT/2;

            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(bodyX, bodyY, endX, endY);

        }
    }

    private double[] calculateArrowInfo(double bodyX, double bodyY)
    {
        double vectorX = cursorX - bodyX;
        double vectorY = cursorY - bodyY;
        double magnitude = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

        //normalize vectors
        double normVectorX = vectorX / magnitude;
        double normVectorY = vectorY / magnitude;

        double arrowAngle = Math.atan2(vectorY, vectorX);

        return new double[]{normVectorX, normVectorY, arrowAngle};
    }

}