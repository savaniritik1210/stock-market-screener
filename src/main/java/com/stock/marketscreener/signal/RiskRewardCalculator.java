package com.stock.marketscreener.signal;

import java.math.BigDecimal;

public class RiskRewardCalculator {
    public BigDecimal calculate(BigDecimal entry, BigDecimal stopLoss, BigDecimal target) {
        if (entry == null || stopLoss == null || target == null) return BigDecimal.ZERO;
        BigDecimal risk = entry.subtract(stopLoss).abs();
        BigDecimal reward = target.subtract(entry).abs();
        if (risk.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return reward.divide(risk, 4, java.math.RoundingMode.HALF_UP);
    }
}
