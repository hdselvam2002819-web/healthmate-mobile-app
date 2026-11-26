package com.example.healthmate.data;

import android.content.Context;
import android.content.SharedPreferences;

public class WaterGoalPreferences {

    private static final String PREFERENCES_NAME = "healthmate_preferences";

    private static final String WATER_GOAL_KEY = "water_goal";

    private static final int DEFAULT_GOAL = 2000;

    private final SharedPreferences preferences;

    public WaterGoalPreferences(Context context) {

        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    // Read the user's daily water goal
    public int getGoal() {

        return preferences.getInt(WATER_GOAL_KEY, DEFAULT_GOAL);
    }

    // Save new daily water goal
    public void saveGoal(int goal) {

        if (goal <= 0) {
            throw new IllegalArgumentException("Water goal must be greater than zero");
        }

        preferences.edit()
                .putInt(WATER_GOAL_KEY, goal)
                .apply();
    }
}