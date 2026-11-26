package com.example.healthmate.data;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.healthmate.model.HealthRecord;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {

    private Context context;
    private DatabaseHelper database;

    private final String databaseName = "healthmate_test.db";
    private final String today = LocalDate.now().toString();

    @Before
    public void setUp() {

        context = InstrumentationRegistry
                .getInstrumentation().getTargetContext();

        context.deleteDatabase(databaseName);

        database = new DatabaseHelper(context, databaseName);
    }

    @After
    public void tearDown() {

        database.close();
        context.deleteDatabase(databaseName);
    }

    @Test
    public void testAddAndReadRecord() {

        HealthRecord record = new HealthRecord(
                0, today, 5000, 250, 1500
        );

        long id = database.addRecord(record);

        assertTrue(id > 0);
        assertEquals(1, database.getAllRecords().size());

        HealthRecord savedRecord =
                database.getAllRecords().get(0);

        assertEquals(id, savedRecord.getId());
        assertEquals(today, savedRecord.getDate());
        assertEquals(5000, savedRecord.getSteps());
        assertEquals(250, savedRecord.getCalories());
        assertEquals(1500, savedRecord.getWater());
    }

    @Test
    public void testUpdateRecord() {

        long id = database.addRecord(
                new HealthRecord(0, today, 5000, 250, 1500)
        );

        HealthRecord updatedRecord = new HealthRecord(
                id, today, 6000, 300, 2000
        );

        assertEquals(1, database.updateRecord(updatedRecord));

        HealthRecord savedRecord =
                database.getAllRecords().get(0);

        assertEquals(6000, savedRecord.getSteps());
        assertEquals(300, savedRecord.getCalories());
        assertEquals(2000, savedRecord.getWater());
    }

    @Test
    public void testDeleteRecord() {

        long id = database.addRecord(
                new HealthRecord(0, today, 5000, 250, 1500)
        );

        assertEquals(1, database.deleteRecord(id));
        assertTrue(database.getAllRecords().isEmpty());
    }

    @Test
    public void testSampleRecords() {

        database.addSampleRecords();

        assertEquals(3, database.getAllRecords().size());
    }

    @Test
    public void testDataPersistence() {

        database.addRecord(new HealthRecord(0, today, 5000, 250, 1500));

        database.close();

        database = new DatabaseHelper(context, databaseName);

        assertEquals(1, database.getAllRecords().size());
        assertEquals(1500,
                database.getAllRecords().get(0).getWater());
    }

    @Test(expected = SQLiteConstraintException.class)
    public void testNegativeValue() {

        database.addRecord(new HealthRecord(0, today, -100, 250, 1500));
    }
}