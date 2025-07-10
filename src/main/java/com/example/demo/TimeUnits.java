package com.example.demo;

public class TimeUnits {

    // convert time abstraction to seconds per real-time second
    public static double getSecondsPerFrame(double unitMultiplier) {
        return unitMultiplier; // already in seconds
    }

    public static final double SECONDS_PER_SECOND = 1;
    public static final double SECONDS_PER_HOUR = 3600;
    public static final double SECONDS_PER_DAY = 86400;
    public static final double SECONDS_PER_WEEK = 604800;
    public static final double SECONDS_PER_MONTH = 2.628e+6;
    public static final double SECONDS_PER_YEAR = 3.154e7;

}
