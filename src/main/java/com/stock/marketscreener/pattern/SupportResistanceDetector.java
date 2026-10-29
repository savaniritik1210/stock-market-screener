package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.SupportResistanceLevel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SupportResistanceDetector {
    public List<SupportResistanceLevel> detect(List<Candle> candles) {
        List<SupportResistanceLevel> levels = new ArrayList<>();
        if (candles == null || candles.isEmpty()) return levels;
        Candle last = candles.get(candles.size() - 1);
        levels.add(new SupportResistanceLevel(last.getHigh(), "RESISTANCE", BigDecimal.valueOf(0.75), 2));
        levels.add(new SupportResistanceLevel(last.getLow(), "SUPPORT", BigDecimal.valueOf(0.75), 2));
        return levels;
    }
}
