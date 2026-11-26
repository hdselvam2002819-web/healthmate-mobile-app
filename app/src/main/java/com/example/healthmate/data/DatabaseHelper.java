package com.example.healthmate.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.healthmate.model.HealthRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "healthmate.db";
    private static final String TABLE_NAME = "health_records";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE health_records ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "date TEXT NOT NULL, "
                + "steps INTEGER NOT NULL CHECK(steps >= 0), "
                + "calories INTEGER NOT NULL CHECK(calories >= 0), "
                + "water INTEGER NOT NULL CHECK(water >= 0))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        throw new SQLiteException(
                "Database migration not implemented"
        );
    }

    private ContentValues getValues(HealthRecord record) {

        ContentValues values = new ContentValues();

        values.put("date", record.getDate());
        values.put("steps", record.getSteps());
        values.put("calories", record.getCalories());
        values.put("water", record.getWater());

        return values;
    }

    // Create a new health record
    public long addRecord(HealthRecord record) {

        SQLiteDatabase db = getWritableDatabase();

        return db.insertOrThrow(
                TABLE_NAME, null, getValues(record)
        );
    }

    // Read all health records
    public List<HealthRecord> getAllRecords() {

        List<HealthRecord> records = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_NAME, null, null, null,
                null, null, "date DESC, id DESC")) {

            while (cursor.moveToNext()) {

                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));

                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));

                int calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));

                int water = cursor.getInt(cursor.getColumnIndexOrThrow("water"));

                records.add(new HealthRecord(id, date, steps, calories, water));
            }
        }

        return records;
    }

    // Update an existing health record
    public int updateRecord(HealthRecord record) {

        SQLiteDatabase db = getWritableDatabase();

        return db.update(TABLE_NAME, getValues(record), "id = ?",
                new String[]{String.valueOf(record.getId())}
        );
    }

    // Delete a health record
    public int deleteRecord(long id) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)}
        );
    }

    // Add sample records for testing
    public void addSampleRecords() {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            if (!getAllRecords().isEmpty()) {
                throw new IllegalStateException("Sample records require an empty database.");
            }

            LocalDate today = LocalDate.now();

            addRecord(new HealthRecord(0, today.toString(), 4500, 230, 900));

            addRecord(new HealthRecord(0, today.minusDays(1).toString(),
                    6200, 310, 1600
            ));

            addRecord(new HealthRecord(0, today.minusDays(2).toString(),
                    7000, 350, 1800
            ));

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }
}