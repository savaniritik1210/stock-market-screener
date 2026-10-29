package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class EmaCalculator {
    public BigDecimal calculate(List<Candle> candles, int period) {
        if (candles == null || candles.isEmpty() || candles.size() < period) return null;
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = candles.get(0).getClose();
        for (int i = 1; i < candles.size(); i++) {
            BigDecimal close = candles.get(i).getClose();
            ema = close.multiply(multiplier).add(ema.multiply(BigDecimal.ONE.subtract(multiplier))).setScale(4, RoundingMode.HALF_UP);
        }
        return ema;
    }
}
