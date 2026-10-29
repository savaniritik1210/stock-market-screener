package com.stock.marketscreener.analysis;

import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.indicator.AtrCalculator;
import com.stock.marketscreener.indicator.SmaCalculator;
import com.stock.marketscreener.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BreakoutStrategy {
    public static final int WARMUP = 60;
    public static final BigDecimal TICK = new BigDecimal("0.05");
    private final TradeAnalysisProperties config;
    private final SmaCalculator sma = new SmaCalculator();
    private final AtrCalculator atr = new AtrCalculator();

    public BreakoutStrategy(TradeAnalysisProperties config) { this.config = config; }
    public record Plan(BigDecimal entry, BigDecimal stop, BigDecimal target, BigDecimal risk) {}

    public Plan plan(List<Candle> history, TradeMode mode) {
        if (history.size() < WARMUP) return null;
        Candle last = history.getLast();
        if (last.getVolume() <= 0) return null;
        List<Candle> previous = history.subList(history.size() - 21, history.size() - 1);
        BigDecimal high = previous.stream().map(Candle::getHigh).max(BigDecimal::compareTo).orElseThrow();
        BigDecimal avgVolume = previous.stream().map(c -> BigDecimal.valueOf(c.getVolume()))
            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal("20"), 8, RoundingMode.HALF_UP);
        if (last.getClose().compareTo(high) <= 0
            || sma.calculate(history, 20).compareTo(sma.calculate(history, 50)) <= 0
            || BigDecimal.valueOf(last.getVolume()).compareTo(avgVolume) < 0) return null;
        BigDecimal entry = tick(last.getHigh().add(TICK), RoundingMode.CEILING);
        BigDecimal risk = tick(atr.calculate(history, 14).multiply(config.getAtrMultiple()), RoundingMode.CEILING);
        BigDecimal pct = risk.multiply(new BigDecimal("100")).divide(entry, 8, RoundingMode.HALF_UP);
        BigDecimal max = mode == TradeMode.INTRADAY ? config.getMaxIntradayRiskPct() : config.getMaxSwingRiskPct();
        if (risk.signum() <= 0 || pct.compareTo(new BigDecimal("0.2")) < 0 || pct.compareTo(max) > 0) return null;
        BigDecimal stop = entry.subtract(risk);
        if (stop.signum() <= 0) return null;
        return new Plan(entry, stop, tick(entry.add(risk.multiply(config.getTargetR())), RoundingMode.CEILING), risk);
    }

    public StockSignal signal(String symbol, List<Candle> history, TradeMode mode) {
        StockSignal signal = new StockSignal();
        signal.setSymbol(symbol);
        signal.setInstrument(symbol);
        signal.setSignalType(SignalType.NO_SETUP);
        signal.setSetup(SetupType.NO_SETUP);
        signal.setDirection(TradeDirection.NONE);
        signal.setReasons(List.of());
        signal.setTimeframeAnalysis(List.of());
        signal.setConfidence(0);
        signal.setSummary("No qualifying fixed-rule breakout. Confidence is uncalibrated; see historical evidence.");
        Plan plan = plan(history, mode);
        if (plan == null) return signal;
        signal.setSignalType(SignalType.WATCH);
        signal.setSetup(SetupType.RANGE_BREAKOUT);
        signal.setDirection(TradeDirection.LONG);
        signal.setEntry(plan.entry());
        signal.setStopLoss(plan.stop());
        signal.setTarget1(plan.target());
        signal.setTarget2(tick(plan.entry().add(plan.risk().multiply(config.getTargetR().add(BigDecimal.ONE))), RoundingMode.CEILING));
        signal.setTarget3(tick(plan.entry().add(plan.risk().multiply(config.getTargetR().add(new BigDecimal("2")))), RoundingMode.CEILING));
        signal.setRiskReward(plan.target().subtract(plan.entry()).divide(plan.risk(), 4, RoundingMode.HALF_UP));
        signal.setScore(100);
        signal.setSummary("Research watchlist only: next-bar breakout trigger; score is rule completion, not success probability. Targets 2/3 are illustrative R levels, not evaluated.");
        signal.setReasons(List.of(
            new SignalReason("STRATEGY", "BREAKOUT_20", "Close above previous 20-bar high", true, 25),
            new SignalReason("TREND", "SMA_20_50", "20-bar SMA above 50-bar SMA", true, 25),
            new SignalReason("VOLUME", "VOLUME_20", "Volume at least previous 20-bar average", true, 25),
            new SignalReason("RISK", "ATR_14", "ATR-based stop within configured risk bounds", true, 25)));
        return signal;
    }

    private static BigDecimal tick(BigDecimal value, RoundingMode rounding) {
        return value.divide(TICK, 0, rounding).multiply(TICK);
    }
}
