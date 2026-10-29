package com.stock.marketscreener.service;

import com.stock.marketscreener.analysis.*;
import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.marketdata.MarketDataClient;
import com.stock.marketscreener.model.*;
import com.stock.marketscreener.util.MarketTimeUtil;
import com.stock.marketscreener.util.SymbolValidator;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeComparisonService {
    private final MarketDataClient provider;
    private final BreakoutStrategy strategy;
    private final HistoricalEvaluator evaluator;
    private final TradeAnalysisProperties config;
    private final Clock clock;
    private final Map<String, Cached> cache = new LinkedHashMap<>();
    private record Cached(Instant expires, TradeComparison value) {}

    @org.springframework.beans.factory.annotation.Autowired
    public TradeComparisonService(MarketDataClient provider, BreakoutStrategy strategy,
                                  HistoricalEvaluator evaluator, TradeAnalysisProperties config) {
        this(provider, strategy, evaluator, config, Clock.system(MarketTimeUtil.INDIA_ZONE));
    }
    public TradeComparisonService(MarketDataClient provider, BreakoutStrategy strategy,
                                  HistoricalEvaluator evaluator, TradeAnalysisProperties config, Clock clock) {
        this.provider = provider;
        this.strategy = strategy;
        this.evaluator = evaluator;
        this.config = config;
        this.clock = clock;
    }

    public synchronized TradeComparison compare(String input) {
        String symbol = SymbolValidator.normalize(input);
        Instant instant = clock.instant();
        Cached cached = cache.get(symbol);
        if (cached != null && cached.expires().isAfter(instant)) return cached.value();
        LocalDateTime now = LocalDateTime.ofInstant(instant, MarketTimeUtil.INDIA_ZONE);
        TradeAssessment intraday = assess(symbol, TradeMode.INTRADAY, now);
        TradeAssessment swing = assess(symbol, TradeMode.SWING, now);
        String preferred = preferred(intraday.evidence(), swing.evidence());
        String reason = preferred.equals("NO_CLEAR_WINNER")
            ? "No defensible accuracy winner: both modes need sufficient held-out trades, separated descriptive 95% intervals and positive after-cost expectancy. Different horizons are not directly equivalent; win rate alone is not profitability."
            : preferred + " has the higher historical success estimate with separated descriptive 95% intervals and positive after-cost expectancy in both modes. This is not a guarantee of future accuracy or profit.";
        reason += " Intervals do not adjust for serial dependence or multiple comparisons; this fixed untrained strategy is not validated or calibrated.";
        TradeComparison result = new TradeComparison(symbol, now.atZone(MarketTimeUtil.INDIA_ZONE).toOffsetDateTime().toString(),
            intraday, swing, preferred, reason);
        cache.entrySet().removeIf(e -> !e.getValue().expires().isAfter(instant));
        while (cache.size() >= config.getCacheSize()) cache.remove(cache.keySet().iterator().next());
        cache.put(symbol, new Cached(instant.plusSeconds(config.getCacheSeconds()), result));
        return result;
    }

    private TradeAssessment assess(String symbol, TradeMode mode, LocalDateTime now) {
        Timeframe timeframe = mode == TradeMode.INTRADAY ? Timeframe.FIFTEEN_MINUTE : Timeframe.ONE_DAY;
        String holding = mode == TradeMode.INTRADAY
            ? "Target 1 before stop by NSE 15:30 session close; no overnight carry"
            : "Target 1 before stop within the next 5 daily session bars, inclusive of entry session; exit at fifth session close";
        try {
            List<Candle> bars = CompletedCandles.filter(provider.getHistoricalCandles(symbol, timeframe,
                mode == TradeMode.INTRADAY ? now.minusDays(55) : now.minusYears(5), now), now);
            if (bars.isEmpty()) throw new MarketDataException("No completed candles returned by provider");
            if (bars.stream().anyMatch(c -> c.getTimeframe() != timeframe)) {
                throw new MarketDataException("Provider returned an unexpected candle timeframe");
            }
            Candle last = bars.getLast();
            StockSignal signal = strategy.signal(symbol, bars, mode);
            boolean fresh = mode == TradeMode.INTRADAY
                ? last.getTimestamp().toLocalDate().equals(now.toLocalDate()) && !CompletedCandles.end(last).isBefore(now.minusMinutes(20))
                : !last.getTimestamp().toLocalDate().isBefore(now.toLocalDate().minusDays(4));
            String warning = "Research only; no broker/execution integration. ";
            if (!last.isLive()) warning += "Delayed or synthetic feed: entry disabled. ";
            if (!fresh) warning += "Last completed candle is stale for this mode. ";
            if (!MarketTimeUtil.isMarketOpen(now)) warning += "Outside regular NSE weekday session (holiday calendar not integrated). ";
            // This application has no verified live quote/holiday feed, so a completed-bar plan is never an executable entry.
            signal.setDataFreshness(last.getDataSource() + (fresh ? " / completed bars" : " / stale") + " / entry disabled");
            return new TradeAssessment(mode, mode == TradeMode.INTRADAY ? "15m" : "1d", holding, signal,
                last.getDataSource(), last.isLive(), last.getClose(),
                CompletedCandles.end(last).atZone(MarketTimeUtil.INDIA_ZONE).toOffsetDateTime().toString(),
                false, warning, evaluator.evaluate(bars, mode),
                List.copyOf(bars.subList(Math.max(0, bars.size() - 200), bars.size())));
        } catch (MarketDataException ex) {
            StockSignal signal = strategy.signal(symbol, List.of(), mode);
            signal.setSummary("Analysis unavailable: " + ex.getMessage());
            signal.setDataFreshness("UNAVAILABLE");
            return new TradeAssessment(mode, mode == TradeMode.INTRADAY ? "15m" : "1d", holding,
                signal, "UNAVAILABLE", false, null, null, false, ex.getMessage(),
                evaluator.unavailableEvidence(ex.getMessage()), List.of());
        }
    }

    public static String preferred(TradeEvidence intraday, TradeEvidence swing) {
        if (!intraday.status().equals("SUFFICIENT") || !swing.status().equals("SUFFICIENT")
            || intraday.netExpectancyR() == null || swing.netExpectancyR() == null
            || intraday.netExpectancyR().signum() <= 0 || swing.netExpectancyR().signum() <= 0) return "NO_CLEAR_WINNER";
        if (intraday.confidenceLow().compareTo(swing.confidenceHigh()) > 0) return "INTRADAY";
        if (swing.confidenceLow().compareTo(intraday.confidenceHigh()) > 0) return "SWING";
        return "NO_CLEAR_WINNER";
    }
}
