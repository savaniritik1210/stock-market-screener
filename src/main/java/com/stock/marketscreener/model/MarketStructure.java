package com.stock.marketscreener.model;

import java.math.BigDecimal;

public class MarketStructure {
    private MarketDirection trend;
    private boolean higherHigh;
    private boolean higherLow;
    private boolean lowerHigh;
    private boolean lowerLow;
    private BigDecimal range;
    private boolean breakOfStructure;

    public MarketDirection getTrend() { return trend; }
    public void setTrend(MarketDirection trend) { this.trend = trend; }
    public boolean isHigherHigh() { return higherHigh; }
    public void setHigherHigh(boolean higherHigh) { this.higherHigh = higherHigh; }
    public boolean isHigherLow() { return higherLow; }
    public void setHigherLow(boolean higherLow) { this.higherLow = higherLow; }
    public boolean isLowerHigh() { return lowerHigh; }
    public void setLowerHigh(boolean lowerHigh) { this.lowerHigh = lowerHigh; }
    public boolean isLowerLow() { return lowerLow; }
    public void setLowerLow(boolean lowerLow) { this.lowerLow = lowerLow; }
    public BigDecimal getRange() { return range; }
    public void setRange(BigDecimal range) { this.range = range; }
    public boolean isBreakOfStructure() { return breakOfStructure; }
    public void setBreakOfStructure(boolean breakOfStructure) { this.breakOfStructure = breakOfStructure; }
}
