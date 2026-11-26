package com.example.healthmate.data;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class WaterGoalPreferencesTest {

    private Context context;

    private String testPreferencesName;

    private WaterGoalPreferences preferences;


    @Before
    public void setUp() {

        context = ApplicationProvider.getApplicationContext();

        testPreferencesName = "healthmate_test_" + UUID.randomUUID();

        preferences = new WaterGoalPreferences(context, testPreferencesName);
    }


    @After
    public void tearDown() {

        context.deleteSharedPreferences(testPreferencesName);
    }


    // Check the default water goal
    @Test
    public void defaultWaterGoalIs2000() {

        assertEquals(2000, preferences.getGoal());
    }


    // Check that the goal can be customised
    @Test
    public void canSaveCustomWaterGoal() {

        preferences.saveGoal(2500);

        assertEquals(2500, preferences.getGoal());
    }


    // Check that the saved goal is retained
    @Test
    public void waterGoalIsRetained() {

        preferences.saveGoal(3000);

        WaterGoalPreferences reopenedPreferences = new WaterGoalPreferences(
                        context, testPreferencesName
                );

        assertEquals(3000, reopenedPreferences.getGoal());
    }


    // Check that invalid goals are rejected
    @Test(expected = IllegalArgumentException.class)
    public void invalidWaterGoalIsRejected() {

        preferences.saveGoal(0);
    }
}