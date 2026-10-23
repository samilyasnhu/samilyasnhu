package com.cs360.cs360projecttwo_weighttracker.service;

import com.cs360.cs360projecttwo_weighttracker.data.WeightRepository;
import com.cs360.cs360projecttwo_weighttracker.domain.InputValidator;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;

/** Handles authentication rules independently from the Android UI. */
public class AuthService {
    private final WeightRepository repository;

    public AuthService(WeightRepository repository) {
        this.repository = repository;
    }

    public Result<Integer> logIn(String username, String password) {
        Result<String> usernameResult = InputValidator.validateUsername(username);
        if (!usernameResult.isSuccessful()) {
            return Result.failure(usernameResult.getMessage());
        }

        Result<String> passwordResult = InputValidator.validatePassword(password, false);
        if (!passwordResult.isSuccessful()) {
            return Result.failure(passwordResult.getMessage());
        }

        int userId = repository.authenticateUser(usernameResult.getValue(), passwordResult.getValue());
        if (userId < 0) {
            return Result.failure("Login failed. Check your username and password.");
        }
        return Result.success(userId);
    }

    public Result<Integer> createAccount(String username, String password) {
        Result<String> usernameResult = InputValidator.validateUsername(username);
        if (!usernameResult.isSuccessful()) {
            return Result.failure(usernameResult.getMessage());
        }

        Result<String> passwordResult = InputValidator.validatePassword(password, true);
        if (!passwordResult.isSuccessful()) {
            return Result.failure(passwordResult.getMessage());
        }

        if (!repository.createUser(usernameResult.getValue(), passwordResult.getValue())) {
            return Result.failure("That username already exists. Try logging in instead.");
        }

        int userId = repository.authenticateUser(usernameResult.getValue(), passwordResult.getValue());
        if (userId < 0) {
            return Result.failure("Account was created, but automatic login failed.");
        }
        return Result.success(userId);
    }
}
