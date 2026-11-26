package com.example.healthmate.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HealthSummaryTest {

    @Test
    public void testDailySummary() {

        List<HealthRecord> records = Arrays.asList(

                new HealthRecord(1, "2026-11-26", 2000, 100, 500),

                new HealthRecord(2, "2026-11-26", 3000, 150, 750),

                new HealthRecord(3, "2026-11-25", 4000, 200, 1000)
        );

        HealthSummary summary = HealthSummary.calculate(records, "2026-11-26", "2026-11-26");

        assertEquals(5000L, summary.getSteps());
        assertEquals(250L, summary.getCalories());
        assertEquals(1250L, summary.getWater());
    }

    @Test
    public void testSevenDaySummary() {

        List<HealthRecord> records = Arrays.asList(

                new HealthRecord(1, "2026-11-26", 2000, 100, 500),

                new HealthRecord(2, "2026-11-23", 3000, 150, 750),

                new HealthRecord(3, "2026-11-20", 4000, 200, 1000),

                new HealthRecord(4, "2026-11-19", 5000, 250, 1500),

                new HealthRecord(5, "2026-11-27", 1000, 50, 500)
        );

        HealthSummary summary = HealthSummary.calculate(records, "2026-11-20", "2026-11-26");

        assertEquals(9000L, summary.getSteps());
        assertEquals(450L, summary.getCalories());
        assertEquals(2250L, summary.getWater());
    }

    @Test
    public void testEmptySummary() {

        HealthSummary summary = HealthSummary.calculate(Collections.emptyList(),
                "2026-11-26",
                "2026-11-26"
        );

        assertEquals(0L, summary.getSteps());
        assertEquals(0L, summary.getCalories());
        assertEquals(0L, summary.getWater());
    }
}