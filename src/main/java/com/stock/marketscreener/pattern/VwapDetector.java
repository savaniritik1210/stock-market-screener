package com.stock.marketscreener.pattern;

import com.stock.marketscreener.indicator.VwapCalculator;
import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class VwapDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 5) return null;
        BigDecimal vwap = new VwapCalculator().calculate(candles);
        Candle last = candles.get(candles.size() - 1);
        if (vwap == null) return null;
        if (last.getClose().compareTo(vwap) > 0) {
            return new PatternResult(PatternType.VWAP, TradeDirection.LONG, vwap, vwap.subtract(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.70), true, "Price above VWAP");
        }
        if (last.getClose().compareTo(vwap) < 0) {
            return new PatternResult(PatternType.VWAP, TradeDirection.SHORT, vwap, vwap.add(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.68), true, "Price below VWAP");
        }
        return null;
    }
}
