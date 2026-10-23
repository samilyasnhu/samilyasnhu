package com.cs360.cs360projecttwo_weighttracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cs360.cs360projecttwo_weighttracker.analysis.WeightAnalysis;
import com.cs360.cs360projecttwo_weighttracker.data.SQLiteWeightRepository;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;
import com.cs360.cs360projecttwo_weighttracker.notification.GoalNotificationService;
import com.cs360.cs360projecttwo_weighttracker.service.WeightService;
import com.cs360.cs360projecttwo_weighttracker.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main weight-tracking screen. The Activity manages presentation while the service,
 * repository, session, notification, and analysis classes handle application logic.
 */
public class WeightGridActivity extends AppCompatActivity {

    private int userId;
    private EditText editDate;
    private EditText editDailyWeight;
    private EditText editGoalWeight;
    private EditText editFilterStartDate;
    private EditText editFilterEndDate;
    private TableLayout tableWeightGrid;
    private TextView textGoalWeight;
    private TextView textAnalysisSummary;
    private TextView textHistoryRange;

    private String activeStartDate = "";
    private String activeEndDate = "";

    private WeightService weightService;
    private SessionManager sessionManager;
    private GoalNotificationService goalNotificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_grid);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        SQLiteWeightRepository repository = new SQLiteWeightRepository(this);
        weightService = new WeightService(repository);
        goalNotificationService = new GoalNotificationService(this, sessionManager);

        textGoalWeight = findViewById(R.id.textGoalWeight);
        textAnalysisSummary = findViewById(R.id.textAnalysisSummary);
        textHistoryRange = findViewById(R.id.textHistoryRange);
        editDate = findViewById(R.id.editDate);
        editDailyWeight = findViewById(R.id.editDailyWeight);
        editGoalWeight = findViewById(R.id.editGoalWeight);
        editFilterStartDate = findViewById(R.id.editFilterStartDate);
        editFilterEndDate = findViewById(R.id.editFilterEndDate);
        tableWeightGrid = findViewById(R.id.tableWeightGrid);

        Button buttonAddWeight = findViewById(R.id.buttonAddWeight);
        Button buttonApplyFilter = findViewById(R.id.buttonApplyFilter);
        Button buttonClearFilter = findViewById(R.id.buttonClearFilter);
        Button buttonSmsSettings = findViewById(R.id.buttonSmsSettings);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        editDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date()));
        buttonAddWeight.setOnClickListener(v -> addWeightEntry());
        buttonApplyFilter.setOnClickListener(v -> applyDateFilter());
        buttonClearFilter.setOnClickListener(v -> clearDateFilter());
        buttonSmsSettings.setOnClickListener(v -> startActivity(new Intent(this, SmsPermissionActivity.class)));
        buttonLogout.setOnClickListener(v -> logOut());

        refreshGoalWeightText();
        loadWeightGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (weightService != null) {
            refreshGoalWeightText();
            loadWeightGrid();
        }
    }

    private void addWeightEntry() {
        Result<WeightService.AddWeightResult> result = weightService.addWeightEntry(
                userId,
                editDate.getText().toString(),
                editDailyWeight.getText().toString(),
                editGoalWeight.getText().toString()
        );

        if (!result.isSuccessful()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        WeightService.AddWeightResult added = result.getValue();
        editDailyWeight.setText("");
        if (added.getUpdatedGoalWeight() != null) {
            editGoalWeight.setText("");
        }

        refreshGoalWeightText();
        loadWeightGrid();
        showGoalNotificationResult(added.getCurrentWeight());
        Toast.makeText(this, "Weight added", Toast.LENGTH_SHORT).show();
    }

    private void refreshGoalWeightText() {
        Double goalWeight = weightService.getGoalWeight(userId);
        if (goalWeight == null) {
            textGoalWeight.setText("Goal Weight: Not set");
        } else {
            textGoalWeight.setText(String.format(Locale.US, "Goal Weight: %.1f lbs", goalWeight));
        }
    }

    private void applyDateFilter() {
        String requestedStart = editFilterStartDate.getText().toString().trim();
        String requestedEnd = editFilterEndDate.getText().toString().trim();

        Result<List<WeightRecord>> result = weightService.getWeightEntriesForRange(
                userId,
                requestedStart,
                requestedEnd
        );
        if (!result.isSuccessful()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        activeStartDate = requestedStart;
        activeEndDate = requestedEnd;
        renderWeightGrid(result.getValue());
    }

    private void clearDateFilter() {
        activeStartDate = "";
        activeEndDate = "";
        editFilterStartDate.setText("");
        editFilterEndDate.setText("");
        loadWeightGrid();
    }

    private void loadWeightGrid() {
        Result<List<WeightRecord>> result = weightService.getWeightEntriesForRange(
                userId,
                activeStartDate,
                activeEndDate
        );
        if (!result.isSuccessful()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        renderWeightGrid(result.getValue());
    }

    private void renderWeightGrid(List<WeightRecord> entries) {
        tableWeightGrid.removeAllViews();
        addHeaderRow();

        for (WeightRecord entry : entries) {
            addDataRow(entry);
        }

        refreshHistoryRangeText(entries.size());
        refreshAnalysisSummary(entries);
    }

    private void refreshHistoryRangeText(int entryCount) {
        if (activeStartDate.isEmpty() && activeEndDate.isEmpty()) {
            textHistoryRange.setText(String.format(Locale.US, "%d entries, oldest to newest", entryCount));
            return;
        }

        String startLabel = activeStartDate.isEmpty() ? "first entry" : activeStartDate;
        String endLabel = activeEndDate.isEmpty() ? "latest entry" : activeEndDate;
        textHistoryRange.setText(String.format(
                Locale.US,
                "%d entries from %s to %s",
                entryCount,
                startLabel,
                endLabel
        ));
    }

    private void refreshAnalysisSummary(List<WeightRecord> entries) {
        WeightAnalysis analysis = weightService.analyzeWeightEntries(
                entries,
                weightService.getGoalWeight(userId)
        );

        if (analysis == null) {
            textAnalysisSummary.setText("No weight entries are available for this range.");
            return;
        }

        String movingAverageText;
        if (analysis.getLatestMovingAverage() == null) {
            movingAverageText = "7-entry moving average: Need at least 7 entries";
        } else {
            movingAverageText = String.format(
                    Locale.US,
                    "7-entry moving average: %.1f lbs",
                    analysis.getLatestMovingAverage()
            );
        }

        String goalText;
        if (analysis.getGoalDistance() == null || analysis.getGoalProgressPercent() == null) {
            goalText = "Goal progress: Set a goal weight to calculate progress";
        } else {
            goalText = String.format(
                    Locale.US,
                    "Goal progress: %.1f%% | Distance to goal: %.1f lbs",
                    analysis.getGoalProgressPercent(),
                    analysis.getGoalDistance()
            );
        }

        String summary = String.format(
                Locale.US,
                "Current: %.1f lbs | Change: %+.1f lbs\n" +
                        "Average: %.1f lbs | Min: %.1f | Max: %.1f\n" +
                        "%s\nTrend: %s\n%s",
                analysis.getCurrentWeight(),
                analysis.getTotalChange(),
                analysis.getAverageWeight(),
                analysis.getMinimumWeight(),
                analysis.getMaximumWeight(),
                movingAverageText,
                analysis.getTrend(),
                goalText
        );
        textAnalysisSummary.setText(summary);
    }

    private void addHeaderRow() {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);

        row.addView(makeHeaderText("Date"));
        row.addView(makeHeaderText("Weight"));
        row.addView(makeHeaderText("Actions"));

        tableWeightGrid.addView(row);
    }

    private void addDataRow(WeightRecord entry) {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);

        TextView dateView = makeBodyText(entry.getDate());
        TextView weightView = makeBodyText(String.format(Locale.US, "%.1f lbs", entry.getWeight()));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(4, 4, 4, 4);

        Button updateButton = new Button(this);
        updateButton.setText("Update");
        updateButton.setMinHeight(dpToPx(48));
        updateButton.setOnClickListener(v -> showUpdateDialog(entry));

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setMinHeight(dpToPx(48));
        deleteButton.setOnClickListener(v -> deleteWeightEntry(entry.getId()));

        actions.addView(updateButton);
        actions.addView(deleteButton);

        row.addView(dateView);
        row.addView(weightView);
        row.addView(actions);

        tableWeightGrid.addView(row);
    }

    private TextView makeHeaderText(String text) {
        TextView textView = makeBaseTableText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0xFF111827);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private TextView makeBodyText(String text) {
        TextView textView = makeBaseTableText(text);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(0xFF374151);
        return textView;
    }

    private TextView makeBaseTableText(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setPadding(8, 8, 8, 8);
        textView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return textView;
    }

    private void showUpdateDialog(WeightRecord entry) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 0);

        EditText dateInput = new EditText(this);
        dateInput.setHint("Date");
        dateInput.setText(entry.getDate());

        EditText weightInput = new EditText(this);
        weightInput.setHint("Weight in lbs");
        weightInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        weightInput.setText(String.format(Locale.US, "%.1f", entry.getWeight()));

        dialogLayout.addView(dateInput);
        dialogLayout.addView(weightInput);

        new AlertDialog.Builder(this)
                .setTitle("Update Weight Entry")
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    Result<Double> result = weightService.updateWeightEntry(
                            userId,
                            entry.getId(),
                            dateInput.getText().toString(),
                            weightInput.getText().toString()
                    );

                    if (!result.isSuccessful()) {
                        Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loadWeightGrid();
                    showGoalNotificationResult(result.getValue());
                    Toast.makeText(this, "Weight updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWeightEntry(int entryId) {
        Result<Boolean> result = weightService.deleteWeightEntry(userId, entryId);
        if (!result.isSuccessful()) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        loadWeightGrid();
        Toast.makeText(this, "Weight deleted", Toast.LENGTH_SHORT).show();
    }

    private void showGoalNotificationResult(double currentWeight) {
        Result<Boolean> notificationResult = goalNotificationService.notifyIfGoalReached(
                userId,
                currentWeight,
                weightService.getGoalWeight(userId)
        );

        if (!notificationResult.isSuccessful()) {
            Toast.makeText(this, notificationResult.getMessage(), Toast.LENGTH_LONG).show();
        } else if (Boolean.TRUE.equals(notificationResult.getValue())) {
            Toast.makeText(this, "Goal reached. SMS notification sent.", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut() {
        sessionManager.clearCurrentUser();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
