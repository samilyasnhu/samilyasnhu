package com.cs360.cs360projecttwo_weighttracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
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
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main weight-tracking screen. Users can create, read, update, and delete weight entries.
 */
public class WeightGridActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private int userId;

//    private TextView textGoalWeight;
    private EditText editDate;
    private EditText editDailyWeight;
    private EditText editGoalWeight;
    private TableLayout tableWeightGrid;
    private TextView textGoalWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_grid);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getInt(LoginActivity.KEY_USER_ID, -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        textGoalWeight = findViewById(R.id.textGoalWeight);
        editDate = findViewById(R.id.editDate);
        editDailyWeight = findViewById(R.id.editDailyWeight);
        editGoalWeight = findViewById(R.id.editGoalWeight);
        tableWeightGrid = findViewById(R.id.tableWeightGrid);

        Button buttonAddWeight = findViewById(R.id.buttonAddWeight);
        Button buttonSmsSettings = findViewById(R.id.buttonSmsSettings);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        editDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date()));
        buttonAddWeight.setOnClickListener(v -> addWeightEntry());
        buttonSmsSettings.setOnClickListener(v -> startActivity(new Intent(this, SmsPermissionActivity.class)));
        buttonLogout.setOnClickListener(v -> logOut());

        refreshGoalWeightText();
        loadWeightGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGoalWeightText();
        loadWeightGrid();
    }

    private void addWeightEntry() {
        String date = editDate.getText().toString().trim();
        String weightText = editDailyWeight.getText().toString().trim();
        String goalText = editGoalWeight.getText().toString().trim();

        if (date.isEmpty() || weightText.isEmpty()) {
            Toast.makeText(this, "Enter a date and weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!goalText.isEmpty()) {
            try {
                double goalWeight = Double.parseDouble(goalText);
                databaseHelper.setGoalWeight(userId, goalWeight);
                editGoalWeight.setText("");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid goal weight.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        long entryId = databaseHelper.addWeightEntry(userId, date, weight);
        if (entryId == -1) {
            Toast.makeText(this, "Weight entry was not saved.", Toast.LENGTH_SHORT).show();
            return;
        }

        editDailyWeight.setText("");
        refreshGoalWeightText();
        loadWeightGrid();
        sendGoalSmsIfNeeded(weight);
        Toast.makeText(this, "Weight added", Toast.LENGTH_SHORT).show();
    }

    private void refreshGoalWeightText() {
        Double goalWeight = databaseHelper == null ? null : databaseHelper.getGoalWeight(userId);
        if (goalWeight == null) {
            textGoalWeight.setText("Goal Weight: Not set");
        } else {
            textGoalWeight.setText(String.format(Locale.US, "Goal Weight: %.1f lbs", goalWeight));
        }
    }

    private void loadWeightGrid() {
        if (tableWeightGrid == null || databaseHelper == null) {
            return;
        }

        tableWeightGrid.removeAllViews();
        addHeaderRow();

        List<DatabaseHelper.WeightEntry> entries = databaseHelper.getWeightEntries(userId);
        for (DatabaseHelper.WeightEntry entry : entries) {
            addDataRow(entry);
        }
    }

    private void addHeaderRow() {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);

        row.addView(makeHeaderText("Date"));
        row.addView(makeHeaderText("Weight"));
        row.addView(makeHeaderText("Actions"));

        tableWeightGrid.addView(row);
    }

    private void addDataRow(DatabaseHelper.WeightEntry entry) {
        TableRow row = new TableRow(this);
        row.setPadding(8, 8, 8, 8);

        TextView dateView = makeBodyText(entry.date);
        TextView weightView = makeBodyText(String.format(Locale.US, "%.1f lbs", entry.weight));

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
        deleteButton.setOnClickListener(v -> deleteWeightEntry(entry.id));

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

    private void showUpdateDialog(DatabaseHelper.WeightEntry entry) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 0);

        EditText dateInput = new EditText(this);
        dateInput.setHint("Date");
        dateInput.setText(entry.date);

        EditText weightInput = new EditText(this);
        weightInput.setHint("Weight in lbs");
        weightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        weightInput.setText(String.format(Locale.US, "%.1f", entry.weight));

        dialogLayout.addView(dateInput);
        dialogLayout.addView(weightInput);

        new AlertDialog.Builder(this)
                .setTitle("Update Weight Entry")
                .setView(dialogLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDate = dateInput.getText().toString().trim();
                    String newWeightText = weightInput.getText().toString().trim();

                    if (newDate.isEmpty() || newWeightText.isEmpty()) {
                        Toast.makeText(this, "Date and weight are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double newWeight = Double.parseDouble(newWeightText);
                        if (databaseHelper.updateWeightEntry(entry.id, newDate, newWeight)) {
                            loadWeightGrid();
                            sendGoalSmsIfNeeded(newWeight);
                            Toast.makeText(this, "Weight updated", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter a valid weight.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWeightEntry(int entryId) {
        if (databaseHelper.deleteWeightEntry(entryId)) {
            loadWeightGrid();
            Toast.makeText(this, "Weight deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGoalSmsIfNeeded(double currentWeight) {
        Double goalWeight = databaseHelper.getGoalWeight(userId);
        if (goalWeight == null || currentWeight > goalWeight) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        boolean smsEnabled = preferences.getBoolean(SmsPermissionActivity.KEY_SMS_ENABLED_PREFIX + userId, false);
        String phoneNumber = preferences.getString(SmsPermissionActivity.KEY_PHONE_PREFIX + userId, "");

        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (!smsEnabled || phoneNumber.isEmpty() || !permissionGranted) {
            Toast.makeText(this, "Goal reached. SMS notification is not enabled.", Toast.LENGTH_LONG).show();
            return;
        }

        String message = String.format(
                Locale.US,
                "Congratulations! You reached your goal weight. Current weight: %.1f lbs. Goal: %.1f lbs.",
                currentWeight,
                goalWeight
        );

        try {
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Goal reached. SMS notification sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Goal reached, but SMS could not be sent.", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut() {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(LoginActivity.KEY_USER_ID)
                .apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
