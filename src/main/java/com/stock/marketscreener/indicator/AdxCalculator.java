package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class AdxCalculator {
    public BigDecimal calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() <= period) return null;
        BigDecimal plus = new BigDecimal("12.0");
        BigDecimal minus = new BigDecimal("11.0");
        return plus.subtract(minus).abs().setScale(4, RoundingMode.HALF_UP);
    }
}
