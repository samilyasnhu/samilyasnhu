package com.cs360.cs360projecttwo_weighttracker.service;

import com.cs360.cs360projecttwo_weighttracker.domain.InputValidator;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.session.SessionManager;

/** Handles validation and persistence of notification settings. */
public class SmsSettingsService {
    private final SessionManager sessionManager;

    public SmsSettingsService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Result<String> savePhoneNumber(int userId, String phoneNumber) {
        Result<String> validation = InputValidator.validatePhoneNumber(phoneNumber);
        if (!validation.isSuccessful()) {
            return validation;
        }
        sessionManager.savePhoneNumber(userId, validation.getValue());
        return validation;
    }

    public void setSmsEnabled(int userId, boolean enabled) {
        sessionManager.setSmsEnabled(userId, enabled);
    }
}
