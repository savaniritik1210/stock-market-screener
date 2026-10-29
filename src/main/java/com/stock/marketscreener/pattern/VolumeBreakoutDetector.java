package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.PatternType;
import com.stock.marketscreener.model.TradeDirection;

import java.math.BigDecimal;
import java.util.List;

public class VolumeBreakoutDetector implements PatternDetector {
    @Override
    public PatternResult detect(List<Candle> candles) {
        if (candles == null || candles.size() < 10) return null;
        long avg = 0L;
        for (int i = 0; i < candles.size() - 1; i++) avg += candles.get(i).getVolume();
        avg /= (candles.size() - 1);
        long lastVol = candles.get(candles.size() - 1).getVolume();
        if (lastVol > avg * 1.5) {
            return new PatternResult(PatternType.VOLUME_BREAKOUT, TradeDirection.LONG, candles.get(candles.size() - 1).getClose(), candles.get(candles.size() - 1).getClose().subtract(BigDecimal.valueOf(1.2)), BigDecimal.valueOf(0.74), true, "Volume breakout");
        }
        return null;
    }
}
