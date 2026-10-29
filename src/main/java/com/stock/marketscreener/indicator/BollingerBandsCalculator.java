package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BollingerBandsCalculator {
    public BigDecimal middle(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period) return null;
        List<Candle> window = candles.subList(candles.size() - period, candles.size());
        BigDecimal sum = BigDecimal.ZERO;
        for (Candle candle : window) sum = sum.add(candle.getClose());
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal upper(List<Candle> candles, int period) {
        BigDecimal middle = middle(candles, period);
        if (middle == null) return null;
        return middle.add(BigDecimal.valueOf(2));
    }

    public BigDecimal lower(List<Candle> candles, int period) {
        BigDecimal middle = middle(candles, period);
        if (middle == null) return null;
        return middle.subtract(BigDecimal.valueOf(2));
    }
}
