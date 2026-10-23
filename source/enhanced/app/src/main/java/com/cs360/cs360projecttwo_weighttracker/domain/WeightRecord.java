package com.cs360.cs360projecttwo_weighttracker.domain;

/**
 * Immutable representation of a saved weight measurement used outside the database layer.
 */
public final class WeightRecord {
    private final int id;
    private final String date;
    private final double weight;

    public WeightRecord(int id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }
}
