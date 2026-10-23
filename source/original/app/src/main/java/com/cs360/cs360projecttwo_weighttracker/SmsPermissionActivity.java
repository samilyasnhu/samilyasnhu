package com.cs360.cs360projecttwo_weighttracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Lets the user allow or skip SMS notifications. The rest of the app works even if SMS is denied.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    public static final String KEY_SMS_ENABLED_PREFIX = "sms_enabled_user_";
    public static final String KEY_PHONE_PREFIX = "phone_user_";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private EditText editPhoneNumber;
    private TextView textSmsStatus;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        userId = preferences.getInt(LoginActivity.KEY_USER_ID, -1);

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        textSmsStatus = findViewById(R.id.textSmsStatus);
        Button buttonRequestSmsPermission = findViewById(R.id.buttonRequestSmsPermission);
        Button buttonSkipSmsPermission = findViewById(R.id.buttonSkipSmsPermission);

        editPhoneNumber.setText(preferences.getString(KEY_PHONE_PREFIX + userId, ""));
        updateStatusMessage();

        buttonRequestSmsPermission.setOnClickListener(v -> enableSmsNotifications());
        buttonSkipSmsPermission.setOnClickListener(v -> skipSmsNotifications());
    }

    private void enableSmsNotifications() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Enter a phone number for alerts.", Toast.LENGTH_SHORT).show();
            return;
        }

        savePhoneNumber(phoneNumber);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            saveSmsPreference(true);
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
        saveSmsPreference(false);
        Toast.makeText(this, "SMS notifications skipped", Toast.LENGTH_SHORT).show();
        openWeightScreen();
    }

    private void savePhoneNumber(String phoneNumber) {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_PHONE_PREFIX + userId, phoneNumber)
                .apply();
    }

    private void saveSmsPreference(boolean enabled) {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SMS_ENABLED_PREFIX + userId, enabled)
                .apply();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            saveSmsPreference(granted);

            if (granted) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. The app will still work without SMS.", Toast.LENGTH_LONG).show();
            }

            openWeightScreen();
        }
    }

    private void openWeightScreen() {
        Intent intent = new Intent(this, WeightGridActivity.class);
        startActivity(intent);
        finish();
    }
}
