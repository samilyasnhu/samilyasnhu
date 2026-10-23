package com.cs360.cs360projecttwo_weighttracker.data;

import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import java.util.List;

/** Data-access contract used by the service layer. */
public interface WeightRepository {
    boolean createUser(String username, String password);
    int authenticateUser(String username, String password);
    long saveWeightEntry(int userId, String date, double weight, Double goalWeight);
    boolean updateWeightEntry(int userId, int entryId, String date, double weight);
    boolean deleteWeightEntry(int userId, int entryId);
    List<WeightRecord> getWeightEntries(int userId);
    List<WeightRecord> getWeightEntriesInRange(int userId, String startDate, String endDate);
    boolean setGoalWeight(int userId, double goalWeight);
    Double getGoalWeight(int userId);
}
