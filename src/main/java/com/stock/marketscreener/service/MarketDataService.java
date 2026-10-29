package com.stock.marketscreener.service;

import com.stock.marketscreener.marketdata.MarketDataClient;
import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Timeframe;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketDataService {
    private final MarketDataClient marketDataClient;

    public MarketDataService(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    public List<Candle> getHistoricalCandles(String instrumentKey, Timeframe timeframe, LocalDateTime from, LocalDateTime to) {
        return marketDataClient.getHistoricalCandles(instrumentKey, timeframe, from, to);
    }

    public List<Candle> getLatestCandles(String instrumentKey, Timeframe timeframe, int limit) {
        return marketDataClient.getLatestCandles(instrumentKey, timeframe, limit);
    }
}
