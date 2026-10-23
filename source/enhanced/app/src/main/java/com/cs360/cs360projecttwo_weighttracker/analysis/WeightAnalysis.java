package com.cs360.cs360projecttwo_weighttracker.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable result produced by WeightAnalyzer for a chronological set of weight records.
 */
public final class WeightAnalysis {
    private final int entryCount;
    private final double startingWeight;
    private final double currentWeight;
    private final double totalChange;
    private final double averageWeight;
    private final double minimumWeight;
    private final double maximumWeight;
    private final List<Double> movingAverages;
    private final String trend;
    private final Double goalDistance;
    private final Double goalProgressPercent;

    public WeightAnalysis(
            int entryCount,
            double startingWeight,
            double currentWeight,
            double totalChange,
            double averageWeight,
            double minimumWeight,
            double maximumWeight,
            List<Double> movingAverages,
            String trend,
            Double goalDistance,
            Double goalProgressPercent
    ) {
        this.entryCount = entryCount;
        this.startingWeight = startingWeight;
        this.currentWeight = currentWeight;
        this.totalChange = totalChange;
        this.averageWeight = averageWeight;
        this.minimumWeight = minimumWeight;
        this.maximumWeight = maximumWeight;
        this.movingAverages = Collections.unmodifiableList(new ArrayList<>(movingAverages));
        this.trend = trend;
        this.goalDistance = goalDistance;
        this.goalProgressPercent = goalProgressPercent;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public double getStartingWeight() {
        return startingWeight;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public double getTotalChange() {
        return totalChange;
    }

    public double getAverageWeight() {
        return averageWeight;
    }

    public double getMinimumWeight() {
        return minimumWeight;
    }

    public double getMaximumWeight() {
        return maximumWeight;
    }

    public List<Double> getMovingAverages() {
        return movingAverages;
    }

    public Double getLatestMovingAverage() {
        if (movingAverages.isEmpty()) {
            return null;
        }
        return movingAverages.get(movingAverages.size() - 1);
    }

    public String getTrend() {
        return trend;
    }

    public Double getGoalDistance() {
        return goalDistance;
    }

    public Double getGoalProgressPercent() {
        return goalProgressPercent;
    }
}
