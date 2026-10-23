package com.cs360.cs360projecttwo_weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cs360.cs360projecttwo_weighttracker.data.DatabaseDateCodec;
import com.cs360.cs360projecttwo_weighttracker.domain.InputValidator;
import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;
import com.cs360.cs360projecttwo_weighttracker.security.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the SQLite schema, migrations, constraints, and queries used by the application.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_WEIGHTS = "weights";
    private static final String TABLE_GOALS = "goals";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_ENTRY_DATE = "entry_date";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_GOAL_WEIGHT = "goal_weight";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createVersionTwoSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateVersionOneToVersionTwo(db);
        }
    }

    private void createVersionTwoSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                COLUMN_CREATED_AT + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE " + TABLE_WEIGHTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_ENTRY_DATE + " TEXT NOT NULL, " +
                COLUMN_WEIGHT + " REAL NOT NULL CHECK(" + COLUMN_WEIGHT + " >= " +
                InputValidator.MIN_WEIGHT_LBS + " AND " + COLUMN_WEIGHT + " <= " +
                InputValidator.MAX_WEIGHT_LBS + "), " +
                COLUMN_CREATED_AT + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_UPDATED_AT + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" +
                COLUMN_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_GOAL_WEIGHT + " REAL NOT NULL CHECK(" + COLUMN_GOAL_WEIGHT + " >= " +
                InputValidator.MIN_WEIGHT_LBS + " AND " + COLUMN_GOAL_WEIGHT + " <= " +
                InputValidator.MAX_WEIGHT_LBS + "), " +
                COLUMN_UPDATED_AT + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" +
                COLUMN_ID + ") ON DELETE CASCADE)");

        createIndexes(db);
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weights_user_date ON " + TABLE_WEIGHTS +
                "(" + COLUMN_USER_ID + ", " + COLUMN_ENTRY_DATE + ", " + COLUMN_ID + ")");
    }

    private void migrateVersionOneToVersionTwo(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE users RENAME TO users_v1");
        db.execSQL("ALTER TABLE weights RENAME TO weights_v1");
        db.execSQL("ALTER TABLE goals RENAME TO goals_v1");

        createVersionTwoSchema(db);

        db.execSQL("INSERT INTO " + TABLE_USERS + " (" +
                COLUMN_ID + ", " + COLUMN_USERNAME + ", " + COLUMN_PASSWORD_HASH + ") " +
                "SELECT id, username, password FROM users_v1");

        try (Cursor cursor = db.rawQuery(
                "SELECT id, user_id, entry_date, weight FROM weights_v1 ORDER BY id ASC",
                null
        )) {
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, cursor.getInt(0));
                values.put(COLUMN_USER_ID, cursor.getInt(1));
                values.put(COLUMN_ENTRY_DATE, normalizeStoredDate(cursor.getString(2)));
                values.put(COLUMN_WEIGHT, cursor.getDouble(3));
                db.insertOrThrow(TABLE_WEIGHTS, null, values);
            }
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT user_id, goal_weight FROM goals_v1",
                null
        )) {
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USER_ID, cursor.getInt(0));
                values.put(COLUMN_GOAL_WEIGHT, cursor.getDouble(1));
                db.insertWithOnConflict(
                        TABLE_GOALS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }
        }

        db.execSQL("DROP TABLE goals_v1");
        db.execSQL("DROP TABLE weights_v1");
        db.execSQL("DROP TABLE users_v1");
    }

    private String normalizeStoredDate(String value) {
        if (DatabaseDateCodec.isStorageDate(value)) {
            return value;
        }
        try {
            return DatabaseDateCodec.toStorageDate(value);
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD_HASH, PasswordHasher.hashPassword(password));

        try {
            return db.insertOrThrow(TABLE_USERS, null, values) != -1;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    /**
     * Authenticates by username and verifies the stored password value in application code.
     * Legacy plain-text values are replaced with PBKDF2 hashes after a successful login.
     */
    public int getUserId(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_PASSWORD_HASH},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null,
                "1"
        )) {
            if (!cursor.moveToFirst()) {
                return -1;
            }

            int userId = cursor.getInt(0);
            String storedPassword = cursor.getString(1);
            if (!PasswordHasher.verifyPassword(password, storedPassword)) {
                return -1;
            }

            if (!PasswordHasher.isHashed(storedPassword)) {
                upgradePlainTextPassword(userId, password);
            }
            return userId;
        }
    }

    private void upgradePlainTextPassword(int userId, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD_HASH, PasswordHasher.hashPassword(password));
        getWritableDatabase().update(
                TABLE_USERS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    /** Saves the weight entry and an optional goal change as one transaction. */
    public long saveWeightEntryAndGoal(int userId, String date, double weight, Double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (goalWeight != null && !upsertGoalWeight(db, userId, goalWeight)) {
                return -1;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_ENTRY_DATE, DatabaseDateCodec.toStorageDate(date));
            values.put(COLUMN_WEIGHT, weight);

            long entryId = db.insertOrThrow(TABLE_WEIGHTS, null, values);
            db.setTransactionSuccessful();
            return entryId;
        } catch (RuntimeException e) {
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateWeightEntry(int userId, int entryId, String date, double weight) {
        ContentValues values = new ContentValues();
        try {
            values.put(COLUMN_ENTRY_DATE, DatabaseDateCodec.toStorageDate(date));
        } catch (RuntimeException e) {
            return false;
        }
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_UPDATED_AT, currentTimestampExpressionValue());

        try {
            int rowsUpdated = getWritableDatabase().update(
                    TABLE_WEIGHTS,
                    values,
                    COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(entryId), String.valueOf(userId)}
            );
            return rowsUpdated == 1;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean deleteWeightEntry(int userId, int entryId) {
        int rowsDeleted = getWritableDatabase().delete(
                TABLE_WEIGHTS,
                COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(entryId), String.valueOf(userId)}
        );
        return rowsDeleted == 1;
    }

    public List<WeightRecord> getWeightEntries(int userId) {
        return queryWeightEntries(userId, null, null);
    }

    public List<WeightRecord> getWeightEntriesInRange(int userId, String startDate, String endDate) {
        String startStorage = toOptionalStorageDate(startDate);
        String endStorage = toOptionalStorageDate(endDate);
        return queryWeightEntries(userId, startStorage, endStorage);
    }

    private List<WeightRecord> queryWeightEntries(
            int userId,
            String startStorageDate,
            String endStorageDate
    ) {
        ArrayList<WeightRecord> entries = new ArrayList<>();
        ArrayList<String> selectionArgs = new ArrayList<>();
        StringBuilder selection = new StringBuilder(COLUMN_USER_ID + " = ?");
        selectionArgs.add(String.valueOf(userId));

        if (startStorageDate != null) {
            selection.append(" AND ").append(COLUMN_ENTRY_DATE).append(" >= ?");
            selectionArgs.add(startStorageDate);
        }
        if (endStorageDate != null) {
            selection.append(" AND ").append(COLUMN_ENTRY_DATE).append(" <= ?");
            selectionArgs.add(endStorageDate);
        }

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_WEIGHTS,
                new String[]{COLUMN_ID, COLUMN_ENTRY_DATE, COLUMN_WEIGHT},
                selection.toString(),
                selectionArgs.toArray(new String[0]),
                null,
                null,
                COLUMN_ENTRY_DATE + " ASC, " + COLUMN_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                entries.add(new WeightRecord(
                        cursor.getInt(0),
                        DatabaseDateCodec.toDisplayDate(cursor.getString(1)),
                        cursor.getDouble(2)
                ));
            }
        }
        return entries;
    }

    public boolean setGoalWeight(int userId, double goalWeight) {
        try {
            return upsertGoalWeight(getWritableDatabase(), userId, goalWeight);
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    private boolean upsertGoalWeight(SQLiteDatabase db, int userId, double goalWeight) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_GOAL_WEIGHT, goalWeight);
        values.put(COLUMN_UPDATED_AT, currentTimestampExpressionValue());

        return db.insertWithOnConflict(
                TABLE_GOALS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        ) != -1;
    }

    public Double getGoalWeight(int userId) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_GOALS,
                new String[]{COLUMN_GOAL_WEIGHT},
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null,
                "1"
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
            return null;
        }
    }

    private String toOptionalStorageDate(String displayDate) {
        String value = displayDate == null ? "" : displayDate.trim();
        if (value.isEmpty()) {
            return null;
        }
        return DatabaseDateCodec.toStorageDate(value);
    }

    private String currentTimestampExpressionValue() {
        return java.time.LocalDateTime.now().toString();
    }
}
