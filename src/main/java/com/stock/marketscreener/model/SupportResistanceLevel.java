package com.stock.marketscreener.model;

import java.math.BigDecimal;

public class SupportResistanceLevel {
    private BigDecimal levelPrice;
    private String levelType;
    private BigDecimal strengthScore;
    private Integer touchCount;

    public SupportResistanceLevel() {}

    public SupportResistanceLevel(BigDecimal levelPrice, String levelType, BigDecimal strengthScore, Integer touchCount) {
        this.levelPrice = levelPrice;
        this.levelType = levelType;
        this.strengthScore = strengthScore;
        this.touchCount = touchCount;
    }

    public BigDecimal getLevelPrice() { return levelPrice; }
    public void setLevelPrice(BigDecimal levelPrice) { this.levelPrice = levelPrice; }
    public String getLevelType() { return levelType; }
    public void setLevelType(String levelType) { this.levelType = levelType; }
    public BigDecimal getStrengthScore() { return strengthScore; }
    public void setStrengthScore(BigDecimal strengthScore) { this.strengthScore = strengthScore; }
    public Integer getTouchCount() { return touchCount; }
    public void setTouchCount(Integer touchCount) { this.touchCount = touchCount; }
}
