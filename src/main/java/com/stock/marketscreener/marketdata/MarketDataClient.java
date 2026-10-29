package com.stock.marketscreener.marketdata;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Tick;
import com.stock.marketscreener.model.Timeframe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface MarketDataClient {
    List<Candle> getHistoricalCandles(String instrumentKey, Timeframe timeframe, LocalDateTime from, LocalDateTime to);
    List<Candle> getLatestCandles(String instrumentKey, Timeframe timeframe, int limit);
    void subscribe(Set<String> instrumentKeys, Consumer<Tick> tickConsumer);
    void unsubscribe(Set<String> instrumentKeys);
    boolean isConnected();
}
