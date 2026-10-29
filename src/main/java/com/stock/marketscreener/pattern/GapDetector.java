package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class GapDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 2) return null;
        Candle prev = candles.get(candles.size() - 2);
        Candle last = candles.get(candles.size() - 1);
        if (last.getOpen().compareTo(prev.getClose()) > 0) {
            return new PatternResult(PatternType.GAP, TradeDirection.LONG, last.getOpen(), last.getOpen().subtract(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.75), true, "Gap up");
        }
        if (last.getOpen().compareTo(prev.getClose()) < 0) {
            return new PatternResult(PatternType.GAP, TradeDirection.SHORT, last.getOpen(), last.getOpen().add(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.75), true, "Gap down");
        }
        return null;
    }
}
