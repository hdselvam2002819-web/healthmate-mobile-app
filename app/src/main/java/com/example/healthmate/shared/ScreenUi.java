package com.example.healthmate.shared;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class ScreenUi {

    // Application colours
    public static final int PRIMARY = Color.parseColor("#146D5A");
    public static final int BAR_GREEN = Color.parseColor("#B7DCC4");
    public static final int BACKGROUND = Color.parseColor("#F3F7F6");
    public static final int WHITE = Color.WHITE;

    public static final int INK = Color.parseColor("#173B33");
    public static final int MUTED = Color.parseColor("#53665F");

    public static final int GREEN = Color.parseColor("#21845B");
    public static final int RED = Color.parseColor("#B63838");
    public static final int BLUE = Color.parseColor("#236AB8");


    // Convert density-independent pixels
    public static int dp(Context context, int value) {

        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }


    // Create a vertical layout
    public static LinearLayout column(Context context) {

        LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.VERTICAL);

        return layout;
    }


    // Create a horizontal layout
    public static LinearLayout row(Context context) {

        LinearLayout layout = new LinearLayout(context);

        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        return layout;
    }


    // Create formatted text
    public static TextView text(Context context, String value, int size, int colour, boolean bold) {

        TextView textView = new TextView(context);

        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(colour);

        if (bold) {
            textView.setTypeface(
                    Typeface.DEFAULT, Typeface.BOLD
            );
        }

        return textView;
    }


    // Create a rounded background
    public static GradientDrawable roundedBackground(Context context, int colour, int radius) {

        GradientDrawable background = new GradientDrawable();

        background.setColor(colour);
        background.setCornerRadius(dp(context, radius));

        return background;
    }


    // Create a reusable activity card
    public static LinearLayout card(Context context) {

        LinearLayout card = column(context);

        card.setBackground(roundedBackground(context, WHITE, 18));

        int padding = dp(context, 16);

        card.setPadding(padding, padding, padding, padding);

        card.setElevation(dp(context, 5));

        return card;
    }


    // Create primary or outlined buttons
    public static MaterialButton button(Context context, String label, boolean filled) {

        MaterialButton button = new MaterialButton(context);

        button.setText(label);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setMinHeight(dp(context, 48));
        button.setCornerRadius(dp(context, 12));

        if (filled) {

            button.setBackgroundTintList(ColorStateList.valueOf(PRIMARY));

            button.setTextColor(WHITE);

        } else {

            button.setBackgroundTintList(ColorStateList.valueOf(WHITE));

            button.setTextColor(PRIMARY);

            button.setStrokeColor(ColorStateList.valueOf(PRIMARY));

            button.setStrokeWidth(dp(context, 1));
        }

        return button;
    }
}