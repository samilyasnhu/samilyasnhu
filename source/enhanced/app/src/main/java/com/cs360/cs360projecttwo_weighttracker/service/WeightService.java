package com.cs360.cs360projecttwo_weighttracker.service;

import com.cs360.cs360projecttwo_weighttracker.analysis.WeightAnalysis;
import com.cs360.cs360projecttwo_weighttracker.analysis.WeightAnalyzer;
import com.cs360.cs360projecttwo_weighttracker.data.WeightRepository;
import com.cs360.cs360projecttwo_weighttracker.domain.InputValidator;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Contains weight-tracking business rules, validation, and historical analysis. Activities remain
 * focused on rendering the interface and responding to user interaction.
 */
public class WeightService {
    private final WeightRepository repository;

    public WeightService(WeightRepository repository) {
        this.repository = repository;
    }

    public Result<AddWeightResult> addWeightEntry(
            int userId,
            String dateText,
            String weightText,
            String optionalGoalText
    ) {
        Result<String> dateResult = InputValidator.validateDate(dateText);
        if (!dateResult.isSuccessful()) {
            return Result.failure(dateResult.getMessage());
        }

        Result<Double> weightResult = InputValidator.validateWeight(weightText, "Weight");
        if (!weightResult.isSuccessful()) {
            return Result.failure(weightResult.getMessage());
        }

        Double updatedGoal = null;
        String goalText = optionalGoalText == null ? "" : optionalGoalText.trim();
        if (!goalText.isEmpty()) {
            Result<Double> goalResult = InputValidator.validateWeight(goalText, "Goal weight");
            if (!goalResult.isSuccessful()) {
                return Result.failure(goalResult.getMessage());
            }
            updatedGoal = goalResult.getValue();
        }

        long entryId = repository.saveWeightEntry(
                userId,
                dateResult.getValue(),
                weightResult.getValue(),
                updatedGoal
        );
        if (entryId < 0) {
            return Result.failure("Weight entry was not saved.");
        }

        return Result.success(new AddWeightResult(entryId, weightResult.getValue(), updatedGoal));
    }

    public Result<Double> updateWeightEntry(
            int userId,
            int entryId,
            String dateText,
            String weightText
    ) {
        Result<String> dateResult = InputValidator.validateDate(dateText);
        if (!dateResult.isSuccessful()) {
            return Result.failure(dateResult.getMessage());
        }

        Result<Double> weightResult = InputValidator.validateWeight(weightText, "Weight");
        if (!weightResult.isSuccessful()) {
            return Result.failure(weightResult.getMessage());
        }

        if (!repository.updateWeightEntry(
                userId,
                entryId,
                dateResult.getValue(),
                weightResult.getValue()
        )) {
            return Result.failure("Weight entry could not be updated.");
        }
        return Result.success(weightResult.getValue());
    }

    public Result<Boolean> deleteWeightEntry(int userId, int entryId) {
        if (!repository.deleteWeightEntry(userId, entryId)) {
            return Result.failure("Weight entry could not be deleted.");
        }
        return Result.success(true);
    }

    /** Returns all entries in chronological order. */
    public List<WeightRecord> getWeightEntries(int userId) {
        return WeightAnalyzer.sortChronologically(repository.getWeightEntries(userId));
    }

    /**
     * Validates an optional inclusive date range and asks the repository to perform the range query.
     */
    public Result<List<WeightRecord>> getWeightEntriesForRange(
            int userId,
            String startDateText,
            String endDateText
    ) {
        String startText = startDateText == null ? "" : startDateText.trim();
        String endText = endDateText == null ? "" : endDateText.trim();

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (!startText.isEmpty()) {
            Result<String> startResult = InputValidator.validateDate(startText);
            if (!startResult.isSuccessful()) {
                return Result.failure("Start date: " + startResult.getMessage());
            }
            startDate = WeightAnalyzer.parseDate(startResult.getValue());
        }

        if (!endText.isEmpty()) {
            Result<String> endResult = InputValidator.validateDate(endText);
            if (!endResult.isSuccessful()) {
                return Result.failure("End date: " + endResult.getMessage());
            }
            endDate = WeightAnalyzer.parseDate(endResult.getValue());
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return Result.failure("Start date cannot be after end date.");
        }

        List<WeightRecord> records = repository.getWeightEntriesInRange(
                userId,
                startText,
                endText
        );
        return Result.success(WeightAnalyzer.sortChronologically(records));
    }

    public WeightAnalysis analyzeWeightEntries(List<WeightRecord> records, Double goalWeight) {
        return WeightAnalyzer.analyzeChronological(
                records,
                goalWeight,
                WeightAnalyzer.DEFAULT_MOVING_AVERAGE_WINDOW
        );
    }

    public Double getGoalWeight(int userId) {
        return repository.getGoalWeight(userId);
    }

    public static final class AddWeightResult {
        private final long entryId;
        private final double currentWeight;
        private final Double updatedGoalWeight;

        public AddWeightResult(long entryId, double currentWeight, Double updatedGoalWeight) {
            this.entryId = entryId;
            this.currentWeight = currentWeight;
            this.updatedGoalWeight = updatedGoalWeight;
        }

        public long getEntryId() {
            return entryId;
        }

        public double getCurrentWeight() {
            return currentWeight;
        }

        public Double getUpdatedGoalWeight() {
            return updatedGoalWeight;
        }
    }
}
