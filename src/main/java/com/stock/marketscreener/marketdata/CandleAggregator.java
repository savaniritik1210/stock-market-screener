package com.stock.marketscreener.marketdata;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Timeframe;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CandleAggregator {
    public List<Candle> aggregate(List<Candle> candles, Timeframe targetTimeframe) {
        if (candles == null || candles.isEmpty()) return Collections.emptyList();
        List<Candle> sorted = new ArrayList<>(candles);
        sorted.sort(Comparator.comparing(Candle::getTimestamp));
        List<Candle> aggregated = new ArrayList<>();
        List<Candle> bucket = new ArrayList<>();
        for (Candle candle : sorted) {
            if (bucket.isEmpty()) {
                bucket.add(candle);
                continue;
            }
            if (Duration.between(bucket.get(0).getTimestamp(), candle.getTimestamp()).toMinutes() < targetIntervalMinutes(targetTimeframe)) {
                bucket.add(candle);
            } else {
                aggregated.add(buildAggregate(bucket, targetTimeframe));
                bucket.clear();
                bucket.add(candle);
            }
        }
        if (!bucket.isEmpty()) aggregated.add(buildAggregate(bucket, targetTimeframe));
        return aggregated;
    }

    private int targetIntervalMinutes(Timeframe timeframe) {
        return switch (timeframe) {
            case ONE_MINUTE -> 1;
            case FIVE_MINUTE -> 5;
            case FIFTEEN_MINUTE -> 15;
            case ONE_HOUR -> 60;
            case ONE_DAY -> 1440;
            case ONE_WEEK -> 10080;
        };
    }

    private Candle buildAggregate(List<Candle> bucket, Timeframe targetTimeframe) {
        Candle first = bucket.get(0);
        Candle last = bucket.get(bucket.size() - 1);
        BigDecimal open = first.getOpen();
        BigDecimal high = bucket.stream().map(Candle::getHigh).max(BigDecimal::compareTo).orElse(open);
        BigDecimal low = bucket.stream().map(Candle::getLow).min(BigDecimal::compareTo).orElse(open);
        BigDecimal close = last.getClose();
        long volume = bucket.stream().mapToLong(c -> c.getVolume() == null ? 0L : c.getVolume()).sum();
        return new Candle(first.getInstrumentId(), first.getSymbol(), first.getExchange(), targetTimeframe, first.getTimestamp(), open, high, low, close, volume, first.getOpenInterest());
    }
}
