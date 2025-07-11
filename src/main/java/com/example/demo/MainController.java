package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load welcome screen by default
        loadScreen("home.fxml");
    }

    @FXML
    private void handleShowHome() {
        loadScreen("home.fxml");
    }

    @FXML
    private void handleShowDemo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sim.fxml"));
            Node screen = loader.load();
            SimController simController = loader.getController();

            double sunMass = 1.989e30;
            double earthMass = 5.972e24;
            //double earthDist = Units.distUnitsToSimUnits(1.496e11); // 1 AU
            double earthVel = 29780;

            simController.planets.add(new Body(50, sunMass, new double[]{0.0, 0.0}, Color.ORANGE));
            simController.planets.add(new Body(10, earthMass, new double[]{1.0, 0.0}, Color.BLUE, new double[]{0.0, 29780}));

            contentArea.getChildren().setAll(screen);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowBlank() {
        loadScreen("sim.fxml");
    }

    private List<List<String>> parseSimLoadFile(File selectedFile) throws FileNotFoundException {
        List<List<String>> loadData = new ArrayList<>();

        String fileName = selectedFile.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, selectedFile.getName().length());

        if (fileExtension.equals("csv")) {
            Scanner scanner = new Scanner(selectedFile);
            scanner.useDelimiter(",");

            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(",");
                List<String> planet = new ArrayList<>();
                for (String value : values) {
                    planet.add(value);
                }
                loadData.add(planet);
            }
            scanner.close();
        }

        return loadData;
    }

    @FXML
    private void handleLoadSimulation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sim.fxml"));
            Node screen = loader.load();

            List<List<String>> planetData = new ArrayList<>();

            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                try {
                    planetData = parseSimLoadFile(selectedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            SimController simController = loader.getController();
            for (List<String> planet : planetData) {
                simController.planets.add(new Body(
                        Integer.parseInt(planet.get(0)),
                        Double.parseDouble(planet.get(1)),
                        new double[]{ Double.parseDouble(planet.get(2)), Double.parseDouble(planet.get(3))},
                        Color.valueOf(planet.get(4).substring(planet.get(4).indexOf(".") + 1)),
                        new double[]{ Double.parseDouble(planet.get(5)),Double.parseDouble(planet.get(6))}
                ));
            }

            contentArea.getChildren().setAll(screen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Realtime Gravity Simulation");
        alert.setContentText("Version 1.0\nMade with JavaFX");
        alert.showAndWait();
    }

    private void loadScreen(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node screen = loader.load();
            contentArea.getChildren().setAll(screen);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
