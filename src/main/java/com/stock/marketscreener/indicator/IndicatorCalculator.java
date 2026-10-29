package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;

import java.util.List;

public interface IndicatorCalculator {
    double calculate(List<Candle> candles);
}
