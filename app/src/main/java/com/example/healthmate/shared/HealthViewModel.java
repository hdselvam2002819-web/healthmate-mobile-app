package com.example.healthmate.shared;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.healthmate.data.DatabaseHelper;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.model.HealthSummary;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HealthViewModel extends AndroidViewModel {

    private final DatabaseHelper database;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<HealthRecord>> records = new MutableLiveData<>();

    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);

    private final MutableLiveData<String> message = new MutableLiveData<>();

    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    public HealthViewModel(Application application) {

        super(application);

        database = new DatabaseHelper(application);

        reload();
    }

    public LiveData<List<HealthRecord>> getRecords() {
        return records;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void clearMessage() {
        message.setValue(null);
    }

    public void clearSaved() {
        saved.setValue(false);
    }

    public boolean isBusy() {
        return Boolean.TRUE.equals(busy.getValue());
    }

    public void reload() {
        perform(null, null, false);
    }

    // Filter records by the selected date
    public List<HealthRecord> filterRecords(String date) {

        List<HealthRecord> result = new ArrayList<>();

        List<HealthRecord> all = records.getValue();

        if (all != null) {

            for (HealthRecord record : all) {

                if (date == null || date.equals(record.getDate())) {

                    result.add(record);
                }
            }
        }

        return result;
    }

    // Calculate activity totals for a date range
    public HealthSummary getSummary(LocalDate start, LocalDate end) {

        return HealthSummary.calculate(filterRecords(null), start.toString(), end.toString());
    }

    // Save a new or existing record
    public void saveRecord(HealthRecord record) {

        try {

            LocalDate date = LocalDate.parse(record.getDate());

            if (date.isAfter(LocalDate.now())
                    || record.getSteps() < 0
                    || record.getCalories() < 0
                    || record.getWater() < 0) {

                message.setValue("Use a valid date and non-negative values.");

                return;
            }

        } catch (RuntimeException e) {

            message.setValue("Invalid record date.");
            return;
        }

        perform(() -> {

            if (record.getId() == 0) {

                database.addRecord(record);

            } else if (database.updateRecord(record) != 1) {

                throw new IllegalStateException("Record not found");
            }

        }, "Record saved", true);
    }

    // Delete a selected record
    public void deleteRecord(long id) {

        perform(() -> {

            if (database.deleteRecord(id) != 1) {

                throw new IllegalStateException(
                        "Record not found"
                );
            }

        }, "Record deleted", false);
    }

    // Insert sample records
    public void addSamples() {

        perform(() -> database.addSampleRecords(), "Sample records added", false);
    }

    // Execute database operations in the background
    private void perform(Runnable operation, String success, boolean returnToList) {

        if (isBusy()) {
            return;
        }

        busy.setValue(true);

        worker.execute(() -> {

            boolean changed = false;

            try {

                if (operation != null) {

                    operation.run();
                    changed = true;
                }

                List<HealthRecord> result = database.getAllRecords();

                mainThread.post(() -> {

                    records.setValue(Collections.unmodifiableList(result));

                    busy.setValue(false);

                    if (returnToList) {
                        saved.setValue(true);
                    }

                    if (success != null) {
                        message.setValue(success);
                    }
                });

            } catch (RuntimeException e) {

                Log.e("HealthMate", "Database operation failed", e);

                final boolean wasChanged = changed;

                mainThread.post(() -> {

                    busy.setValue(false);

                    if (wasChanged) {

                        records.setValue(null);

                        if (returnToList) {
                            saved.setValue(true);
                        }

                        message.setValue("Change saved, but refresh failed. Tap Retry.");

                    } else {

                        message.setValue("Unable to complete the operation. Please try again.");
                    }
                });
            }
        });
    }

    @Override
    protected void onCleared() {

        worker.execute(() -> database.close());

        worker.shutdown();

        super.onCleared();
    }
}