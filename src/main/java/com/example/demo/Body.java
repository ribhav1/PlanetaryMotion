package com.example.demo;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.UUID;

class Body {
    int radius;
    double mass;
    double[] transform = {0.0, 0.0};
    double[] velocity = {0.0, 0.0};
    double[] acceleration = {0.0, 0.0};
    Color color;
    double creationTime = 0.0;
    String uniqueID = UUID.randomUUID().toString();

    double forceG = 0;

    public Body(int r, double m, double[] position, Color c) {
        radius = r;
        mass = m;
        transform = position.clone();
        color = c;
    }

    public Body(int r, double m, double[] position, Color c, double[] initialVelocity) {
        this(r, m, position, c);
        //this.velocity = initialVelocity.clone();
        this.velocity[0] = Units.distUnitsToSimUnits(initialVelocity[0]);
        this.velocity[1] = Units.distUnitsToSimUnits(initialVelocity[1]);
    }

    public static void updateTransform(ArrayList<Body> planets, double deltaTime) {
        for (int i = 0; i < planets.size(); i++) {
            Body thisPlanet = planets.get(i);
            double[] force = {0.0, 0.0};

            for (int j = 0; j < planets.size(); j++) {
                if (i == j) continue;
                Body otherPlanet = planets.get(j);

                double dx = Units.simUnitsToDistUnits(otherPlanet.transform[0] - thisPlanet.transform[0]);
                double dy = Units.simUnitsToDistUnits(otherPlanet.transform[1] - thisPlanet.transform[1]);
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance == 0) continue; // Avoid singularity

                double gForce = (Units.G * otherPlanet.mass * thisPlanet.mass) / (distance * distance);
                double angle = Math.atan2(dy, dx);

                thisPlanet.forceG = gForce;

                force[0] += gForce * Math.cos(angle);
                force[1] += gForce * Math.sin(angle);
            }

            // Update acceleration
            thisPlanet.acceleration[0] = force[0] / thisPlanet.mass;
            thisPlanet.acceleration[1] = force[1] / thisPlanet.mass;

            // Update velocity
            thisPlanet.velocity[0] += Units.distUnitsToSimUnits(thisPlanet.acceleration[0]) * deltaTime;
            thisPlanet.velocity[1] += Units.distUnitsToSimUnits(thisPlanet.acceleration[1]) * deltaTime;

            // Update position
            thisPlanet.transform[0] += thisPlanet.velocity[0] * deltaTime;
            thisPlanet.transform[1] += thisPlanet.velocity[1] * deltaTime;
        }
    }
}
