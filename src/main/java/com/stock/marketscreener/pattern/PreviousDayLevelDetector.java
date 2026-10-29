package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class PreviousDayLevelDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 2) return null;
        Candle last = candles.get(candles.size() - 1);
        Candle prev = candles.get(candles.size() - 2);
        if (last.getClose().compareTo(prev.getHigh()) > 0) {
            return new PatternResult(PatternType.PREVIOUS_DAY_LEVEL, TradeDirection.LONG, prev.getHigh(), prev.getHigh().subtract(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.7), true, "Break above prior high");
        }
        if (last.getClose().compareTo(prev.getLow()) < 0) {
            return new PatternResult(PatternType.PREVIOUS_DAY_LEVEL, TradeDirection.SHORT, prev.getLow(), prev.getLow().add(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.7), true, "Break below prior low");
        }
        return null;
    }
}
