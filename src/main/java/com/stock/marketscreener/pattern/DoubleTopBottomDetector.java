package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class DoubleTopBottomDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 3) return null;
        Candle op = candles.get(candles.size() - 3);
        Candle mid = candles.get(candles.size() - 2);
        Candle last = candles.get(candles.size() - 1);
        if (op.getHigh().compareTo(mid.getHigh()) >= 0 && mid.getHigh().compareTo(last.getHigh()) >= 0) {
            return new PatternResult(PatternType.DOUBLE_TOP, TradeDirection.SHORT, last.getClose(), last.getClose().add(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.8), true, "Double top");
        }
        if (op.getLow().compareTo(mid.getLow()) <= 0 && mid.getLow().compareTo(last.getLow()) <= 0) {
            return new PatternResult(PatternType.DOUBLE_BOTTOM, TradeDirection.LONG, last.getClose(), last.getClose().subtract(BigDecimal.valueOf(1.0)), BigDecimal.valueOf(0.8), true, "Double bottom");
        }
        return null;
    }
}
