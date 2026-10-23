package com.cs360.cs360projecttwo_weighttracker.analysis;

import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Performs chronological sorting, date-range filtering, summary statistics, and moving-average
 * analysis for saved weight records.
 */
public final class WeightAnalyzer {
    public static final int DEFAULT_MOVING_AVERAGE_WINDOW = 7;
    private static final double TREND_EPSILON = 0.1;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("MM/dd/uuuu", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);

    private WeightAnalyzer() {
        // Utility class.
    }

    /**
     * Returns a new list ordered from oldest to newest. The input list is never modified.
     */
    public static List<WeightRecord> sortChronologically(List<WeightRecord> records) {
        ArrayList<WeightRecord> sorted = new ArrayList<>();
        if (records != null) {
            sorted.addAll(records);
        }

        sorted.sort(new Comparator<WeightRecord>() {
            @Override
            public int compare(WeightRecord first, WeightRecord second) {
                LocalDate firstDate = tryParseDate(first.getDate());
                LocalDate secondDate = tryParseDate(second.getDate());

                if (firstDate != null && secondDate != null) {
                    int dateComparison = firstDate.compareTo(secondDate);
                    if (dateComparison != 0) {
                        return dateComparison;
                    }
                    return Integer.compare(first.getId(), second.getId());
                }
                if (firstDate != null) {
                    return -1;
                }
                if (secondDate != null) {
                    return 1;
                }
                return Integer.compare(first.getId(), second.getId());
            }
        });
        return sorted;
    }

    /**
     * Filters an already chronological list using inclusive date boundaries. Binary searches find
     * the first and last matching positions before the selected range is copied.
     */
    public static List<WeightRecord> filterByDateRange(
            List<WeightRecord> sortedRecords,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (sortedRecords == null || sortedRecords.isEmpty()) {
            return new ArrayList<>();
        }

        int validCount = countValidDateRecords(sortedRecords);
        if (validCount == 0) {
            return new ArrayList<>();
        }

        int startIndex = startDate == null
                ? 0
                : lowerBound(sortedRecords, validCount, startDate);
        int endIndex = endDate == null
                ? validCount
                : upperBound(sortedRecords, validCount, endDate);

        if (startIndex >= endIndex) {
            return new ArrayList<>();
        }
        return new ArrayList<>(sortedRecords.subList(startIndex, endIndex));
    }

    /**
     * Sorts the supplied records and then calculates the analysis.
     */
    public static WeightAnalysis analyze(
            List<WeightRecord> records,
            Double goalWeight,
            int movingAverageWindow
    ) {
        if (records == null || records.isEmpty()) {
            return null;
        }
        return analyzeChronological(
                sortChronologically(records),
                goalWeight,
                movingAverageWindow
        );
    }

    /**
     * Calculates summary statistics in one pass for a list that is already oldest to newest.
     */
    public static WeightAnalysis analyzeChronological(
            List<WeightRecord> sorted,
            Double goalWeight,
            int movingAverageWindow
    ) {
        if (sorted == null || sorted.isEmpty()) {
            return null;
        }
        if (movingAverageWindow <= 0) {
            throw new IllegalArgumentException("Moving-average window must be greater than zero.");
        }

        double total = 0.0;
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;

        Deque<Double> window = new ArrayDeque<>();
        ArrayList<Double> movingAverages = new ArrayList<>();
        double runningWindowSum = 0.0;

        for (WeightRecord record : sorted) {
            double weight = record.getWeight();
            total += weight;
            minimum = Math.min(minimum, weight);
            maximum = Math.max(maximum, weight);

            window.addLast(weight);
            runningWindowSum += weight;
            if (window.size() > movingAverageWindow) {
                runningWindowSum -= window.removeFirst();
            }
            if (window.size() == movingAverageWindow) {
                movingAverages.add(runningWindowSum / movingAverageWindow);
            }
        }

        double startingWeight = sorted.get(0).getWeight();
        double currentWeight = sorted.get(sorted.size() - 1).getWeight();
        double totalChange = currentWeight - startingWeight;
        double averageWeight = total / sorted.size();
        String trend = determineTrend(sorted, movingAverages);

        Double goalDistance = null;
        Double goalProgress = null;
        if (goalWeight != null) {
            goalDistance = Math.abs(currentWeight - goalWeight);
            goalProgress = calculateGoalProgress(startingWeight, currentWeight, goalWeight);
        }

        return new WeightAnalysis(
                sorted.size(),
                startingWeight,
                currentWeight,
                totalChange,
                averageWeight,
                minimum,
                maximum,
                movingAverages,
                trend,
                goalDistance,
                goalProgress
        );
    }

    public static LocalDate parseDate(String dateText) {
        return LocalDate.parse(dateText, DATE_FORMATTER);
    }

    private static String determineTrend(
            List<WeightRecord> sorted,
            List<Double> movingAverages
    ) {
        double difference;
        if (movingAverages.size() >= 2) {
            difference = movingAverages.get(movingAverages.size() - 1) - movingAverages.get(0);
        } else {
            difference = sorted.get(sorted.size() - 1).getWeight() - sorted.get(0).getWeight();
        }

        if (difference > TREND_EPSILON) {
            return "Increasing";
        }
        if (difference < -TREND_EPSILON) {
            return "Decreasing";
        }
        return "Stable";
    }

    private static double calculateGoalProgress(
            double startingWeight,
            double currentWeight,
            double goalWeight
    ) {
        double initialDistance = Math.abs(startingWeight - goalWeight);
        if (initialDistance < 0.000001) {
            return 100.0;
        }

        boolean crossedGoal = (startingWeight > goalWeight && currentWeight <= goalWeight)
                || (startingWeight < goalWeight && currentWeight >= goalWeight);
        if (crossedGoal) {
            return 100.0;
        }

        double currentDistance = Math.abs(currentWeight - goalWeight);
        double progress = (1.0 - (currentDistance / initialDistance)) * 100.0;
        return Math.max(0.0, Math.min(100.0, progress));
    }

    private static int countValidDateRecords(List<WeightRecord> sortedRecords) {
        int count = 0;
        for (WeightRecord record : sortedRecords) {
            if (tryParseDate(record.getDate()) == null) {
                break;
            }
            count++;
        }
        return count;
    }

    private static int lowerBound(
            List<WeightRecord> records,
            int limit,
            LocalDate target
    ) {
        int low = 0;
        int high = limit;
        while (low < high) {
            int middle = low + (high - low) / 2;
            LocalDate date = parseDate(records.get(middle).getDate());
            if (date.isBefore(target)) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static int upperBound(
            List<WeightRecord> records,
            int limit,
            LocalDate target
    ) {
        int low = 0;
        int high = limit;
        while (low < high) {
            int middle = low + (high - low) / 2;
            LocalDate date = parseDate(records.get(middle).getDate());
            if (!date.isAfter(target)) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static LocalDate tryParseDate(String dateText) {
        try {
            return parseDate(dateText);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
