package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class InsideBarDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 2) return null;
        Candle prev = candles.get(candles.size() - 2);
        Candle last = candles.get(candles.size() - 1);
        if (last.getHigh().compareTo(prev.getHigh()) < 0 && last.getLow().compareTo(prev.getLow()) > 0) {
            return new PatternResult(PatternType.INSIDE_BAR, TradeDirection.LONG, last.getClose(), last.getClose().subtract(BigDecimal.valueOf(1.2)), BigDecimal.valueOf(0.73), true, "Inside bar breakout");
        }
        return null;
    }
}
