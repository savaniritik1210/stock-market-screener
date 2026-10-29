package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class VwapCalculator {
    public BigDecimal calculate(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) return null;
        BigDecimal totalPriceVolume = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (Candle candle : candles) {
            BigDecimal typicalPrice = candle.getHigh().add(candle.getLow()).add(candle.getClose()).divide(BigDecimal.valueOf(3), 6, RoundingMode.HALF_UP);
            BigDecimal volume = BigDecimal.valueOf(candle.getVolume() == null ? 0L : candle.getVolume());
            totalPriceVolume = totalPriceVolume.add(typicalPrice.multiply(volume));
            totalVolume = totalVolume.add(volume);
        }
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) return null;
        return totalPriceVolume.divide(totalVolume, 4, RoundingMode.HALF_UP);
    }
}
