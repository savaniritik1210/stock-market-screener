package com.stock.marketscreener.service;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.signal.SignalEngine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignalService {
    private final SignalEngine signalEngine;

    public SignalService(SignalEngine signalEngine) {
        this.signalEngine = signalEngine;
    }

    public StockSignal analyze(String symbol, List<Candle> candles) {
        return signalEngine.evaluate(symbol, candles);
    }
}
