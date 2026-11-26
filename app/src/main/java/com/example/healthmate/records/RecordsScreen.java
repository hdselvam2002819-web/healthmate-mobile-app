package com.example.healthmate.records;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.healthmate.R;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.shared.HealthViewModel;
import com.example.healthmate.shared.ScreenUi;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class RecordsScreen {

    private final Context context;
    private final HealthViewModel viewModel;

    private LocalDate calendarDate;
    private boolean showAll;

    public RecordsScreen(Context context, HealthViewModel viewModel) {

        this.context = context;
        this.viewModel = viewModel;
    }


    // Create the Health Records screen
    public ScrollView create(
            boolean history,
            LocalDate selectedDate,
            Consumer<Boolean> onTabChanged,
            Consumer<LocalDate> onDateChanged,
            Consumer<LocalDate> onAdd,
            Consumer<HealthRecord> onEdit) {

        ScrollView scrollView = new ScrollView(context);

        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout content = ScreenUi.column(context);

        content.setClipChildren(false);
        content.setClipToPadding(false);

        content.setPadding(ScreenUi.dp(context, 16), ScreenUi.dp(context, 32),
                ScreenUi.dp(context, 16), ScreenUi.dp(context, 24)
        );

        scrollView.addView(content);

        // Screen heading
        content.addView(ScreenUi.text(context, "Health Records",
                24, ScreenUi.INK, true
        ));

        addSpace(content, 8);

        content.addView(ScreenUi.text(context, "Track your progress, one day at a time",
                14, ScreenUi.MUTED, false
        ));

        addSpace(content, 24);

        // Add Record button above the Today tab
        if (!history) {

            LocalDate today = LocalDate.now();

            content.addView(createAddButton(today, true, onAdd));

            addSpace(content, 24);
        }

        // Today and History tabs
        addTabs(content, history, onTabChanged);

        addSpace(content, 24);

        LinearLayout recordArea = ScreenUi.column(context);

        recordArea.setClipChildren(false);
        recordArea.setClipToPadding(false);

        if (history) {

            showAll = selectedDate == null;

            calendarDate = showAll
                    ? LocalDate.now()
                    : selectedDate;

            if (calendarDate.isAfter(LocalDate.now())) {
                calendarDate = LocalDate.now();
            }

            addCalendar(content, recordArea, onDateChanged, onAdd, onEdit);

            addSpace(content, 24);

        } else {

            calendarDate = LocalDate.now();
            showAll = false;
        }

        content.addView(recordArea);

        showRecords(recordArea, showAll ? null : calendarDate, history, onAdd, onEdit);

        return scrollView;
    }


    // Create the Today and History tabs
    private void addTabs(LinearLayout content, boolean history, Consumer<Boolean> onTabChanged) {

        LinearLayout tabs = ScreenUi.row(context);

        MaterialButton todayButton = createTabButton("Today", !history);

        MaterialButton historyButton = createTabButton("History", history);

        LinearLayout.LayoutParams todayParams = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1
                );

        todayParams.rightMargin = ScreenUi.dp(context, 12);

        tabs.addView(todayButton, todayParams);

        tabs.addView(historyButton, new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );

        todayButton.setOnClickListener(view -> onTabChanged.accept(false));

        historyButton.setOnClickListener(view -> onTabChanged.accept(true));

        content.addView(tabs);

        addDivider(content);
    }


    // Create a selected or unselected tab
    private MaterialButton createTabButton(String label, boolean selected) {

        MaterialButton button = ScreenUi.button(context, label, true);

        button.setCornerRadius(ScreenUi.dp(context, 24));

        button.setBackgroundTintList(ColorStateList.valueOf(selected
                                ? ScreenUi.BAR_GREEN
                                : ScreenUi.BACKGROUND
                )
        );

        button.setTextColor(
                selected ? ScreenUi.INK : ScreenUi.MUTED
        );

        button.setSelected(selected);

        return button;
    }


    // Create the Add Record button
    private MaterialButton createAddButton(LocalDate date, boolean enabled,
            Consumer<LocalDate> onAdd) {

        MaterialButton button = ScreenUi.button(context, "Add Record", true);

        button.setIconResource(R.drawable.ic_add);

        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);

        button.setIconTint(ColorStateList.valueOf(ScreenUi.WHITE));

        button.setEnabled(enabled);

        if (!enabled) {

            button.setBackgroundTintList(ColorStateList.valueOf(0xFFE0E5E2));

            button.setTextColor(ScreenUi.MUTED);

            button.setIconTint(ColorStateList.valueOf(ScreenUi.MUTED));
        }

        button.setOnClickListener(view -> onAdd.accept(date));

        return button;
    }


    // Display the calendar
    private void addCalendar(LinearLayout content, LinearLayout recordArea,
            Consumer<LocalDate> onDateChanged, Consumer<LocalDate> onAdd,
            Consumer<HealthRecord> onEdit) {

        LinearLayout calendarCard = ScreenUi.card(context);

        int padding = ScreenUi.dp(context, 8);

        calendarCard.setPadding(padding, padding, padding, padding);

        CalendarView calendar = new CalendarView(context);

        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // Prevent future dates
        calendar.setMaxDate(System.currentTimeMillis());

        long selectedTime = calendarDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        calendar.setDate(selectedTime, false, true);

        calendarCard.addView(
                calendar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        content.addView(calendarCard);

        addSpace(content, 12);

        MaterialButton viewAllButton = ScreenUi.button(
                context,
                context.getString(
                        showAll
                                ? R.string.show_selected_date
                                : R.string.view_all_records
                ),
                false
        );

        content.addView(viewAllButton);

        // Filter records when selecting a date
        calendar.setOnDateChangeListener(
                (view, year, month, day) -> {

                    calendarDate = LocalDate.of(year, month + 1, day);

                    showAll = false;

                    onDateChanged.accept(calendarDate);

                    viewAllButton.setText(R.string.view_all_records);

                    showRecords(recordArea, calendarDate, true, onAdd, onEdit);
                }
        );

        // View the complete record history
        viewAllButton.setOnClickListener(view -> {

            showAll = !showAll;

            LocalDate date = showAll ? null : calendarDate;

            onDateChanged.accept(date);

            viewAllButton.setText(
                    showAll
                            ? R.string.show_selected_date
                            : R.string.view_all_records
            );

            showRecords(recordArea, date, true, onAdd, onEdit);
        });
    }


    // Display records for the selected date
    private void showRecords(LinearLayout area, LocalDate date, boolean history,
            Consumer<LocalDate> onAdd, Consumer<HealthRecord> onEdit) {

        area.removeAllViews();

        List<HealthRecord> records = viewModel.filterRecords(date == null ? null : date.toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        "EEEE, dd MMMM yyyy", Locale.ENGLISH
                );

        String heading = date == null ? "All Health Records" : date.format(formatter);

        area.addView(ScreenUi.text(context, heading, 15, ScreenUi.INK, false));

        addSpace(area, 16);

        if (records.isEmpty()) {

            showEmptyState(area, date, history, onAdd);

            return;
        }

        for (HealthRecord record : records) {

            if (date == null) {

                String recordDate = LocalDate.parse(record.getDate()).format(formatter);

                area.addView(ScreenUi.text(context, recordDate, 15, ScreenUi.INK, true));

                addSpace(area, 10);
            }

            area.addView(createRecordCard(record, onEdit));

            addSpace(area, 24);
        }

        if (!history && date != null) {

            TextView message = ScreenUi.text(context, "Today's record already exists. "
                            + "Edit it to update your activities.",
                    13, ScreenUi.MUTED, false
            );

            message.setGravity(Gravity.CENTER);

            area.addView(message);
        }
    }


    // Display an empty record state
    private void showEmptyState(LinearLayout area, LocalDate date,
            boolean history, Consumer<LocalDate> onAdd) {

        LinearLayout card = ScreenUi.card(context);

        card.setGravity(Gravity.CENTER);

        card.addView(createIcon(R.drawable.ic_calendar, ScreenUi.MUTED, 40));

        addSpace(card, 16);

        TextView title = ScreenUi.text(context, "No health records",
                18, ScreenUi.INK, true
        );

        title.setGravity(Gravity.CENTER);

        card.addView(title);

        addSpace(card, 8);

        String description = date == null
                ? "Select a date to add your first record."
                : "No activities have been recorded for this date.";

        TextView message = ScreenUi.text(context, description, 14, ScreenUi.MUTED, false);

        message.setGravity(Gravity.CENTER);

        card.addView(message);

        area.addView(card);

        // History tab Add Record button
        if (history && date != null) {

            addSpace(area, 20);

            area.addView(createAddButton(date, true, onAdd));
        }
    }


    // Create an individual record card
    private LinearLayout createRecordCard(HealthRecord record, Consumer<HealthRecord> onEdit) {

        LinearLayout card = ScreenUi.card(context);

        LinearLayout topRow = ScreenUi.row(context);

        topRow.addView(
                createActivity("Steps walked",
                        formatNumber(record.getSteps()) + " steps",
                        R.drawable.ic_steps, ScreenUi.GREEN
                ),
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        topRow.addView(
                createActivity("Calories burned",
                        formatNumber(record.getCalories()) + " kcal",
                        R.drawable.ic_calories, ScreenUi.RED
                ),
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        card.addView(topRow);

        addSpace(card, 24);

        card.addView(createActivity("Water intake",
                formatNumber(record.getWater()) + " ml", R.drawable.ic_water, ScreenUi.BLUE
        ));

        addSpace(card, 24);

        addDivider(card);

        addSpace(card, 12);

        // Edit and Delete actions
        LinearLayout actions = ScreenUi.row(context);

        MaterialButton editButton = createActionButton("Edit", R.drawable.ic_edit, ScreenUi.GREEN);

        MaterialButton deleteButton = createActionButton(
                "Delete", R.drawable.ic_delete, ScreenUi.RED
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1
                );

        editParams.rightMargin = ScreenUi.dp(context, 12);

        actions.addView(editButton, editParams);

        actions.addView(deleteButton, new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        editButton.setOnClickListener(view -> onEdit.accept(record));

        deleteButton.setOnClickListener(view -> confirmDelete(record));

        card.addView(actions);

        return card;
    }


    // Create an activity item
    private LinearLayout createActivity(String label, String value, int resource, int colour) {

        LinearLayout item = ScreenUi.column(context);

        item.setGravity(Gravity.CENTER);

        item.addView(createIcon(resource, colour, 28));

        addSpace(item, 8);

        TextView labelText = ScreenUi.text(context, label, 13, colour, false);

        labelText.setGravity(Gravity.CENTER);

        item.addView(labelText);

        addSpace(item, 4);

        TextView valueText = ScreenUi.text(context, value, 17, colour, true);

        valueText.setGravity(Gravity.CENTER);

        item.addView(valueText);

        return item;
    }


    // Create Edit and Delete buttons
    private MaterialButton createActionButton(String label, int icon, int colour) {

        MaterialButton button = ScreenUi.button(context, label, false);

        button.setStrokeWidth(0);

        button.setTextColor(colour);

        button.setIconResource(icon);

        button.setIconTint(ColorStateList.valueOf(colour));

        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);

        return button;
    }


    // Confirm before deleting a record
    private void confirmDelete(HealthRecord record) {

        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Record?")
                .setMessage(
                        "Are you sure you want to delete this "
                                + "health record? "
                                + "This action cannot be undone."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) ->
                        viewModel.deleteRecord(record.getId())
                )
                .show();
    }


    // Create a colored icon
    private ImageView createIcon(
            int resource, int colour, int size) {

        ImageView icon = new ImageView(context);

        icon.setImageResource(resource);
        icon.setColorFilter(colour);

        icon.setLayoutParams(new LinearLayout.LayoutParams(
                        ScreenUi.dp(context, size), ScreenUi.dp(context, size)
                )
        );

        return icon;
    }


    // Create a horizontal divider
    private void addDivider(LinearLayout layout) {

        View divider = new View(context);

        divider.setBackgroundColor(0xFFDCE5DF);

        layout.addView(
                divider,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ScreenUi.dp(context, 1)
                )
        );
    }


    // Add vertical spacing
    private void addSpace(LinearLayout layout, int height) {

        View space = new View(context);

        layout.addView(space, new LinearLayout.LayoutParams(1, ScreenUi.dp(context, height)));
    }


    // Format activity values
    private String formatNumber(long value) {

        return String.format(Locale.ENGLISH, "%,d", value);
    }
}