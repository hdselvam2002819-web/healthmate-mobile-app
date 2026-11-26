package com.example.healthmate.model;

import java.util.List;

public class HealthSummary {

    private long steps;
    private long calories;
    private long water;

    public static HealthSummary calculate(
            List<HealthRecord> records,
            String from, String to) {

        HealthSummary summary = new HealthSummary();

        for (HealthRecord record : records) {

            String date = record.getDate();

            if (date.compareTo(from) >= 0
                    && date.compareTo(to) <= 0) {

                summary.steps += record.getSteps();
                summary.calories += record.getCalories();
                summary.water += record.getWater();
            }
        }

        return summary;
    }

    public long getSteps() {
        return steps;
    }

    public long getCalories() {
        return calories;
    }

    public long getWater() {
        return water;
    }
}