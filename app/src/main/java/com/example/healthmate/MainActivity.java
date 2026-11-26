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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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
import com.example.healthmate.records.RecordsScreen;
import com.example.healthmate.model.HealthRecord;
import com.example.healthmate.records.RecordFormScreen;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    private static final int HOME = 1;
    private static final int RECORDS = 2;

    private HealthViewModel viewModel;

    private FrameLayout screenContent;

    private BottomNavigationView navigation;

    private int currentPage = HOME;

    private boolean historySelected = false;

    private LocalDate selectedHistoryDate = LocalDate.now();

    private RecordFormScreen recordForm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        viewModel = new ViewModelProvider(this).get(HealthViewModel.class);

        if (savedInstanceState != null) {

            currentPage = savedInstanceState.getInt("currentPage", HOME);

            historySelected = savedInstanceState.getBoolean("historySelected", false);

            String date = savedInstanceState.getString("selectedHistoryDate",
                    LocalDate.now().toString()
            );

            selectedHistoryDate = date.isEmpty()
                    ? null
                    : LocalDate.parse(date);
        }

        createApplicationLayout();

        restoreRecordForm(savedInstanceState);

        observeData();

        configureBackButton();
    }


    // Create the main application layout
    private void createApplicationLayout() {

        LinearLayout root = ScreenUi.column(this);

        root.setBackgroundColor(ScreenUi.BAR_GREEN);

        setContentView(root);

        ViewCompat.setOnApplyWindowInsetsListener(
                root, (view, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    Insets keyboard = insets.getInsets(
                            WindowInsetsCompat.Type.ime()
                    );

                    boolean keyboardVisible = insets.isVisible(
                            WindowInsetsCompat.Type.ime()
                    );

                    // Hide bottom navigation while typing
                    if (navigation != null) {

                        navigation.setVisibility(
                                keyboardVisible
                                        ? View.GONE
                                        : View.VISIBLE
                        );
                    }

                    view.setPadding(
                            bars.left, bars.top, bars.right,
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

        header.addView(logo, new LinearLayout.LayoutParams(ScreenUi.dp(this, 36),
                        ScreenUi.dp(this, 36)
                )
        );

        return header;
    }


    // Create the bottom navigation
    private BottomNavigationView createNavigation() {

        navigation = new BottomNavigationView(this);

        navigation.setMinimumHeight(0);

        navigation.setItemPaddingTop(ScreenUi.dp(this, 10));

        navigation.setItemPaddingBottom(ScreenUi.dp(this, 2));

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

        navigation.setItemActiveIndicatorColor(ColorStateList.valueOf(0xFFE7F4EB));

        navigation.setOnItemSelectedListener(item -> {

            int requestedPage = item.getItemId();

            // Protect unfinished record forms
            if (recordForm != null) {

                if (viewModel.isBusy()) {
                    return false;
                }

                if (requestedPage == HOME) {

                    if (recordForm.hasUnsavedChanges()) {

                        confirmDiscardChanges(() -> {

                            hideFormKeyboard();

                            recordForm = null;

                            navigation.setSelectedItemId(HOME);
                        });

                        return false;
                    }

                    hideFormKeyboard();

                    recordForm = null;

                } else {

                    return false;
                }
            }

            currentPage = requestedPage;

            screenContent.removeAllViews();

            renderScreen();

            return true;
        });

        return navigation;
    }

    // Observe changes from the ViewModel
    private void observeData() {

        // Observe health records
        viewModel.getRecords().observe(this, records -> {

            if (recordForm == null && !viewModel.isBusy()) {

                renderScreen();
            }
        });

        // Observe loading and saving state
        viewModel.getBusy().observe(this, busy -> {

            boolean loading = Boolean.TRUE.equals(busy);

            if (recordForm != null) {

                recordForm.setBusy(loading);

            } else {

                renderScreen();
            }
        });

        // Observe water goal changes
        viewModel.getWaterGoal().observe(this, goal -> {

            if (recordForm == null && !viewModel.isBusy()) {

                renderScreen();
            }
        });

        // Display success or error messages
        viewModel.getMessage().observe(this, message -> {

            if (message != null) {

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                viewModel.clearMessage();
            }
        });

        // Observe successful record saves
        viewModel.getSaved().observe(this, saved -> {

            if (Boolean.TRUE.equals(saved)) {

                viewModel.clearSaved();

                if (recordForm != null) {

                    LocalDate savedDate = recordForm.getSelectedDate();

                    // Display the newly saved record
                    if (historySelected || !savedDate.equals(LocalDate.now())) {

                        historySelected = true;

                        selectedHistoryDate = savedDate;
                    }

                    closeRecordForm();
                }
            }
        });
    }


    // Display the currently selected screen
    private void renderScreen() {

        // Preserve the form while the user is editing
        if (recordForm != null) {
            return;
        }

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

            ScrollView page = dashboard.create(viewModel.getSummary(today, today),
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

            showRecordsScreen();
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

    // Display the Health Records screen
    private void showRecordsScreen() {

        RecordsScreen recordsScreen = new RecordsScreen(this, viewModel);

        ScrollView page = recordsScreen.create(historySelected, selectedHistoryDate,

                history -> {

                    historySelected = history;

                    renderScreen();
                },

                date -> {

                    selectedHistoryDate = date;
                },

                this::openAddRecord,

                this::openEditRecord
        );

        screenContent.addView(
                page,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    // Open the Add Record form
    private void openAddRecord(LocalDate date) {

        if (viewModel.isBusy()) {
            return;
        }

        // Prevent future records
        if (date.isAfter(LocalDate.now())) {

            Toast.makeText(this,
                    "Cannot add records for future dates.", Toast.LENGTH_SHORT).show();

            return;
        }

        // Prevent duplicate daily records
        if (!viewModel.filterRecords(date.toString()).isEmpty()) {

            Toast.makeText(this,
                    "A record already exists for this date. "
                            + "Please edit the existing record.", Toast.LENGTH_LONG).show();

            return;
        }

        viewModel.clearSaved();

        showRecordForm(date, null, null);
    }

    // Open the Edit Record form
    private void openEditRecord(HealthRecord record) {

        if (viewModel.isBusy()) {
            return;
        }

        viewModel.clearSaved();

        LocalDate date = LocalDate.parse(record.getDate());

        showRecordForm(date, record, null);
    }

    // Display the shared Add or Edit Record form
    private void showRecordForm(LocalDate date, HealthRecord record, Bundle draft) {

        recordForm = new RecordFormScreen(this, date, record);

        ScrollView page = recordForm.create(viewModel::saveRecord, this::requestCloseForm);

        if (draft != null) {
            recordForm.restoreDraft(draft);
        }

        screenContent.removeAllViews();

        screenContent.addView(
                page,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        recordForm.setBusy(viewModel.isBusy());
    }

    // Request to leave the record form
    private void requestCloseForm() {

        if (recordForm == null || viewModel.isBusy()) {
            return;
        }

        if (recordForm.hasUnsavedChanges()) {

            confirmDiscardChanges(this::closeRecordForm);

        } else {

            closeRecordForm();
        }
    }

    // Confirm before discarding unfinished changes
    private void confirmDiscardChanges(Runnable onDiscard) {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Discard changes?")
                .setMessage("Your unsaved changes will be lost.")
                .setNegativeButton("Keep editing", null)
                .setPositiveButton("Discard", (dialog, which) -> onDiscard.run())
                .show();
    }

    // Return to the Records screen
    private void closeRecordForm() {

        hideFormKeyboard();

        recordForm = null;

        screenContent.removeAllViews();

        renderScreen();
    }

    // Hide the keyboard when leaving the form
    private void hideFormKeyboard() {

        View focusedView = getCurrentFocus();

        if (focusedView != null) {

            InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            if (keyboard != null) {

                keyboard.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }

            focusedView.clearFocus();
        }
    }

    // Handle Android system Back navigation
    private void configureBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,

                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        // Return from Add or Edit form
                        if (recordForm != null) {

                            requestCloseForm();

                            return;
                        }

                        // Return from Records to Home
                        if (currentPage == RECORDS) {

                            navigation.setSelectedItemId(HOME);

                            return;
                        }

                        // Close the application from Home
                        finish();
                    }
                }
        );
    }


    // Refresh date and greeting when returning to the app
    @Override
    protected void onResume() {

        super.onResume();

        if (recordForm != null) {
            return;
        }

        renderScreen();
    }


    // Remember selected screen during recreation
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt("currentPage", currentPage);

        outState.putBoolean("historySelected", historySelected);

        outState.putString("selectedHistoryDate",
                selectedHistoryDate == null
                        ? ""
                        : selectedHistoryDate.toString()
        );

        // Preserve unfinished form input
        if (recordForm != null) {

            outState.putBundle("recordDraft", recordForm.saveDraft());
        }

        super.onSaveInstanceState(outState);
    }

    // Restore an unfinished Add or Edit form
    private void restoreRecordForm(Bundle savedState) {

        if (savedState == null) {
            return;
        }

        Bundle draft = savedState.getBundle("recordDraft");

        if (draft == null) {
            return;
        }

        LocalDate initialDate = LocalDate.parse(
                draft.getString("initialDate", LocalDate.now().toString())
        );

        long recordId = draft.getLong("recordId", 0);

        HealthRecord originalRecord = null;

        if (recordId != 0) {

            originalRecord = new HealthRecord(recordId, initialDate.toString(),
                    draft.getInt("originalSteps"),
                    draft.getInt("originalCalories"),
                    draft.getInt("originalWater")
            );
        }

        showRecordForm(initialDate, originalRecord, draft);
    }
}