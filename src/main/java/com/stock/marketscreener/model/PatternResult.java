package com.stock.marketscreener.model;

import java.math.BigDecimal;

public class PatternResult {
    private PatternType patternType;
    private TradeDirection direction;
    private BigDecimal triggerPrice;
    private BigDecimal invalidationPrice;
    private BigDecimal confidence;
    private boolean confirmed;
    private String details;

    public PatternResult() {}

    public PatternResult(PatternType patternType, TradeDirection direction, BigDecimal triggerPrice,
                        BigDecimal invalidationPrice, BigDecimal confidence, boolean confirmed, String details) {
        this.patternType = patternType;
        this.direction = direction;
        this.triggerPrice = triggerPrice;
        this.invalidationPrice = invalidationPrice;
        this.confidence = confidence;
        this.confirmed = confirmed;
        this.details = details;
    }

    public PatternType getPatternType() { return patternType; }
    public void setPatternType(PatternType patternType) { this.patternType = patternType; }
    public TradeDirection getDirection() { return direction; }
    public void setDirection(TradeDirection direction) { this.direction = direction; }
    public BigDecimal getTriggerPrice() { return triggerPrice; }
    public void setTriggerPrice(BigDecimal triggerPrice) { this.triggerPrice = triggerPrice; }
    public BigDecimal getInvalidationPrice() { return invalidationPrice; }
    public void setInvalidationPrice(BigDecimal invalidationPrice) { this.invalidationPrice = invalidationPrice; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
