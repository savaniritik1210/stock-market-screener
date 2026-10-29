package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class VolumeCalculator {
    public BigDecimal calculateSma(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period) return null;
        long sum = 0L;
        for (int i = candles.size() - period; i < candles.size(); i++) {
            sum += candles.get(i).getVolume() == null ? 0L : candles.get(i).getVolume();
        }
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal relativeVolume(List<Candle> candles) {
        if (candles == null || candles.size() < 2) return BigDecimal.ZERO;
        long current = candles.get(candles.size() - 1).getVolume() == null ? 0L : candles.get(candles.size() - 1).getVolume();
        long avg = 0L;
        for (int i = 1; i < candles.size(); i++) {
            avg += candles.get(candles.size() - 1 - i).getVolume() == null ? 0L : candles.get(candles.size() - 1 - i).getVolume();
        }
        avg = Math.max(1L, avg / (candles.size() - 1));
        return BigDecimal.valueOf(current).divide(BigDecimal.valueOf(avg), 4, RoundingMode.HALF_UP);
    }
}
