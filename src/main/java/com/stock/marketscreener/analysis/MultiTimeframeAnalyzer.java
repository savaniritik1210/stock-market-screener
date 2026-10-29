package com.stock.marketscreener.analysis;

import com.stock.marketscreener.indicator.MarketStructureDetector;
import com.stock.marketscreener.model.*;

import java.util.ArrayList;
import java.util.List;

public class MultiTimeframeAnalyzer {
    public List<TimeframeAnalysis> analyze(String symbol, List<Candle> candles) {
        List<TimeframeAnalysis> result = new ArrayList<>();
        for (Timeframe timeframe : Timeframe.values()) {
            List<Candle> filtered = candles.stream().filter(c -> c.getTimeframe() == timeframe).toList();
            if (filtered.isEmpty()) continue;
            MarketStructure structure = new MarketStructureDetector().detect(filtered);
            result.add(new TimeframeAnalysis(timeframe, structure.getTrend(), filtered.get(filtered.size() - 1).getClose(), null, structure, List.of(), List.of()));
        }
        return result;
    }
}
