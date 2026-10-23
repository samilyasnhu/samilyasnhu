package com.cs360.cs360projecttwo_weighttracker.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** Converts between the date format shown in the UI and the sortable format stored in SQLite. */
public final class DatabaseDateCodec {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter
            .ofPattern("MM/dd/uuuu", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter STORAGE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private DatabaseDateCodec() {
        // Utility class.
    }

    public static String toStorageDate(String displayDate) {
        LocalDate date = LocalDate.parse(displayDate, DISPLAY_FORMATTER);
        return STORAGE_FORMATTER.format(date);
    }

    public static String toDisplayDate(String storageDate) {
        try {
            LocalDate date = LocalDate.parse(storageDate, STORAGE_FORMATTER);
            return DISPLAY_FORMATTER.format(date);
        } catch (DateTimeParseException ignored) {
            return storageDate;
        }
    }

    public static boolean isStorageDate(String value) {
        if (value == null) {
            return false;
        }
        try {
            LocalDate.parse(value, STORAGE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
