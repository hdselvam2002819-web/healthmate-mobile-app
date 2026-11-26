package com.example.healthmate.data;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WaterGoalPreferencesTest {

    private Context context;
    private WaterGoalPreferences preferences;

    @Before
    public void setUp() {

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        clearPreferences();

        preferences = new WaterGoalPreferences(context);
    }

    @After
    public void tearDown() {

        clearPreferences();
    }

    private void clearPreferences() {

        context.getSharedPreferences("healthmate_preferences",
                Context.MODE_PRIVATE
        ).edit().clear().commit();
    }

    @Test
    public void testDefaultGoal() {

        assertEquals(2000, preferences.getGoal());
    }

    @Test
    public void testSaveGoal() {

        preferences.saveGoal(2500);

        assertEquals(2500, preferences.getGoal());
    }

    @Test
    public void testGoalPersistence() {

        preferences.saveGoal(1800);

        WaterGoalPreferences newPreferences = new WaterGoalPreferences(context);

        assertEquals(1800, newPreferences.getGoal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGoal() {

        preferences.saveGoal(0);
    }
}