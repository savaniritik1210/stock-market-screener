package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.MarketDirection;
import com.stock.marketscreener.model.MarketStructure;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarketStructureDetector {
    public MarketStructure detect(List<Candle> candles) {
        MarketStructure marketStructure = new MarketStructure();
        if (candles == null || candles.size() < 2) {
            marketStructure.setTrend(MarketDirection.NEUTRAL);
            return marketStructure;
        }
        Candle latest = candles.get(candles.size() - 1);
        Candle prev = candles.get(candles.size() - 2);
        if (latest.getClose().compareTo(prev.getClose()) > 0) {
            marketStructure.setTrend(MarketDirection.BULLISH);
            marketStructure.setHigherHigh(true);
            marketStructure.setHigherLow(true);
        } else if (latest.getClose().compareTo(prev.getClose()) < 0) {
            marketStructure.setTrend(MarketDirection.BEARISH);
            marketStructure.setLowerHigh(true);
            marketStructure.setLowerLow(true);
        } else {
            marketStructure.setTrend(MarketDirection.NEUTRAL);
        }
        marketStructure.setRange(latest.getHigh().subtract(latest.getLow()));
        return marketStructure;
    }
}
