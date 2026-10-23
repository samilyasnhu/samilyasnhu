package com.cs360.cs360projecttwo_weighttracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.service.SmsSettingsService;
import com.cs360.cs360projecttwo_weighttracker.session.SessionManager;

/**
 * Lets the user allow or skip SMS notifications. Preference storage and phone-number
 * validation are delegated so this Activity can focus on permission flow and navigation.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private EditText editPhoneNumber;
    private TextView textSmsStatus;
    private int userId;
    private SessionManager sessionManager;
    private SmsSettingsService smsSettingsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        sessionManager = new SessionManager(this);
        smsSettingsService = new SmsSettingsService(sessionManager);
        userId = sessionManager.getCurrentUserId();

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        Button buttonRequestSmsPermission = findViewById(R.id.buttonRequestSmsPermission);
        Button buttonSkipSmsPermission = findViewById(R.id.buttonSkipSmsPermission);

        editPhoneNumber.setText(sessionManager.getPhoneNumber(userId));
        updateStatusMessage();

        buttonRequestSmsPermission.setOnClickListener(v -> enableSmsNotifications());
        buttonSkipSmsPermission.setOnClickListener(v -> skipSmsNotifications());
    }

    private void enableSmsNotifications() {
        Result<String> phoneResult = smsSettingsService.savePhoneNumber(
                userId,
                editPhoneNumber.getText().toString()
        );
        if (!phoneResult.isSuccessful()) {
            Toast.makeText(this, phoneResult.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsSettingsService.setSmsEnabled(userId, true);
            Toast.makeText(this, "SMS notifications enabled", Toast.LENGTH_SHORT).show();
            openWeightScreen();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void skipSmsNotifications() {
        smsSettingsService.setSmsEnabled(userId, false);
        Toast.makeText(this, "SMS notifications skipped", Toast.LENGTH_SHORT).show();
        openWeightScreen();
    }

    private void updateStatusMessage() {
        boolean permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {
            textSmsStatus.setText("SMS permission is currently granted. You can enable goal alerts.");
        } else {
            textSmsStatus.setText("SMS permission is not granted. You can request it or skip notifications.");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            smsSettingsService.setSmsEnabled(userId, granted);

            if (granted) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        this,
                        "SMS permission denied. The app will still work without SMS.",
                        Toast.LENGTH_LONG
                ).show();
            }

            openWeightScreen();
        }
    }

    private void openWeightScreen() {
        startActivity(new Intent(this, WeightGridActivity.class));
        finish();
    }
}
