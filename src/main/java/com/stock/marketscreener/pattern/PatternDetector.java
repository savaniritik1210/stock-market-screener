package com.stock.marketscreener.pattern;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.PatternResult;

import java.util.List;

public interface PatternDetector {
    PatternResult detect(List<Candle> candles);
}
