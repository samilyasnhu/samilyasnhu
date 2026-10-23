package com.cs360.cs360projecttwo_weighttracker.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Centralizes user-input validation so the same rules can be reused by multiple screens.
 */
public final class InputValidator {
    public static final double MIN_WEIGHT_LBS = 20.0;
    public static final double MAX_WEIGHT_LBS = 1500.0;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final String DATE_PATTERN = "MM/dd/yyyy";

    private InputValidator() {
        // Utility class.
    }

    public static Result<String> validateUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (value.isEmpty()) {
            return Result.failure("Enter a username.");
        }
        if (value.length() < MIN_USERNAME_LENGTH || value.length() > MAX_USERNAME_LENGTH) {
            return Result.failure("Username must be between 3 and 30 characters.");
        }
        return Result.success(value);
    }

    public static Result<String> validatePassword(String password, boolean creatingAccount) {
        String value = password == null ? "" : password;
        if (value.trim().isEmpty()) {
            return Result.failure("Enter a password.");
        }
        if (creatingAccount && value.length() < MIN_PASSWORD_LENGTH) {
            return Result.failure("Password must be at least 6 characters.");
        }
        return Result.success(value);
    }

    public static Result<String> validateDate(String dateText) {
        String value = dateText == null ? "" : dateText.trim();
        if (value.isEmpty()) {
            return Result.failure("Enter a date.");
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        formatter.setLenient(false);
        try {
            formatter.parse(value);
            return Result.success(value);
        } catch (ParseException e) {
            return Result.failure("Enter the date in MM/dd/yyyy format.");
        }
    }

    public static Result<Double> validateWeight(String weightText, String fieldName) {
        String value = weightText == null ? "" : weightText.trim();
        String label = fieldName == null || fieldName.trim().isEmpty() ? "Weight" : fieldName.trim();

        if (value.isEmpty()) {
            return Result.failure("Enter " + label.toLowerCase(Locale.US) + ".");
        }

        final double weight;
        try {
            weight = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Result.failure("Enter a valid " + label.toLowerCase(Locale.US) + ".");
        }

        if (!Double.isFinite(weight) || weight < MIN_WEIGHT_LBS || weight > MAX_WEIGHT_LBS) {
            return Result.failure(label + " must be between 20 and 1500 lbs.");
        }

        return Result.success(weight);
    }

    public static Result<String> validatePhoneNumber(String phoneNumber) {
        String value = phoneNumber == null ? "" : phoneNumber.trim();
        if (value.isEmpty()) {
            return Result.failure("Enter a phone number for alerts.");
        }

        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() < 10 || digits.length() > 15) {
            return Result.failure("Enter a valid phone number.");
        }
        return Result.success(value);
    }
}
