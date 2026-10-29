package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class OrbDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 2) return null;
        Candle last = candles.get(candles.size() - 1);
        Candle prev = candles.get(candles.size() - 2);
        if (last.getClose().compareTo(prev.getHigh()) > 0) {
            return new PatternResult(PatternType.ORB, TradeDirection.LONG, last.getClose(), last.getClose().subtract(BigDecimal.valueOf(1.5)), BigDecimal.valueOf(0.76), true, "Opening range breakout");
        }
        if (last.getClose().compareTo(prev.getLow()) < 0) {
            return new PatternResult(PatternType.ORB, TradeDirection.SHORT, last.getClose(), last.getClose().add(BigDecimal.valueOf(1.5)), BigDecimal.valueOf(0.72), true, "Opening range breakdown");
        }
        return null;
    }
}
