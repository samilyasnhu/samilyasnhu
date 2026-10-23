package com.cs360.cs360projecttwo_weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the persistent SQLite database for users, weight entries, and goal weight values.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_WEIGHTS = "weights";
    private static final String TABLE_GOALS = "goals";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_WEIGHTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "entry_date TEXT NOT NULL, " +
                "weight REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))");

        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE NOT NULL, " +
                "goal_weight REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean createUser(String username, String password) {
        if (usernameExists(username)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ?",
                new String[]{username}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int getUserId(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username = ? AND password = ?",
                new String[]{username, password}
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    public long addWeightEntry(int userId, String date, double weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("entry_date", date);
        values.put("weight", weight);

        return db.insert(TABLE_WEIGHTS, null, values);
    }

    public boolean updateWeightEntry(int entryId, String date, double weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("entry_date", date);
        values.put("weight", weight);

        int rowsUpdated = db.update(
                TABLE_WEIGHTS,
                values,
                "id = ?",
                new String[]{String.valueOf(entryId)}
        );
        return rowsUpdated > 0;
    }

    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_WEIGHTS,
                "id = ?",
                new String[]{String.valueOf(entryId)}
        );
        return rowsDeleted > 0;
    }

    public List<WeightEntry> getWeightEntries(int userId) {
        ArrayList<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, entry_date, weight FROM " + TABLE_WEIGHTS +
                        " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {
            entries.add(new WeightEntry(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getDouble(2)
            ));
        }
        cursor.close();
        return entries;
    }

    public void setGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("goal_weight", goalWeight);

        int rowsUpdated = db.update(
                TABLE_GOALS,
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        if (rowsUpdated == 0) {
            db.insert(TABLE_GOALS, null, values);
        }
    }

    public Double getGoalWeight(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT goal_weight FROM " + TABLE_GOALS + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        Double goalWeight = null;
        if (cursor.moveToFirst()) {
            goalWeight = cursor.getDouble(0);
        }
        cursor.close();
        return goalWeight;
    }

    public static class WeightEntry {
        public final int id;
        public final String date;
        public final double weight;

        public WeightEntry(int id, String date, double weight) {
            this.id = id;
            this.date = date;
            this.weight = weight;
        }
    }
}
