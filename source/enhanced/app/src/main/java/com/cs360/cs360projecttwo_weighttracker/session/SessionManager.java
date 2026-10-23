package com.cs360.cs360projecttwo_weighttracker.session;

import android.content.Context;
import android.content.SharedPreferences;

/** Centralizes session and SMS preference keys instead of scattering them across Activities. */
public class SessionManager {
    public static final String PREFS_NAME = "weight_tracker_prefs";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_SMS_ENABLED_PREFIX = "sms_enabled_user_";
    private static final String KEY_PHONE_PREFIX = "phone_user_";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getCurrentUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public void saveCurrentUser(int userId) {
        preferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public void clearCurrentUser() {
        preferences.edit().remove(KEY_USER_ID).apply();
    }

    public void setSmsEnabled(int userId, boolean enabled) {
        preferences.edit().putBoolean(KEY_SMS_ENABLED_PREFIX + userId, enabled).apply();
    }

    public boolean isSmsEnabled(int userId) {
        return preferences.getBoolean(KEY_SMS_ENABLED_PREFIX + userId, false);
    }

    public void savePhoneNumber(int userId, String phoneNumber) {
        preferences.edit().putString(KEY_PHONE_PREFIX + userId, phoneNumber).apply();
    }

    public String getPhoneNumber(int userId) {
        return preferences.getString(KEY_PHONE_PREFIX + userId, "");
    }
}
