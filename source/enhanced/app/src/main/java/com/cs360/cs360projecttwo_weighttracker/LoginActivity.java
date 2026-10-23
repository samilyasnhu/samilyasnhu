package com.cs360.cs360projecttwo_weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cs360.cs360projecttwo_weighttracker.data.SQLiteWeightRepository;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.service.AuthService;
import com.cs360.cs360projecttwo_weighttracker.session.SessionManager;

/**
 * Login and account creation screen. UI responsibilities stay in the Activity while
 * validation, authentication, and session persistence are delegated to separate classes.
 */
public class LoginActivity extends AppCompatActivity {

    private AuthService authService;
    private SessionManager sessionManager;
    private EditText editUsername;
    private EditText editPassword;
    private TextView textLoginMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SQLiteWeightRepository repository = new SQLiteWeightRepository(this);
        authService = new AuthService(repository);
        sessionManager = new SessionManager(this);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        textLoginMessage = findViewById(R.id.textLoginMessage);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> logInUser());
        buttonCreateAccount.setOnClickListener(v -> createNewUser());
    }

    private void logInUser() {
        Result<Integer> result = authService.logIn(
                editUsername.getText().toString(),
                editPassword.getText().toString()
        );

        if (!result.isSuccessful()) {
            showMessage(result.getMessage());
            return;
        }

        sessionManager.saveCurrentUser(result.getValue());
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
        openWeightScreen();
    }

    private void createNewUser() {
        Result<Integer> result = authService.createAccount(
                editUsername.getText().toString(),
                editPassword.getText().toString()
        );

        if (!result.isSuccessful()) {
            showMessage(result.getMessage());
            return;
        }

        sessionManager.saveCurrentUser(result.getValue());
        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SmsPermissionActivity.class));
        finish();
    }

    private void openWeightScreen() {
        startActivity(new Intent(this, WeightGridActivity.class));
        finish();
    }

    private void showMessage(String message) {
        textLoginMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
