package com.cs360.cs360projecttwo_weighttracker.domain;

/**
 * Small result wrapper used by the service layer so Activities can display useful
 * feedback without containing validation and business rules themselves.
 */
public final class Result<T> {
    private final boolean successful;
    private final T value;
    private final String message;

    private Result(boolean successful, T value, String message) {
        this.successful = successful;
        this.value = value;
        this.message = message;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(true, value, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public T getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
