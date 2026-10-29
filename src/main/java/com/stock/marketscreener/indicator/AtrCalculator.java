package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class AtrCalculator {
    public BigDecimal calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() <= period) return null;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 1; i <= period; i++) {
            Candle prev = candles.get(candles.size() - i - 1);
            Candle curr = candles.get(candles.size() - i);
            BigDecimal tr = curr.getHigh().max(prev.getClose()).subtract(curr.getLow().min(prev.getClose()));
            sum = sum.add(tr);
        }
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }
}
