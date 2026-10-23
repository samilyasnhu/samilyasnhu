package com.cs360.cs360projecttwo_weighttracker.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeightAnalyzerTest {

    @Test
    public void sortChronologicallyOrdersByDateInsteadOfInsertionId() {
        List<WeightRecord> input = Arrays.asList(
                new WeightRecord(10, "01/05/2026", 195.0),
                new WeightRecord(20, "12/31/2025", 200.0),
                new WeightRecord(30, "01/02/2026", 198.0)
        );

        List<WeightRecord> sorted = WeightAnalyzer.sortChronologically(input);

        assertEquals("12/31/2025", sorted.get(0).getDate());
        assertEquals("01/02/2026", sorted.get(1).getDate());
        assertEquals("01/05/2026", sorted.get(2).getDate());
        assertEquals("01/05/2026", input.get(0).getDate());
    }

    @Test
    public void analyzeCalculatesSummaryAndSlidingWindowAverage() {
        List<WeightRecord> records = records(
                200.0, 198.0, 197.0, 196.0, 195.0, 194.0, 193.0, 192.0
        );

        WeightAnalysis result = WeightAnalyzer.analyze(records, 180.0, 7);

        assertNotNull(result);
        assertEquals(8, result.getEntryCount());
        assertEquals(200.0, result.getStartingWeight(), 0.001);
        assertEquals(192.0, result.getCurrentWeight(), 0.001);
        assertEquals(-8.0, result.getTotalChange(), 0.001);
        assertEquals(195.625, result.getAverageWeight(), 0.001);
        assertEquals(192.0, result.getMinimumWeight(), 0.001);
        assertEquals(200.0, result.getMaximumWeight(), 0.001);
        assertEquals(2, result.getMovingAverages().size());
        assertEquals(196.142857, result.getMovingAverages().get(0), 0.001);
        assertEquals(195.0, result.getLatestMovingAverage(), 0.001);
        assertEquals("Decreasing", result.getTrend());
        assertEquals(12.0, result.getGoalDistance(), 0.001);
        assertEquals(40.0, result.getGoalProgressPercent(), 0.001);
    }

    @Test
    public void filterByDateRangeUsesInclusiveBoundaries() {
        List<WeightRecord> sorted = WeightAnalyzer.sortChronologically(records(
                200.0, 199.0, 198.0, 197.0, 196.0, 195.0
        ));

        List<WeightRecord> filtered = WeightAnalyzer.filterByDateRange(
                sorted,
                LocalDate.of(2026, 1, 3),
                LocalDate.of(2026, 1, 5)
        );

        assertEquals(3, filtered.size());
        assertEquals("01/03/2026", filtered.get(0).getDate());
        assertEquals("01/05/2026", filtered.get(2).getDate());
    }

    @Test
    public void goalProgressCapsAtOneHundredAfterCrossingGoal() {
        List<WeightRecord> records = Arrays.asList(
                new WeightRecord(1, "01/01/2026", 200.0),
                new WeightRecord(2, "01/02/2026", 175.0)
        );

        WeightAnalysis result = WeightAnalyzer.analyze(records, 180.0, 7);

        assertNotNull(result);
        assertEquals(100.0, result.getGoalProgressPercent(), 0.001);
        assertEquals(5.0, result.getGoalDistance(), 0.001);
    }

    private List<WeightRecord> records(double... weights) {
        ArrayList<WeightRecord> records = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            records.add(new WeightRecord(i + 1, String.format("01/%02d/2026", i + 1), weights[i]));
        }
        return records;
    }
}
