package com.cs360.cs360projecttwo_weighttracker.data;

import android.content.Context;

import com.cs360.cs360projecttwo_weighttracker.DatabaseHelper;
import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import java.util.List;

/** SQLite implementation of the application's data-access contract. */
public class SQLiteWeightRepository implements WeightRepository {
    private final DatabaseHelper databaseHelper;

    public SQLiteWeightRepository(Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    @Override
    public boolean createUser(String username, String password) {
        return databaseHelper.createUser(username, password);
    }

    @Override
    public int authenticateUser(String username, String password) {
        return databaseHelper.getUserId(username, password);
    }

    @Override
    public long saveWeightEntry(int userId, String date, double weight, Double goalWeight) {
        return databaseHelper.saveWeightEntryAndGoal(userId, date, weight, goalWeight);
    }

    @Override
    public boolean updateWeightEntry(int userId, int entryId, String date, double weight) {
        return databaseHelper.updateWeightEntry(userId, entryId, date, weight);
    }

    @Override
    public boolean deleteWeightEntry(int userId, int entryId) {
        return databaseHelper.deleteWeightEntry(userId, entryId);
    }

    @Override
    public List<WeightRecord> getWeightEntries(int userId) {
        return databaseHelper.getWeightEntries(userId);
    }

    @Override
    public List<WeightRecord> getWeightEntriesInRange(int userId, String startDate, String endDate) {
        return databaseHelper.getWeightEntriesInRange(userId, startDate, endDate);
    }

    @Override
    public boolean setGoalWeight(int userId, double goalWeight) {
        return databaseHelper.setGoalWeight(userId, goalWeight);
    }

    @Override
    public Double getGoalWeight(int userId) {
        return databaseHelper.getGoalWeight(userId);
    }
}
