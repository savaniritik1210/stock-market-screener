package com.stock.marketscreener.analysis;

import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.model.*;
import com.stock.marketscreener.service.TradeComparisonService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TradeAnalysisTest {
    private final TradeAnalysisProperties config = new TradeAnalysisProperties();
    private final BreakoutStrategy strategy = new BreakoutStrategy(config);
    private final HistoricalEvaluator evaluator = new HistoricalEvaluator(strategy, config);
    private final BreakoutStrategy.Plan plan = new BreakoutStrategy.Plan(bd("100"), bd("98"), bd("103"), bd("2"));

    static BigDecimal bd(String text) { return new BigDecimal(text); }
    static Candle candle(LocalDateTime time, Timeframe timeframe, String open, String high, String low, String close) {
        return new Candle(null, "TEST", "NSE", timeframe, time, bd(open), bd(high), bd(low), bd(close),
            1000L, 0L, "YAHOO_FINANCE_DELAYED", false);
    }
    static List<Candle> daily(int count) {
        List<Candle> bars = new ArrayList<>();
        LocalDate day = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < count; i++) {
            while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) day = day.plusDays(1);
            BigDecimal close = bd("100").add(bd("0.5").multiply(BigDecimal.valueOf(i)));
            bars.add(candle(day.atTime(9, 15), Timeframe.ONE_DAY, close.subtract(bd("0.2")).toPlainString(),
                close.add(bd("0.1")).toPlainString(), close.subtract(bd("0.3")).toPlainString(), close.toPlainString()));
            day = day.plusDays(1);
        }
        return bars;
    }
    private List<Candle> intraday() {
        List<Candle> bars = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(2025, 1, 6, 9, 15);
        for (int i = 0; i < 25; i++) bars.add(candle(start.plusMinutes(15L * i), Timeframe.FIFTEEN_MINUTE, "99.5", "101", "99", "100"));
        return bars;
    }
    private List<HistoricalEvaluator.Outcome> outcomes(int n, int wins, String net) {
        return IntStream.range(0, n).mapToObj(i -> new HistoricalEvaluator.Outcome(i, i < wins, false, bd(net))).toList();
    }

    @Test void strategyUsesDecimalAtrAndNoArbitraryPlan() {
        List<Candle> bars = daily(70);
        BreakoutStrategy.Plan actual = strategy.plan(bars, TradeMode.SWING);
        assertNotNull(actual);
        assertTrue(actual.entry().compareTo(bars.getLast().getHigh()) > 0);
        assertEquals(0, actual.entry().subtract(actual.stop()).compareTo(actual.risk()));
        StockSignal signal = strategy.signal("TEST", bars, TradeMode.SWING);
        assertEquals(SignalType.WATCH, signal.getSignalType());
        assertEquals(0, signal.getConfidence());
        assertEquals(100, signal.getScore());
        assertTrue(signal.getTarget1().compareTo(signal.getEntry()) > 0);
    }
    @Test void insufficientWarmupHasNoPricePlan() {
        StockSignal signal = strategy.signal("TEST", daily(59), TradeMode.SWING);
        assertEquals(SignalType.NO_SETUP, signal.getSignalType());
        assertNull(signal.getEntry());
        assertNull(signal.getTarget1());
    }
    @Test void lowVolumeRejectsSetup() {
        List<Candle> bars = daily(70);
        bars.getLast().setVolume(1L);
        assertNull(strategy.plan(bars, TradeMode.SWING));
    }
    @Test void flatPricesRejectSetup() {
        List<Candle> bars = daily(70);
        bars.forEach(c -> { c.setClose(bd("100")); c.setHigh(bd("101")); c.setLow(bd("99")); c.setOpen(bd("100")); });
        assertNull(strategy.plan(bars, TradeMode.SWING));
    }
    @Test void riskBoundsRejectWideAtr() {
        List<Candle> bars = daily(70);
        bars.getLast().setLow(bd("1"));
        assertNull(strategy.plan(bars, TradeMode.INTRADAY));
    }
    @Test void prefixPlanDoesNotDependOnFutureBars() {
        List<Candle> bars = daily(100);
        BreakoutStrategy.Plan before = strategy.plan(bars.subList(0, 70), TradeMode.SWING);
        bars.get(80).setClose(bd("9000000"));
        assertEquals(before, strategy.plan(bars.subList(0, 70), TradeMode.SWING));
    }
    @Test void noSameBarEntry() {
        List<Candle> bars = intraday();
        bars.get(0).setHigh(bd("105"));
        bars.get(1).setHigh(bd("99.9"));
        assertNull(evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY));
    }
    @Test void ambiguousEntryBarStopsFirst() {
        List<Candle> bars = intraday();
        bars.get(1).setHigh(bd("104"));
        bars.get(1).setLow(bd("97"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertFalse(result.win());
        assertFalse(result.timeout());
        assertEquals(1, result.exitIndex());
        assertTrue(result.netR().compareTo(bd("-1")) < 0);
    }
    @Test void targetBeforeStopWinsAfterCosts() {
        List<Candle> bars = intraday();
        bars.get(2).setHigh(bd("104"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertTrue(result.win());
        assertTrue(result.netR().compareTo(bd("1.5")) < 0);
        assertTrue(result.netR().signum() > 0);
    }
    @Test void gapsFillAtOpenAndReduceReward() {
        List<Candle> bars = intraday();
        bars.get(2).setHigh(bd("104"));
        var normal = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        bars.get(1).setOpen(bd("100.4"));
        var gap = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertTrue(gap.netR().compareTo(normal.netR()) < 0);
        bars.get(1).setOpen(bd("101"));
        assertNull(evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY));
    }
    @Test void gapThroughStopUsesWorseOpenWithoutClipping() {
        List<Candle> bars = intraday();
        bars.get(2).setOpen(bd("90"));
        bars.get(2).setLow(bd("89"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertTrue(result.netR().compareTo(bd("-4")) < 0);
    }
    @Test void intradayTimeoutAtSessionClose() {
        var result = evaluator.simulate(intraday(), 0, plan, TradeMode.INTRADAY);
        assertTrue(result.timeout());
        assertFalse(result.win());
        assertEquals(24, result.exitIndex());
    }
    @Test void incompleteOrMissingSessionBarsExcluded() {
        List<Candle> bars = intraday();
        assertNull(evaluator.simulate(bars.subList(0, 24), 0, plan, TradeMode.INTRADAY));
        bars.remove(4);
        assertNull(evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY));
    }
    @Test void swingExpiresAfterFiveTradingBarsNotCalendarDays() {
        List<Candle> bars = daily(8);
        bars.forEach(c -> { c.setOpen(bd("100")); c.setHigh(bd("101")); c.setLow(bd("99")); c.setClose(bd("100")); });
        bars.get(6).setHigh(bd("110"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.SWING);
        assertTrue(result.timeout());
        assertEquals(5, result.exitIndex());
        assertNull(evaluator.simulate(bars.subList(0, 5), 0, plan, TradeMode.SWING));
    }
    @Test void incompleteBarsAndDelayedCurrentBarsExcluded() {
        List<Candle> bars = intraday();
        LocalDateTime now = bars.get(2).getTimestamp();
        assertEquals(0, CompletedCandles.filter(bars, now).size());
        bars.get(0).setLive(true);
        assertEquals(1, CompletedCandles.filter(bars, now).size());
    }
    @Test void malformedAndNonchronologicalBarsFailClosed() {
        List<Candle> bars = intraday();
        bars.get(0).setVolume(null);
        assertThrows(MarketDataException.class, () -> CompletedCandles.filter(bars, LocalDateTime.of(2026, 1, 1, 0, 0)));
        bars.get(0).setVolume(100L);
        bars.get(1).setTimestamp(bars.get(0).getTimestamp());
        assertThrows(MarketDataException.class, () -> CompletedCandles.filter(bars, LocalDateTime.of(2026, 1, 1, 0, 0)));
    }
    @Test void samplesBelowThirtyNeverPublishEstimate() {
        config.setMinSamples(1);
        TradeEvidence evidence = evaluator.summarize(outcomes(29, 29, "1"), null, null, List.of());
        assertEquals("INSUFFICIENT", evidence.status());
        assertNull(evidence.successRate());
        assertNull(evidence.netExpectancyR());
    }
    @Test void wilsonIntervalAndTimeoutDenominatorAreCorrect() {
        List<HistoricalEvaluator.Outcome> data = new ArrayList<>(outcomes(30, 15, "1"));
        data.set(29, new HistoricalEvaluator.Outcome(29, false, true, bd("-0.5")));
        TradeEvidence evidence = evaluator.summarize(data, null, null, List.of());
        assertEquals(bd("50.00"), evidence.successRate());
        assertEquals(bd("33.15"), evidence.confidenceLow());
        assertEquals(bd("66.85"), evidence.confidenceHigh());
        assertEquals(1, evidence.timeouts());
        assertEquals(14, evidence.losses());
        assertEquals(bd("0.9500"), evidence.netExpectancyR());
        assertEquals(bd("58.0000"), evidence.profitFactor());
    }
    @Test void noLossesDoesNotReportInfiniteProfitFactor() {
        TradeEvidence evidence = evaluator.summarize(outcomes(30, 30, "1"), null, null, List.of());
        assertNull(evidence.profitFactor());
        assertTrue(evidence.confidenceLow().compareTo(bd("100")) < 0);
    }
    @Test void historicalLoopReservesWarmupAndExcludesMock() {
        List<Candle> bars = daily(600);
        TradeEvidence result = evaluator.evaluate(bars, TradeMode.SWING);
        assertTrue(result.sampleSize() >= 30);
        assertTrue(result.sampleSize() < 420);
        assertTrue(result.from().startsWith(bars.get(180).getTimestamp().toLocalDate().toString()));
        bars.get(0).setDataSource("MOCK");
        assertEquals("UNAVAILABLE", evaluator.evaluate(bars, TradeMode.SWING).status());
    }
    @Test void modeComparisonNeedsSeparatedIntervalsAndPositiveExpectancy() {
        TradeEvidence good = evaluator.summarize(outcomes(100, 90, "1"), null, null, List.of());
        TradeEvidence poorer = evaluator.summarize(outcomes(100, 40, "0.1"), null, null, List.of());
        TradeEvidence losing = evaluator.summarize(outcomes(100, 40, "-1"), null, null, List.of());
        assertEquals("INTRADAY", TradeComparisonService.preferred(good, poorer));
        assertEquals("SWING", TradeComparisonService.preferred(poorer, good));
        assertEquals("NO_CLEAR_WINNER", TradeComparisonService.preferred(good, good));
        assertEquals("NO_CLEAR_WINNER", TradeComparisonService.preferred(good, losing));
        assertEquals("NO_CLEAR_WINNER", TradeComparisonService.preferred(good, HistoricalEvaluator.unavailable("test")));
    }
    @Test void historicalTradesNeverOverlapAndOnlyOneEntryPerIntradaySession() {
        List<Candle> bars = new ArrayList<>();
        LocalDate day = LocalDate.of(2025, 1, 6);
        for (int session = 0; session < 50; session++) {
            while (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) day = day.plusDays(1);
            for (int i = 0; i < 25; i++) {
                BigDecimal close = bd("100").add(bd("0.2").multiply(BigDecimal.valueOf(bars.size())));
                bars.add(candle(day.atTime(9, 15).plusMinutes(15L * i), Timeframe.FIFTEEN_MINUTE,
                    close.subtract(bd("0.1")).toPlainString(), close.add(bd("0.05")).toPlainString(),
                    close.subtract(bd("0.15")).toPlainString(), close.toPlainString()));
            }
            day = day.plusDays(1);
        }
        TradeEvidence result = evaluator.evaluate(bars, TradeMode.INTRADAY);
        assertTrue(result.sampleSize() > 0);
        assertTrue(result.sampleSize() <= 35, "Held-out sessions bound independent entry count");
        assertEquals(result.sampleSize(), result.wins() + result.losses() + result.timeouts());
        assertEquals("INSUFFICIENT", result.status(), "Sparse risk-qualified sessions cannot manufacture 30 samples");
    }
    @Test void stopCanBeHitOnFifthSwingSessionButSixthIsOutsideHorizon() {
        List<Candle> bars = daily(8);
        bars.forEach(c -> { c.setOpen(bd("100")); c.setHigh(bd("101")); c.setLow(bd("99")); c.setClose(bd("100")); });
        bars.get(5).setLow(bd("97"));
        var stopped = evaluator.simulate(bars, 0, plan, TradeMode.SWING);
        assertFalse(stopped.timeout());
        assertEquals(5, stopped.exitIndex());
        bars.get(5).setLow(bd("99"));
        bars.get(6).setLow(bd("97"));
        assertTrue(evaluator.simulate(bars, 0, plan, TradeMode.SWING).timeout());
    }
    @Test void targetHitWithNegativeAfterCostReturnIsNotAWin() {
        config.setFeeBps(bd("100"));
        config.setSlippageBps(bd("100"));
        List<Candle> bars = intraday();
        bars.get(2).setHigh(bd("104"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertFalse(result.win());
        assertFalse(result.timeout());
        assertTrue(result.netR().signum() < 0);
        var evidence = evaluator.summarize(java.util.Collections.nCopies(30, result), null, null, List.of());
        assertEquals(bd("0.00"), evidence.successRate());
        assertEquals(30, evidence.losses());
    }
    @Test void profitableTimeoutStillCountsAsUnsuccessful() {
        List<Candle> bars = intraday();
        bars.getLast().setHigh(bd("102.5"));
        bars.getLast().setClose(bd("102"));
        var result = evaluator.simulate(bars, 0, plan, TradeMode.INTRADAY);
        assertTrue(result.timeout());
        assertFalse(result.win());
        assertTrue(result.netR().signum() > 0);
        var evidence = evaluator.summarize(java.util.Collections.nCopies(30, result), null, null, List.of());
        assertEquals(bd("0.00"), evidence.successRate());
        assertEquals(30, evidence.timeouts());
    }
    @Test void evidenceExposesVersionConfiguredCostsGroupingAndProvenance() {
        config.setFeeBps(bd("15"));
        config.setSlippageBps(bd("7"));
        config.setMinSamples(45);
        TradeEvidence evidence = evaluator.evaluate(daily(600), TradeMode.SWING);
        assertTrue(evidence.method().contains("NSE_LONG_BREAKOUT_V1"));
        assertTrue(evidence.method().contains("TEST / SWING / RANGE_BREAKOUT / LONG"));
        assertTrue(evidence.method().contains("threshold: 45"));
        assertTrue(evidence.method().contains("slippage=7 basis points on EACH"));
        assertTrue(evidence.method().contains("fees=15 basis points on EACH"));
        assertTrue(evidence.method().contains("not externally validated"));
        assertTrue(evidence.method().contains("NOT calibrated probability for the current entry"));
    }
}
