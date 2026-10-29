package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.util.List;

public class TimeframeAnalysis {
    private Timeframe timeframe;
    private MarketDirection direction;
    private BigDecimal currentPrice;
    private IndicatorSnapshot indicatorSnapshot;
    private MarketStructure marketStructure;
    private List<PatternResult> patterns;
    private List<SupportResistanceLevel> supportResistance;

    public TimeframeAnalysis() {}

    public TimeframeAnalysis(Timeframe timeframe, MarketDirection direction, BigDecimal currentPrice,
                            IndicatorSnapshot indicatorSnapshot, MarketStructure marketStructure,
                            List<PatternResult> patterns, List<SupportResistanceLevel> supportResistance) {
        this.timeframe = timeframe;
        this.direction = direction;
        this.currentPrice = currentPrice;
        this.indicatorSnapshot = indicatorSnapshot;
        this.marketStructure = marketStructure;
        this.patterns = patterns;
        this.supportResistance = supportResistance;
    }

    public Timeframe getTimeframe() { return timeframe; }
    public void setTimeframe(Timeframe timeframe) { this.timeframe = timeframe; }
    public MarketDirection getDirection() { return direction; }
    public void setDirection(MarketDirection direction) { this.direction = direction; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public IndicatorSnapshot getIndicatorSnapshot() { return indicatorSnapshot; }
    public void setIndicatorSnapshot(IndicatorSnapshot indicatorSnapshot) { this.indicatorSnapshot = indicatorSnapshot; }
    public MarketStructure getMarketStructure() { return marketStructure; }
    public void setMarketStructure(MarketStructure marketStructure) { this.marketStructure = marketStructure; }
    public List<PatternResult> getPatterns() { return patterns; }
    public void setPatterns(List<PatternResult> patterns) { this.patterns = patterns; }
    public List<SupportResistanceLevel> getSupportResistance() { return supportResistance; }
    public void setSupportResistance(List<SupportResistanceLevel> supportResistance) { this.supportResistance = supportResistance; }
}
