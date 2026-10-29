package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class SmaCalculator {
    public BigDecimal calculate(List<Candle> candles, int period) {
        if (candles == null || candles.isEmpty() || candles.size() < period) return null;
        List<Candle> slice = candles.subList(candles.size() - period, candles.size());
        BigDecimal sum = BigDecimal.ZERO;
        for (Candle candle : slice) {
            sum = sum.add(candle.getClose());
        }
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }
}
