package com.example.healthmate.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.healthmate.R;
import com.example.healthmate.model.HealthSummary;
import com.example.healthmate.shared.ScreenUi;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.IntConsumer;

public class DashboardScreen {

    private final Context context;

    public DashboardScreen(Context context) {
        this.context = context;
    }

    public ScrollView create(HealthSummary today, HealthSummary lastSevenDays, int waterGoal,
                             IntConsumer onGoalChanged) {

        ScrollView scrollView = new ScrollView(context);

        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout content = ScreenUi.column(context);

        content.setClipChildren(false);
        content.setClipToPadding(false);

        int padding = ScreenUi.dp(context, 16);

        content.setPadding(padding, ScreenUi.dp(context, 32),
                padding, ScreenUi.dp(context, 24)
        );
        scrollView.addView(content);

        // Greeting and current date
        addGreeting(content);

        addSpace(content, 36);

        // Today's activity heading
        content.addView(ScreenUi.text(context, "Today's Activity",
                20, ScreenUi.INK, true
        ));

        addSpace(content, 16);

        // Steps and calories cards
        LinearLayout activityRow = ScreenUi.row(context);

        activityRow.setClipChildren(false);
        activityRow.setClipToPadding(false);

        LinearLayout stepsCard = createActivityCard("Steps walked",
                formatNumber(today.getSteps()) + " steps",
                R.drawable.ic_steps,
                ScreenUi.GREEN
        );

        LinearLayout caloriesCard = createActivityCard("Calories burned",
                formatNumber(today.getCalories()) + " kcal",
                R.drawable.ic_calories,
                ScreenUi.RED
        );

        LinearLayout.LayoutParams stepsParams =
                new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                );

        stepsParams.rightMargin = ScreenUi.dp(context, 12);

        activityRow.addView(stepsCard, stepsParams);

        activityRow.addView(caloriesCard, new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        content.addView(activityRow);

        addSpace(content, 20);

        // Water intake and customizable goal
        content.addView(createWaterCard(today.getWater(), waterGoal, onGoalChanged));

        addSpace(content, 40);

        // Seven-day health statistics
        addHealthStats(content, lastSevenDays);

        return scrollView;
    }


    // Create the greeting section
    private void addGreeting(LinearLayout content) {

        int hour = LocalTime.now().getHour();

        String greeting;
        int icon;
        int iconColour;

        if (hour < 12) {

            greeting = "Hello! Good Morning";
            icon = R.drawable.ic_sun;
            iconColour = 0xFFE89B19;

        } else if (hour < 17) {

            greeting = "Hello! Good Afternoon";
            icon = R.drawable.ic_sun;
            iconColour = 0xFFE89B19;

        } else {

            greeting = "Hello! Good Evening";
            icon = R.drawable.ic_moon;
            iconColour = ScreenUi.MUTED;
        }

        LinearLayout greetingRow = ScreenUi.row(context);

        TextView greetingText = ScreenUi.text(context, greeting, 23, ScreenUi.INK, true);

        greetingRow.addView(greetingText, new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        greetingRow.addView(createIcon(icon, iconColour, 38));

        content.addView(greetingRow);

        addSpace(content, 6);

        content.addView(ScreenUi.text(context, "Let's take care of your health today",
                14, ScreenUi.MUTED, false
        ));

        addSpace(content, 10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEEE, dd MMMM yyyy", Locale.ENGLISH
        );

        content.addView(ScreenUi.text(context, LocalDate.now().format(formatter),
                14, ScreenUi.MUTED, false
        ));
    }


    // Create a steps or calories card
    private LinearLayout createActivityCard(String label, String value, int icon, int colour) {

        LinearLayout card = ScreenUi.card(context);

        card.addView(createIcon(icon, colour, 28));

        addSpace(card, 8);

        card.addView(ScreenUi.text(context, label, 13, colour, false));

        addSpace(card, 4);

        card.addView(ScreenUi.text(context, value, 19, colour, true));

        return card;
    }


    // Create the water intake card
    private LinearLayout createWaterCard(long water, int goal, IntConsumer onGoalChanged) {

        LinearLayout card = ScreenUi.card(context);

        LinearLayout heading = ScreenUi.row(context);

        heading.addView(createIcon(R.drawable.ic_water, ScreenUi.BLUE, 24));

        TextView label = ScreenUi.text(context, "Water intake",
                14, ScreenUi.BLUE, false
        );

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );

        labelParams.leftMargin = ScreenUi.dp(context, 8);

        heading.addView(label, labelParams);

        ImageButton settings = new ImageButton(context);

        settings.setImageResource(R.drawable.ic_settings);
        settings.setColorFilter(ScreenUi.MUTED);
        settings.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        settings.setContentDescription("Change daily water goal");

        heading.addView(settings, new LinearLayout.LayoutParams(
                        ScreenUi.dp(context, 48),
                        ScreenUi.dp(context, 48)
                )
        );

        settings.setOnClickListener(view -> showGoalDialog(goal, onGoalChanged));

        card.addView(heading);

        card.addView(ScreenUi.text(context, formatNumber(water) + " ml",
                22, ScreenUi.BLUE, true
        ));

        addSpace(card, 8);

        long percentage = Math.round(water * 100.0 / goal);

        ProgressBar progress = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal
        );

        progress.setMax(100);

        progress.setProgressTintList(ColorStateList.valueOf(ScreenUi.BLUE));

        progress.setProgressBackgroundTintList(ColorStateList.valueOf(0xFFE0E9F1));

        card.addView(progress, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ScreenUi.dp(context, 10)
                )
        );

        progress.setProgress((int) Math.min(percentage, 100), true);

        addSpace(card, 6);

        card.addView(ScreenUi.text(context, percentage + "% of daily goal (" + formatNumber(goal) + " ml)",
                13, ScreenUi.MUTED, false
        ));

        return card;
    }


    // Display the water goal settings dialog
    private void showGoalDialog(
            int currentGoal, IntConsumer onGoalChanged) {

        LinearLayout layout = ScreenUi.column(context);

        int padding = ScreenUi.dp(context, 24);

        layout.setPadding(padding, 0, padding, 0);

        EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true);
        input.setHint("Daily goal in ml");
        input.setText(String.valueOf(currentGoal));
        input.setSelectAllOnFocus(true);

        layout.addView(input);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Set Daily Water Goal")
                        .setMessage("Enter your preferred goal in ml.")
                        .setView(layout)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Save", null)
                        .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {

                    try {

                        int goal = Integer.parseInt(input.getText().toString().trim());

                        if (goal <= 0) {

                            input.setError("Enter a goal greater than zero");

                            return;
                        }

                        onGoalChanged.accept(goal);

                        dialog.dismiss();

                    } catch (NumberFormatException e) {

                        input.setError("Enter a valid water goal");
                    }
                });
    }


    // Display the seven-day statistics
    private void addHealthStats(
            LinearLayout content, HealthSummary summary) {

        LinearLayout heading = ScreenUi.row(context);

        TextView title = ScreenUi.text(
                context, "Health Stats",
                20, ScreenUi.INK, true
        );

        heading.addView(
                title,
                new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        heading.addView(createIcon(R.drawable.ic_stats, ScreenUi.PRIMARY, 26));

        content.addView(heading);

        addSpace(content, 5);

        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

        String dateRange = today.minusDays(6).format(formatter) + " - " + today.format(formatter);

        content.addView(ScreenUi.text(context, "Last 7 days • " + dateRange,
                13, ScreenUi.MUTED, false
        ));

        addSpace(content, 12);

        LinearLayout card = ScreenUi.card(context);

        LinearLayout topRow = ScreenUi.row(context);

        topRow.addView(
                createStat("Total steps", formatNumber(summary.getSteps()) + " steps",
                        R.drawable.ic_steps, ScreenUi.GREEN
                ),
                new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
                )
        );

        topRow.addView(
                createStat("Calories burned", formatNumber(summary.getCalories()) + " kcal",
                        R.drawable.ic_calories, ScreenUi.RED
                ),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );

        card.addView(topRow);

        addSpace(card, 16);

        card.addView(createStat("Water intake", formatNumber(summary.getWater()) + " ml",
                R.drawable.ic_water, ScreenUi.BLUE
        ));

        content.addView(card);
    }


    // Create an individual statistics item
    private LinearLayout createStat(String label, String value, int icon, int colour) {

        LinearLayout item = ScreenUi.column(context);

        item.setGravity(Gravity.CENTER);

        item.addView(createIcon(icon, colour, 26));

        addSpace(item, 5);

        TextView labelText = ScreenUi.text(context, label, 13, colour, false);

        labelText.setGravity(Gravity.CENTER);

        item.addView(labelText);

        TextView valueText = ScreenUi.text(context, value, 17, colour, true);

        valueText.setGravity(Gravity.CENTER);

        item.addView(valueText);

        return item;
    }


    // Create and color an icon
    private ImageView createIcon(int resource, int colour, int size) {

        ImageView icon = new ImageView(context);

        icon.setImageResource(resource);
        icon.setColorFilter(colour);

        icon.setLayoutParams(new LinearLayout.LayoutParams(
                        ScreenUi.dp(context, size),
                        ScreenUi.dp(context, size)
                )
        );

        return icon;
    }


    // Add vertical spacing
    private void addSpace(LinearLayout layout, int height) {

        android.view.View space = new android.view.View(context);

        layout.addView(space, new LinearLayout.LayoutParams(1, ScreenUi.dp(context, height)));
    }


    // Display numbers with thousands separators
    private String formatNumber(long value) {

        return String.format(Locale.ENGLISH, "%,d", value);
    }
}