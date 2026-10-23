package com.cs360.cs360projecttwo_weighttracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Login and account creation screen. A new user can create an account, and returning users can log in.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "weight_tracker_prefs";
    public static final String KEY_USER_ID = "current_user_id";

    private DatabaseHelper databaseHelper;
    private EditText editUsername;
    private EditText editPassword;
    private TextView textLoginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        textLoginMessage = findViewById(R.id.textLoginMessage);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> logInUser());
        buttonCreateAccount.setOnClickListener(v -> createNewUser());
    }

    private void logInUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Enter both a username and password.");
            return;
        }

        int userId = databaseHelper.getUserId(username, password);
        if (userId == -1) {
            showMessage("Login failed. Check your username and password.");
            return;
        }

        saveCurrentUser(userId);
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
        openWeightScreen();
    }

    private void createNewUser() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Choose a username and password.");
            return;
        }

        if (!databaseHelper.createUser(username, password)) {
            showMessage("That username already exists. Try logging in instead.");
            return;
        }

        int userId = databaseHelper.getUserId(username, password);
        saveCurrentUser(userId);
        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, SmsPermissionActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveCurrentUser(int userId) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    private void openWeightScreen() {
        Intent intent = new Intent(this, WeightGridActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        textLoginMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
