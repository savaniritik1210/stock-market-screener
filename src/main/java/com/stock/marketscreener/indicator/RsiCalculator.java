package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RsiCalculator {
    public BigDecimal calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() <= period) return null;
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        for (int i = 1; i <= period; i++) {
            BigDecimal delta = candles.get(candles.size() - i).getClose().subtract(candles.get(candles.size() - i - 1).getClose());
            if (delta.compareTo(BigDecimal.ZERO) > 0) gains = gains.add(delta);
            else losses = losses.add(delta.abs());
        }
        if (losses.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100");
        if (gains.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("0");
        BigDecimal rs = gains.divide(losses, 10, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 10, RoundingMode.HALF_UP));
    }
}
