package com.example.demo;

public class Units {
    // gravitational constant
    public static final double G = 6.67430e-11;

    // conversion factors
    public static final double AU_IN_METERS = 1.496e11;
    public static final double KM_IN_METERS = 1e3;

    // distance unit used for simulation space, e.g. AU or KM
    public static double SIM_DISTANCE_UNIT = AU_IN_METERS;

    // helper methods for converting sim units to SI units, e.g. m
    public static double simUnitsToDistUnits(double simDist) {
        return simDist * SIM_DISTANCE_UNIT;
    }
    public static double distUnitsToSimUnits(double dist) {
        return dist / SIM_DISTANCE_UNIT;
    }
}
