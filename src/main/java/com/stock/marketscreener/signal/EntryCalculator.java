package com.stock.marketscreener.signal;

import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;

public class EntryCalculator {
    public BigDecimal calculate(BigDecimal currentPrice, TradeDirection direction) {
        if (direction == TradeDirection.LONG) return currentPrice.add(BigDecimal.valueOf(0.5));
        if (direction == TradeDirection.SHORT) return currentPrice.subtract(BigDecimal.valueOf(0.5));
        return currentPrice;
    }
}
