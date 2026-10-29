package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ObvCalculator {
    public BigDecimal calculate(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) return null;
        BigDecimal obv = BigDecimal.ZERO;
        for (Candle candle : candles) {
            BigDecimal volume = BigDecimal.valueOf(candle.getVolume() == null ? 0L : candle.getVolume());
            if (candle.getClose().compareTo(candle.getOpen()) >= 0) {
                obv = obv.add(volume);
            } else {
                obv = obv.subtract(volume);
            }
        }
        return obv;
    }
}
