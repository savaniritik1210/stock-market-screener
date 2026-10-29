package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class FlagDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 5) return null;
        Candle last = candles.get(candles.size() - 1);
        if (last.getClose().compareTo(candles.get(0).getClose()) > 0) {
            return new PatternResult(PatternType.FLAG, TradeDirection.LONG, last.getClose(), last.getClose().subtract(BigDecimal.valueOf(0.8)), BigDecimal.valueOf(0.70), true, "Bull flag");
        }
        return null;
    }
}
