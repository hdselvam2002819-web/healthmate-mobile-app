package com.example.healthmate.model;

public class HealthRecord {

    private final long id;
    private final String date;
    private final int steps;
    private final int calories;
    private final int water;

    public HealthRecord(long id, String date, int steps,
                        int calories, int water) {

        this.id = id;
        this.date = date;
        this.steps = steps;
        this.calories = calories;
        this.water = water;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getSteps() {
        return steps;
    }

    public int getCalories() {
        return calories;
    }

    public int getWater() {
        return water;
    }
}