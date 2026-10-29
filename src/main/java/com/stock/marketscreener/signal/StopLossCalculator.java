package com.stock.marketscreener.signal;

import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;

public class StopLossCalculator {
    public BigDecimal calculate(BigDecimal entry, BigDecimal currentPrice, TradeDirection direction) {
        if (direction == TradeDirection.LONG) {
            return entry.subtract(BigDecimal.valueOf(1.5));
        }
        if (direction == TradeDirection.SHORT) {
            return entry.add(BigDecimal.valueOf(1.5));
        }
        return currentPrice;
    }
}
