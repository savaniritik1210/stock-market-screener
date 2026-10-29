package com.stock.marketscreener.model;

public class SignalReason {
    private String category;
    private String code;
    private String description;
    private boolean positive;
    private double weight;

    public SignalReason() {}

    public SignalReason(String category, String code, String description, boolean positive, double weight) {
        this.category = category;
        this.code = code;
        this.description = description;
        this.positive = positive;
        this.weight = weight;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPositive() { return positive; }
    public void setPositive(boolean positive) { this.positive = positive; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
