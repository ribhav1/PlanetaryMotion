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
import java.util.List;

public class SimController {
    // ui elements
    @FXML private Pane canvasContainer;
    @FXML private Canvas canvas;
    @FXML private Button pauseButton;
    @FXML private Button dayTimeRatioButton;
    @FXML private Button monthTimeRatioButton;
    @FXML private Button yearTimeRatioButton;
    @FXML private Slider timelapseSlider;
    @FXML private TextField newPlanetMass;
    @FXML private TextField newPlanetRadius;
    @FXML private TextField initVelocity;
    @FXML private Button addPlanetButton;
    @FXML private Label errorText;
    @FXML private Button reverseButton;

    // canvas drawing
    private GraphicsContext gc;
    private double[] centerScreen;
    private static final double PIXELS_PER_UNIT = 250; // Scale factor for visualization
    public double cursorX;
    public double cursorY;

    // planet data
    public ArrayList<Body> initPlanets = new ArrayList<>();
    private ArrayList<Body> planets = new ArrayList<>();
    private List<Body> removedPlanets = new ArrayList<>();
    private Body newPlanet;
    private double newPlanetMassValue;
    private double newPlanetRadiusValue;

    // simulation state
    private boolean paused = true;
    private boolean prePlacePause = paused;
    private boolean firstDraw = true;
    private boolean inPlaceMode = false;
    private boolean inArrowMode = false;

    private double timelapse = TimeUnits.SECONDS_PER_MONTH;
    private double sliderTimelapse = 1;
    private double reverse = 1;
    private double simulationTime = 0;

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
                double simX = (cursorX - centerScreen[0]) / PIXELS_PER_UNIT;
                double simY = (cursorY - centerScreen[1]) / PIXELS_PER_UNIT;

                newPlanet = new Body((int)newPlanetRadiusValue, newPlanetMassValue, new double[]{simX, simY}, Color.BLACK);
                addPlanetButton.setText("Place");
                addPlanetButton.setDisable(true);

                inArrowMode = true;
                initVelocity.setDisable(false);
                inPlaceMode = false;
            } else if (inArrowMode) {
                if (!initVelocity.getText().isEmpty()) {
                    // convert simulation coords to screen coords
                    double bodyX = centerScreen[0] + newPlanet.transform[0] * PIXELS_PER_UNIT;
                    double bodyY = centerScreen[1] + newPlanet.transform[1] * PIXELS_PER_UNIT;

                    double[] arrowInfo = calculateArrowInfo(bodyX, bodyY);
                    double arrowAngle = arrowInfo[2];
                    double[] initV = new double[]{ Double.parseDouble(initVelocity.getText()) * Math.cos(arrowAngle), Double.parseDouble(initVelocity.getText()) * Math.sin(arrowAngle)};

                    Body b = new Body(newPlanet.radius, newPlanet.mass, newPlanet.transform, newPlanet.color, initV);
                    b.creationTime = simulationTime;
                    planets.add(b);

                    reverseButton.setDisable(false);
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

        //setupPlanets();
        startSimulation();
    }

    @FXML
    private void handlePauseResumeClick() {
        paused = !paused;
        reverseButton.setDisable(false);
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

        reverse = 1;
        reverseButton.setDisable(true);

        paused = true;
        prePlacePause = paused;
    }

    private void copyInitPlanets()
    {
        planets.clear();
        removedPlanets.clear();
        for(int i = 0; i < initPlanets.size(); i++)
        {
            // nightmare bug:
            // even though the initial velocity of an initPlanet is inputted in real world units
            // initPlanets stores velocity in simulation units due to initial conversion in Body constructor.
            // reusing these velocities directly would cause a double conversion when creating new Body instances,
            // resulting in incorrect (overscaled, and very small) velocities.
            // to fix this, the velocity is converted back to real-world units before passing it to the constructor,
            // which will then reapply the correct sim-unit conversion.

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

    @FXML
    private void handleAddPlanetClick() {
        if (!newPlanetRadius.getText().isEmpty() && !newPlanetMass.getText().isEmpty()) {
            if (errorText.isVisible()) {
                errorText.setVisible(false);
            }
            newPlanetMassValue = Double.parseDouble(newPlanetMass.getText());
            newPlanetRadiusValue = Double.parseDouble(newPlanetRadius.getText());

            reverse = 1;
            reverseButton.setDisable(true);
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
    private void handleReverseClick() {
        reverse = -1 * reverse;
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
                double deltaTime = (now - lastTime) / 1e9; // convert to seconds
                lastTime = now;

                // scale the simulation time by the same factors as the physics,
                // ensuring creation/deletion logic of planets is based on in-simulation time and not real world time
                double TIME_TOLERANCE = 0.01 * timelapse * sliderTimelapse;
                if (!paused) {
                    simulationTime += deltaTime * timelapse * sliderTimelapse * reverse;
                }

                if (Math.abs(simulationTime - 0) < TIME_TOLERANCE && reverse == -1)
                {
                    handleResetClick();
                    simulationTime = 0;
                }

                if (reverse == -1) {
                    if (!planets.isEmpty()) {
                        // logic to delete planets at their creation time when reversing past it
                        List<Body> planetsToRemove = new ArrayList<>();
                        for (Body planet : planets) {
                            if (Math.abs(simulationTime - planet.creationTime) < TIME_TOLERANCE) {
                                planetsToRemove.add(planet);
                                if (!initPlanets.contains(planet)) {
                                    removedPlanets.add(planet);
                                }
                            }
                        }
                        planets.removeAll(planetsToRemove);
                    }

                } else if (reverse == 1) {
                    // logic to restore deleted planets when at their creation time when forwarding past it
                    List<Body> restoredPlanets = new ArrayList<>();
                    for (Body planet : removedPlanets) {
                        if (Math.abs(simulationTime - planet.creationTime) < TIME_TOLERANCE) {
                            planets.add(planet);
                            restoredPlanets.add(planet);
                        }
                    }
                    // prevent already restored planets from being restored multiple times
                    removedPlanets.removeAll(restoredPlanets);
                }

                updatePhysics(deltaTime);
                drawPlanets();
            }
        }.start();
    }

    private void updatePhysics(double deltaTime) {
        if(!paused) {
            Body.updateTransform(planets, deltaTime * timelapse * sliderTimelapse * reverse);
        }
    }

    private void drawPlanets() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            if (firstDraw) {
                copyInitPlanets();
                firstDraw = false;
            }
        } else {
            return;
        }

        centerScreen = new double[]{canvas.getWidth() / 2, canvas.getHeight() / 2};

        for (Body planet : planets) {
            renderPlanet(centerScreen, planet, Color.BLACK);
        }

        if (inPlaceMode) {
            // render the illusion of a planet that follows the cursor
            gc.setFill(Color.BLACK);
            gc.fillText((cursorX - centerScreen[0])/ PIXELS_PER_UNIT + ", " + (cursorY - centerScreen[1])/ PIXELS_PER_UNIT, cursorX + 0.25 * PIXELS_PER_UNIT, cursorY - 0.25 * PIXELS_PER_UNIT);
            gc.fillOval(cursorX - newPlanetRadiusValue, cursorY - newPlanetRadiusValue, newPlanetRadiusValue * 2, newPlanetRadiusValue * 2);
        }

        if (inArrowMode && newPlanet != null) {
            // render temp planet while placing arrow for its initial velocity
            renderPlanet(centerScreen, newPlanet, Color.BLACK);

            // convert simulation coords to screen coords
            double[] bodyCoords = simCoordsToScreenCoords(newPlanet.transform, centerScreen, PIXELS_PER_UNIT);
            double bodyX = bodyCoords[0];
            double bodyY = bodyCoords[1];

            // calculate normalized vector components of arrow
            double[] arrowInfo = calculateArrowInfo(bodyX, bodyY);

            // scale vector components to screen units using PIXELS_PER_UNIT
            // set component origins at the planet's body
            double endX = bodyX + arrowInfo[0] * PIXELS_PER_UNIT/2;
            double endY = bodyY + arrowInfo[1] * PIXELS_PER_UNIT/2;

            renderArrow(bodyCoords, new double[]{endX, endY}, Color.RED, 2);
        }
    }

    private double[] simCoordsToScreenCoords(double[] simCoords, double[] screenCoordsOrigin, double pixelUnitRatio) {
        // scale sim coords to screen units using pixelUnitRatio
        // then translate by screenCoordsOrigin
        double x = screenCoordsOrigin[0] + simCoords[0] * pixelUnitRatio;
        double y = screenCoordsOrigin[1] + simCoords[1] * pixelUnitRatio;
        return new double[]{x, y};
    }

    private void renderPlanet(double[] centerScreen, Body planet, Color textColor) {
        double x = simCoordsToScreenCoords(planet.transform, centerScreen, PIXELS_PER_UNIT)[0];
        double y = simCoordsToScreenCoords(planet.transform, centerScreen, PIXELS_PER_UNIT)[1];

        gc.setFill(textColor);
        gc.fillText((Math.round(planet.transform[0] * 100.0) / 100.0 + ", " + (Math.round(planet.transform[1]*100.0)/100.0)), x + 0.25 * PIXELS_PER_UNIT, y - 0.25 * PIXELS_PER_UNIT);

        gc.setFill(planet.color);
        gc.fillOval(x - planet.radius, y - planet.radius, planet.radius * 2, planet.radius * 2);
    }

    private double[] calculateArrowInfo(double bodyX, double bodyY)
    {
        // calculate vectors by subtracting cursor position from planet body position
        double vectorX = cursorX - bodyX;
        double vectorY = cursorY - bodyY;
        double magnitude = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

        // normalize vectors
        double normVectorX = vectorX / magnitude;
        double normVectorY = vectorY / magnitude;

        double arrowAngle = Math.atan2(vectorY, vectorX);

        return new double[]{normVectorX, normVectorY, arrowAngle};
    }

    private void renderArrow(double[] startCoords, double[] endCoords, Color lineColor, int lineWidth) {
        gc.setStroke(lineColor);
        gc.setLineWidth(lineWidth);
        gc.strokeLine(startCoords[0], startCoords[1], endCoords[0], endCoords[1]);
    }

}