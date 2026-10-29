package com.stock.marketscreener.analysis;

import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Timeframe;
import com.stock.marketscreener.util.MarketTimeUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class CompletedCandles {
    private CompletedCandles() {}

    public static LocalDateTime end(Candle candle) {
        return switch (candle.getTimeframe()) {
            case ONE_MINUTE -> candle.getTimestamp().plusMinutes(1);
            case FIVE_MINUTE -> candle.getTimestamp().plusMinutes(5);
            case FIFTEEN_MINUTE -> candle.getTimestamp().plusMinutes(15);
            case ONE_HOUR -> candle.getTimestamp().plusHours(1);
            case ONE_DAY -> candle.getTimestamp().toLocalDate().atTime(15, 30);
            case ONE_WEEK -> candle.getTimestamp().plusDays(7);
        };
    }

    public static List<Candle> filter(List<Candle> input, LocalDateTime now) {
        if (input == null) throw new MarketDataException("Provider returned no candles");
        List<Candle> result = new ArrayList<>();
        LocalDateTime previous = null;
        for (Candle c : input) {
            if (c == null || c.getTimestamp() == null || c.getTimeframe() == null
                || c.getOpen() == null || c.getHigh() == null || c.getLow() == null || c.getClose() == null
                || c.getVolume() == null || c.getVolume() < 0 || c.getLow().signum() <= 0
                || c.getHigh().compareTo(c.getOpen().max(c.getClose())) < 0
                || c.getLow().compareTo(c.getOpen().min(c.getClose())) > 0) {
                throw new MarketDataException("Provider returned invalid or missing OHLC/volume");
            }
            if (previous != null && !c.getTimestamp().isAfter(previous)) {
                throw new MarketDataException("Provider candles must be chronological and unique");
            }
            previous = c.getTimestamp();
            boolean daily = c.getTimeframe() == Timeframe.ONE_DAY || c.getTimeframe() == Timeframe.ONE_WEEK;
            if (MarketTimeUtil.isWeekend(c.getTimestamp())) continue;
            if (!daily && (c.getTimestamp().toLocalTime().isBefore(LocalTime.of(9, 15))
                || end(c).toLocalTime().isAfter(LocalTime.of(15, 30)))) continue;
            // A delayed provider may still return the current, incomplete bar.
            LocalDateTime availableThrough = c.isLive() ? now : now.minusMinutes(20);
            if (!end(c).isAfter(availableThrough)) result.add(c);
        }
        return List.copyOf(result);
    }
}
