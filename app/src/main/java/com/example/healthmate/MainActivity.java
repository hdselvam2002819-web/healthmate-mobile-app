package com.example.healthmate;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthmate.dashboard.DashboardScreen;
import com.example.healthmate.shared.HealthViewModel;
import com.example.healthmate.shared.ScreenUi;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    private static final int HOME = 1;
    private static final int RECORDS = 2;

    private HealthViewModel viewModel;

    private FrameLayout screenContent;

    private BottomNavigationView navigation;

    private int currentPage = HOME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        if (savedInstanceState != null) {

            currentPage = savedInstanceState.getInt("currentPage", HOME);
        }

        createApplicationLayout();

        observeData();

        configureBackButton();
    }


    // Create the main application layout
    private void createApplicationLayout() {

        LinearLayout root = ScreenUi.column(this);

        root.setBackgroundColor(ScreenUi.BAR_GREEN);

        setContentView(root);

        // Keep content clear of Android system bars
        ViewCompat.setOnApplyWindowInsetsListener(
                root, (view, insets) -> {

                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());

                    view.setPadding(bars.left, bars.top, bars.right,
                            Math.max(bars.bottom, keyboard.bottom)
                    );

                    return WindowInsetsCompat.CONSUMED;
                });

        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(true);

        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(true);

        // Fixed application header
        root.addView(createHeader());

        // Changeable screen content
        screenContent = new FrameLayout(this);
        screenContent.setBackgroundColor(ScreenUi.BACKGROUND);

        root.addView(screenContent, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
                )
        );

        // Fixed bottom navigation
        root.addView(createNavigation(), new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ScreenUi.dp(this, 64)
                )
        );
        navigation.setSelectedItemId(currentPage);
    }


    // Create the HealthMate header
    private LinearLayout createHeader() {

        LinearLayout header = ScreenUi.row(this);

        header.setBackgroundColor(ScreenUi.BAR_GREEN);

        header.setMinimumHeight(ScreenUi.dp(this, 60));

        int padding = ScreenUi.dp(this, 16);

        header.setPadding(padding, 0, padding, 0);

        TextView title = ScreenUi.text(this, "HealthMate",
                24, ScreenUi.INK, true
        );

        header.addView(
                title, new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );

        ImageView logo = new ImageView(this);

        logo.setImageResource(R.drawable.ic_health);

        header.addView(logo, new LinearLayout.LayoutParams(
                        ScreenUi.dp(this, 36),
                        ScreenUi.dp(this, 36)
                )
        );

        return header;
    }


    // Create the bottom navigation
    private BottomNavigationView createNavigation() {

        navigation = new BottomNavigationView(this);

        navigation.setMinimumHeight(0);

        navigation.setItemPaddingTop(
                ScreenUi.dp(this, 10)
        );

        navigation.setItemPaddingBottom(
                ScreenUi.dp(this, 2)
        );

        navigation.setBackgroundColor(ScreenUi.BAR_GREEN);

        navigation.getMenu().add(0, HOME, 0, "Home")
                .setIcon(R.drawable.ic_home);

        navigation.getMenu().add(0, RECORDS, 1, "Records")
                .setIcon(R.drawable.ic_history);

        navigation.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        ColorStateList colours = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        ScreenUi.PRIMARY,
                        ScreenUi.MUTED
                }
        );

        navigation.setItemIconTintList(colours);

        navigation.setItemTextColor(colours);

        navigation.setItemActiveIndicatorColor(
                ColorStateList.valueOf(0xFFE7F4EB)
        );

        navigation.setOnItemSelectedListener(item -> {

            currentPage = item.getItemId();

            renderScreen();

            return true;
        });

        return navigation;
    }


    // Observe changes in application data
    private void observeData() {

        viewModel.getRecords().observe(this, records -> {

            if (!viewModel.isBusy()) {

                renderScreen();
            }
        });

        viewModel.getBusy().observe(this, busy -> renderScreen());

        viewModel.getWaterGoal().observe(this, goal -> {

            if (!viewModel.isBusy()) {

                renderScreen();
            }
        });

        viewModel.getMessage().observe(this, message -> {

            if (message != null) {

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                viewModel.clearMessage();
            }
        });
    }


    // Display the currently selected screen
    private void renderScreen() {

        if (screenContent == null) {

            return;
        }

        int scrollPosition = 0;

        if (screenContent.getChildAt(0) instanceof ScrollView) {

            scrollPosition = screenContent.getChildAt(0).getScrollY();
        }

        screenContent.removeAllViews();

        if (viewModel.isBusy()) {

            showStatus("Loading health records...", true);

            return;
        }

        if (viewModel.getRecords().getValue() == null) {

            showStatus("Unable to load health records.", false);

            return;
        }

        if (currentPage == HOME) {

            LocalDate today = LocalDate.now();

            DashboardScreen dashboard = new DashboardScreen(this);

            Integer waterGoal = viewModel.getWaterGoal().getValue();

            if (waterGoal == null) {
                waterGoal = 2000;
            }

            ScrollView page = dashboard.create(
                    viewModel.getSummary(today, today),
                    viewModel.getSummary(today.minusDays(6), today), waterGoal,
                    viewModel::saveWaterGoal
            );

            screenContent.addView(page, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
            );

            int position = scrollPosition;

            page.post(() -> page.scrollTo(0, position));

        } else {

            showRecordsPreview();
        }
    }


    // Display loading or error information
    private void showStatus(String message, boolean loading) {

        LinearLayout layout = ScreenUi.column(this);

        layout.setGravity(Gravity.CENTER);

        int padding = ScreenUi.dp(this, 24);

        layout.setPadding(padding, padding, padding, padding);

        if (loading) {

            layout.addView(new ProgressBar(this));
        }

        TextView label = ScreenUi.text(this, message, 16, ScreenUi.MUTED, false);

        label.setGravity(Gravity.CENTER);

        label.setPadding(0, padding, 0, padding);

        layout.addView(label);

        if (!loading) {

            MaterialButton retry = ScreenUi.button(this, "Retry", true);

            retry.setOnClickListener(view -> viewModel.reload());

            layout.addView(retry);
        }

        screenContent.addView(layout, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }


    // Temporary Records screen for dashboard testing
    private void showRecordsPreview() {

        LinearLayout layout = ScreenUi.column(this);

        int padding = ScreenUi.dp(this, 16);

        layout.setPadding(padding, padding, padding, padding);

        layout.addView(ScreenUi.text(this, "Health Records",
                24, ScreenUi.INK, true
        ));

        layout.addView(ScreenUi.text(this, "Track your progress, one day at a time",
                14, ScreenUi.MUTED, false
        ));

        if (viewModel.filterRecords(null).isEmpty()) {

            MaterialButton sampleButton = ScreenUi.button(
                            this, "Load Sample Records", true
                    );

            sampleButton.setOnClickListener(view -> viewModel.addSamples());

            layout.addView(sampleButton);

        } else {

            layout.addView(ScreenUi.text(this, viewModel.filterRecords(null).size()
                            + " saved health records", 16, ScreenUi.INK, false
            ));
        }

        screenContent.addView(layout);
    }


    // Android system Back button
    private void configureBackButton() {

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (currentPage == RECORDS) {

                            navigation.setSelectedItemId(HOME);

                        } else {

                            finish();
                        }
                    }
                }
        );
    }


    // Refresh date and greeting when returning to the app
    @Override
    protected void onResume() {

        super.onResume();

        renderScreen();
    }


    // Remember selected screen during recreation
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("currentPage", currentPage);

        super.onSaveInstanceState(outState);
    }
}