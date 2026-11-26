package com.example.healthmate.records;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.core.widget.TextViewCompat;

import com.example.healthmate.R;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.shared.ScreenUi;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class RecordFormScreen {

    private final Context context;

    private final HealthRecord existingRecord;

    private final LocalDate initialDate;

    private LocalDate selectedDate;

    private EditText stepsInput;
    private EditText caloriesInput;
    private EditText waterInput;

    private MaterialButton dateButton;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;


    public RecordFormScreen(Context context, LocalDate date, HealthRecord existingRecord) {

        this.context = context;
        this.existingRecord = existingRecord;

        initialDate = date;
        selectedDate = date;
    }


    // Create the Add or Edit Record form
    public ScrollView create(Consumer<HealthRecord> onSave, Runnable onCancel) {

        boolean editing = existingRecord != null;

        ScrollView scrollView = new ScrollView(context);

        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout content = ScreenUi.column(context);

        content.setPadding(ScreenUi.dp(context, 16), ScreenUi.dp(context, 32),
                ScreenUi.dp(context, 16), ScreenUi.dp(context, 24)
        );

        scrollView.addView(content);

        // Form heading
        content.addView(ScreenUi.text(context, context.getString(
                        editing
                                ? R.string.form_edit_record
                                : R.string.form_add_record
                ),
                24, ScreenUi.INK, true
        ));

        addSpace(content, 8);

        content.addView(ScreenUi.text(context, context.getString(
                        editing
                                ? R.string.form_edit_subtitle
                                : R.string.form_add_subtitle
                ),
                14, ScreenUi.MUTED, false
        ));

        addSpace(content, 28);

        // Date selection
        addLabel(content, R.string.form_date);

        dateButton = ScreenUi.button(context, formatDate(selectedDate), false);

        dateButton.setIconResource(R.drawable.ic_calendar);

        dateButton.setIconGravity(MaterialButton.ICON_GRAVITY_END);

        dateButton.setIconTint(ColorStateList.valueOf(ScreenUi.MUTED));

        dateButton.setMinHeight(ScreenUi.dp(context, 56));

        dateButton.setOnClickListener(view -> showDatePicker());

        content.addView(dateButton);

        addSpace(content, 24);

        // Steps input
        stepsInput = addNumberField(content, R.string.form_steps,
                R.string.form_steps_hint, R.drawable.ic_steps, ScreenUi.GREEN
        );

        addSpace(content, 24);

        // Calories input
        caloriesInput = addNumberField(content, R.string.form_calories,
                R.string.form_calories_hint, R.drawable.ic_calories, ScreenUi.RED
        );

        addSpace(content, 24);

        // Water intake input
        waterInput = addNumberField(content, R.string.form_water,
                R.string.form_water_hint, R.drawable.ic_water, ScreenUi.BLUE
        );

        // Fill existing values when editing
        if (editing) {

            stepsInput.setText(String.valueOf(existingRecord.getSteps()));

            caloriesInput.setText(String.valueOf(existingRecord.getCalories()));

            waterInput.setText(String.valueOf(existingRecord.getWater()));
        }

        addSpace(content, 28);

        // Save or Update button
        saveButton = ScreenUi.button(context, context.getString(
                        editing
                                ? R.string.form_update
                                : R.string.form_save
                ),
                true
        );

        saveButton.setOnClickListener(view -> submitForm(onSave));

        content.addView(saveButton);

        addSpace(content, 12);

        // Cancel button
        cancelButton = ScreenUi.button(context, context.getString(R.string.form_cancel),
                false
        );

        cancelButton.setOnClickListener(view -> onCancel.run());

        content.addView(cancelButton);

        // Do not automatically open the keyboard
        content.setFocusableInTouchMode(true);
        content.requestFocus();

        return scrollView;
    }


    // Display a field label
    private void addLabel(
            LinearLayout content, int label) {

        content.addView(ScreenUi.text(context, context.getString(label),
                15, ScreenUi.INK, true
        ));

        addSpace(content, 8);
    }


    // Create a numeric input field
    private EditText addNumberField(LinearLayout content, int label, int hint,
            int icon, int iconColour) {

        addLabel(content, label);

        EditText input = new EditText(context);

        input.setSingleLine(true);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        input.setHint(hint);

        input.setTextSize(16);

        input.setTextColor(ScreenUi.INK);

        input.setHintTextColor(ScreenUi.MUTED);

        input.setContentDescription(context.getString(label));

        input.setMinHeight(ScreenUi.dp(context, 56));

        int padding = ScreenUi.dp(context, 14);

        input.setPadding(padding, padding, padding, padding);

        GradientDrawable background = ScreenUi.roundedBackground(context, ScreenUi.WHITE, 12);

        background.setStroke(ScreenUi.dp(context, 1), 0xFFB7CBC0);

        input.setBackground(background);

        input.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, icon, 0
        );

        TextViewCompat.setCompoundDrawableTintList(input, ColorStateList.valueOf(iconColour));

        input.setCompoundDrawablePadding(ScreenUi.dp(context, 12));

        content.addView(input);

        return input;
    }


    // Open the calendar date picker
    private void showDatePicker() {

        DatePickerDialog dialog = new DatePickerDialog(context,

                (view, year, month, day) -> {

                    selectedDate = LocalDate.of(year, month + 1, day);

                    dateButton.setText(formatDate(selectedDate));
                },

                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth()
        );

        // Prevent selecting future dates
        long maxDate = LocalDate.now()
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        dialog.getDatePicker().setMaxDate(maxDate);

        dialog.show();
    }


    // Validate and submit the record
    private void submitForm(
            Consumer<HealthRecord> onSave) {

        if (selectedDate.isAfter(LocalDate.now())) {

            Toast.makeText(context, R.string.form_future_date, Toast.LENGTH_SHORT).show();

            return;
        }

        Integer steps = readNumber(stepsInput);
        Integer calories = readNumber(caloriesInput);
        Integer water = readNumber(waterInput);

        if (steps == null
                || calories == null
                || water == null) {

            return;
        }

        long id = existingRecord == null
                ? 0
                : existingRecord.getId();

        HealthRecord record = new HealthRecord(id, selectedDate.toString(), steps, calories, water);

        onSave.accept(record);
    }


    // Read and validate a numeric field
    private Integer readNumber(EditText input) {

        String value = input.getText().toString().trim();

        if (value.isEmpty()) {

            input.setError(context.getString(R.string.form_required));

            return null;
        }

        try {

            int number = Integer.parseInt(value);

            if (number < 0) {

                input.setError(context.getString(R.string.form_invalid_number));

                return null;
            }

            input.setError(null);

            return number;

        } catch (NumberFormatException e) {

            input.setError(context.getString(R.string.form_invalid_number));

            return null;
        }
    }


    // Check whether the user changed the form
    public boolean hasUnsavedChanges() {

        if (!selectedDate.equals(initialDate)) {

            return true;
        }

        String originalSteps = existingRecord == null
                ? ""
                : String.valueOf(existingRecord.getSteps());

        String originalCalories = existingRecord == null
                ? ""
                : String.valueOf(existingRecord.getCalories());

        String originalWater = existingRecord == null
                ? ""
                : String.valueOf(existingRecord.getWater());

        return !stepsInput.getText().toString()
                .equals(originalSteps)

                || !caloriesInput.getText().toString()
                .equals(originalCalories)

                || !waterInput.getText().toString()
                .equals(originalWater);
    }


    // Prevent repeated submissions while saving
    public void setBusy(boolean busy) {

        dateButton.setEnabled(!busy);

        stepsInput.setEnabled(!busy);
        caloriesInput.setEnabled(!busy);
        waterInput.setEnabled(!busy);

        saveButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);

        saveButton.setText(
                busy
                        ? R.string.form_saving
                        : existingRecord == null
                        ? R.string.form_save
                        : R.string.form_update
        );
    }


    // Retrieve the selected date
    public LocalDate getSelectedDate() {

        return selectedDate;
    }


    // Preserve form data during screen recreation
    public Bundle saveDraft() {

        Bundle draft = new Bundle();

        draft.putString("initialDate", initialDate.toString());

        draft.putString("selectedDate", selectedDate.toString());

        draft.putLong("recordId", existingRecord == null ? 0 : existingRecord.getId());

        if (existingRecord != null) {

            draft.putInt("originalSteps", existingRecord.getSteps());

            draft.putInt("originalCalories", existingRecord.getCalories());

            draft.putInt("originalWater", existingRecord.getWater());
        }

        draft.putString("steps", stepsInput.getText().toString());

        draft.putString("calories", caloriesInput.getText().toString());

        draft.putString("water", waterInput.getText().toString());

        return draft;
    }


    // Restore unfinished form input
    public void restoreDraft(Bundle draft) {

        selectedDate = LocalDate.parse(draft.getString("selectedDate", initialDate.toString()));

        dateButton.setText(formatDate(selectedDate));

        stepsInput.setText(draft.getString("steps", ""));

        caloriesInput.setText(draft.getString("calories", ""));

        waterInput.setText(draft.getString("water", ""));
    }


    // Format the selected date
    private String formatDate(LocalDate date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

        return date.format(formatter);
    }


    // Add vertical spacing
    private void addSpace(LinearLayout layout, int height) {

        android.view.View space = new android.view.View(context);

        layout.addView(space, new LinearLayout.LayoutParams(1, ScreenUi.dp(context, height))
        );
    }
}