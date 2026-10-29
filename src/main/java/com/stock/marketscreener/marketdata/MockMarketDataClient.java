package com.stock.marketscreener.marketdata;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Tick;
import com.stock.marketscreener.model.Timeframe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Explicit opt-in synthetic development provider. NEVER represents real prices.
 * dataSource = "MOCK", live = false.
 */
public class MockMarketDataClient implements MarketDataClient {
    private static final String DATA_SOURCE = "MOCK";

    @Override
    public List<Candle> getHistoricalCandles(String instrumentKey, Timeframe timeframe, LocalDateTime from, LocalDateTime to) {
        List<Candle> candles = new ArrayList<>();
        LocalDateTime current = from;
        BigDecimal price = new BigDecimal("100.00");
        int i = 0;
        while (!current.isAfter(to) && candles.size() < 200) {
            BigDecimal open = price;
            BigDecimal close = price.add(BigDecimal.valueOf(Math.sin(i / 4.0) * 2.5 + 0.7));
            BigDecimal high = open.max(close).add(BigDecimal.valueOf(1.8));
            BigDecimal low = open.min(close).subtract(BigDecimal.valueOf(1.5));
            candles.add(new Candle(1L, instrumentKey, "NSE", timeframe, current, open, high, low, close, 1200L + i * 50L, 0L, DATA_SOURCE, false));
            price = close;
            current = switch (timeframe) {
                case ONE_MINUTE -> current.plusMinutes(1);
                case FIVE_MINUTE -> current.plusMinutes(5);
                case FIFTEEN_MINUTE -> current.plusMinutes(15);
                case ONE_HOUR -> current.plusHours(1);
                case ONE_DAY -> current.plusDays(1);
                case ONE_WEEK -> current.plusWeeks(1);
            };
            i++;
        }
        return candles;
    }

    @Override
    public List<Candle> getLatestCandles(String instrumentKey, Timeframe timeframe, int limit) {
        LocalDateTime now = LocalDateTime.now(com.stock.marketscreener.util.MarketTimeUtil.INDIA_ZONE);
        return getHistoricalCandles(instrumentKey, timeframe, now.minusDays(7), now);
    }

    @Override
    public void subscribe(Set<String> instrumentKeys, Consumer<Tick> tickConsumer) {
    }

    @Override
    public void unsubscribe(Set<String> instrumentKeys) {
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
