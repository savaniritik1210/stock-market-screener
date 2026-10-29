package com.stock.marketscreener.analysis;

import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class HistoricalEvaluator {
    private final BreakoutStrategy strategy;
    private final TradeAnalysisProperties config;
    public static final String METHOD = "Fixed long-only 20-bar breakout + SMA20>SMA50 + volume >= prior20 average; "
        + "ATR14 stop. First 30% (at least 60 bars) reserved for warmup; remaining chronological bars held out, "
        + "no fitting or tuning. Next-bar trigger only, expires after that bar; gap entries >0.25R rejected. "
        + "Stop first on ambiguous bars, adverse slippage on both sides and per-side fees; no overlapping trades, "
        + "at most one intraday trade per session. Success = target1 before stop within horizon AND positive net return after costs; "
        + "timeouts always count as non-success even when net profitable. "
        + "Intraday exits by the same session's 15:30 close with no overnight carry; swing includes the next five daily "
        + "session bars starting with the entry bar and exits at the fifth session close. "
        + "95% Wilson interval is descriptive only: serial dependence and multiple comparisons are not adjusted. "
        + "This is a chronological holdout-style simulation, not externally validated out-of-sample research. "
        + "Strategy provenance has not been independently audited; retrospective design informed by this dataset would "
        + "invalidate a genuine out-of-sample claim. A fixed untrained strategy is not thereby validated or calibrated.";

    public HistoricalEvaluator(BreakoutStrategy strategy, TradeAnalysisProperties config) {
        this.strategy = strategy;
        this.config = config;
    }

    public record Outcome(int exitIndex, boolean win, boolean timeout, BigDecimal netR) {}

    public TradeEvidence evaluate(List<Candle> candles, TradeMode mode) {
        if (candles.stream().anyMatch(c -> c.getDataSource() == null
            || c.getDataSource().equals("UNKNOWN") || c.getDataSource().contains("MOCK"))) {
            return unavailableEvidence("Synthetic or unverified-source candles cannot count as historical evidence");
        }
        int start = Math.max(BreakoutStrategy.WARMUP, (candles.size() * 3 + 9) / 10);
        List<Outcome> outcomes = new ArrayList<>();
        LocalDate lastEntryDay = null;
        for (int i = start; i < candles.size() - 1; i++) {
            if (mode == TradeMode.INTRADAY && candles.get(i).getTimestamp().toLocalDate().equals(lastEntryDay)) continue;
            BreakoutStrategy.Plan plan = strategy.plan(candles.subList(0, i + 1), mode);
            if (plan == null) continue;
            Outcome outcome = simulate(candles, i, plan, mode);
            if (outcome == null) continue;
            outcomes.add(outcome);
            lastEntryDay = candles.get(i + 1).getTimestamp().toLocalDate();
            i = outcome.exitIndex();
        }
        List<String> warnings = new ArrayList<>(List.of(
            "Historical target-before-stop and net-positive success frequency for this fixed strategy, not a calibrated probability for the current entry.",
            "Chronological holdout-style evaluation is not external validation. Strategy provenance is unaudited; if this dataset informed retrospective strategy design, the result is not genuinely out of sample.",
            "Non-overlap reduces dependence but does not establish statistical independence; regimes and symbols differ.",
            "Wilson intervals are descriptive only: serial dependence and selection across symbols, modes or parameters can invalidate nominal coverage. No multiple-testing adjustment is applied.",
            "Yahoo delayed OHLC is not an execution feed. Intrabar order, liquidity, corporate actions and actual costs may differ.",
            "Swing horizon uses five observed trading sessions; exchange holiday calendar is not integrated.",
            "One unit risk, unlevered long exposure only; gap losses are not clipped. Targets 2 and 3 are not backtested.",
            "Parameters must be fixed before inspection; changing them after viewing results invalidates the held-out interpretation.",
            "Costs per side: fee " + config.getFeeBps() + " bps; adverse slippage " + config.getSlippageBps() + " bps."));
        return summarize(outcomes, start < candles.size() ? stamp(candles.get(start)) : null,
            candles.isEmpty() ? null : stamp(candles.getLast()), warnings,
            (candles.isEmpty() ? "unknown symbol" : candles.getFirst().getSymbol()) + " / " + mode
                + " / RANGE_BREAKOUT / LONG");
    }

    public Outcome simulate(List<Candle> bars, int signalIndex, BreakoutStrategy.Plan plan, TradeMode mode) {
        int entryIndex = signalIndex + 1;
        if (entryIndex >= bars.size()) return null;
        Candle signal = bars.get(signalIndex);
        Candle first = bars.get(entryIndex);
        int last = entryIndex + 4;
        if (mode == TradeMode.INTRADAY) {
            if (!first.getTimestamp().equals(signal.getTimestamp().plusMinutes(15))) return null;
            if (!first.getTimestamp().toLocalDate().equals(signal.getTimestamp().toLocalDate())) return null;
            last = entryIndex;
            while (last + 1 < bars.size()
                && bars.get(last + 1).getTimestamp().toLocalDate().equals(first.getTimestamp().toLocalDate())) last++;
            if (!CompletedCandles.end(bars.get(last)).toLocalTime().equals(LocalTime.of(15, 30))) return null;
            for (int j = entryIndex + 1; j <= last; j++) {
                if (!bars.get(j).getTimestamp().equals(bars.get(j - 1).getTimestamp().plusMinutes(15))) return null;
            }
        } else {
            if (last >= bars.size()) return null;
            // A multi-week gap cannot stand in for five consecutive trading sessions.
            for (int j = entryIndex; j <= last; j++) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    bars.get(j - 1).getTimestamp().toLocalDate(), bars.get(j).getTimestamp().toLocalDate());
                if (days < 1 || days > 4) return null;
            }
        }
        if (first.getHigh().compareTo(plan.entry()) < 0) return null;
        BigDecimal rawFill = first.getOpen().max(plan.entry());
        if (rawFill.compareTo(plan.entry().add(plan.risk().multiply(new BigDecimal("0.25")))) > 0) return null;
        BigDecimal slip = config.getSlippageBps().movePointLeft(4);
        BigDecimal fill = rawFill.multiply(BigDecimal.ONE.add(slip));
        if (fill.compareTo(plan.target()) >= 0) return null;
        BigDecimal risk = fill.subtract(plan.stop());
        for (int j = entryIndex; j <= last; j++) {
            Candle c = bars.get(j);
            boolean stop = c.getLow().compareTo(plan.stop()) <= 0;
            boolean target = c.getHigh().compareTo(plan.target()) >= 0;
            if (stop) {
                // On entry bar a below-trigger open predates the fill: use stop, not that open.
                BigDecimal exit = j == entryIndex ? plan.stop() : c.getOpen().min(plan.stop());
                return outcome(j, false, false, fill, exit, risk, slip);
            }
            if (target) return outcome(j, true, false, fill, plan.target(), risk, slip);
        }
        return outcome(last, false, true, fill, bars.get(last).getClose(), risk, slip);
    }

    private Outcome outcome(int index, boolean win, boolean timeout, BigDecimal fill, BigDecimal rawExit,
                            BigDecimal risk, BigDecimal slip) {
        BigDecimal exit = rawExit.multiply(BigDecimal.ONE.subtract(slip));
        BigDecimal fees = fill.add(exit).multiply(config.getFeeBps().movePointLeft(4));
        BigDecimal net = exit.subtract(fill).subtract(fees).divide(risk, 8, RoundingMode.HALF_UP);
        return new Outcome(index, win && !timeout && net.signum() > 0, timeout, net);
    }

    public TradeEvidence summarize(List<Outcome> results, String from, String to, List<String> notes) {
        return summarize(results, from, to, notes, "one requested symbol / one mode / RANGE_BREAKOUT / LONG");
    }

    private TradeEvidence summarize(List<Outcome> results, String from, String to, List<String> notes, String grouping) {
        int n = results.size();
        int wins = (int) results.stream().filter(o -> o.win() && !o.timeout() && o.netR().signum() > 0).count();
        int timeouts = (int) results.stream().filter(Outcome::timeout).count();
        int losses = n - wins - timeouts;
        List<String> warnings = new ArrayList<>(notes);
        if (n < config.getMinSamples()) {
            warnings.add("Need at least " + config.getMinSamples() + " completed non-overlapping trades; found " + n + ".");
            return new TradeEvidence("INSUFFICIENT", null, null, null, n, wins, losses, timeouts,
                null, null, from, to, method(grouping), List.copyOf(warnings));
        }
        // Binary statistics use floating point; prices, fills and P&L remain BigDecimal.
        double p = (double) wins / n;
        double z = 1.959963984540054;
        double d = 1 + z * z / n;
        double center = (p + z * z / (2 * n)) / d;
        double half = z * Math.sqrt(p * (1 - p) / n + z * z / (4.0 * n * n)) / d;
        BigDecimal sum = results.stream().map(Outcome::netR).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal gains = results.stream().map(Outcome::netR).filter(x -> x.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal loss = results.stream().map(Outcome::netR).filter(x -> x.signum() < 0).reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        if (loss.signum() == 0) warnings.add("Profit factor undefined: no observed net losing returns.");
        return new TradeEvidence("SUFFICIENT", percent(p), percent(Math.max(0, center - half)),
            percent(Math.min(1, center + half)), n, wins, losses, timeouts,
            sum.divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP),
            loss.signum() == 0 ? null : gains.divide(loss, 4, RoundingMode.HALF_UP),
            from, to, method(grouping), List.copyOf(warnings));
    }

    private String method(String grouping) {
        return "Strategy version NSE_LONG_BREAKOUT_V1. Sample grouping: " + grouping
            + "; no pooling across symbols, modes, setups or directions. Reporting threshold: "
            + config.getMinSamples() + " completed non-overlapping trades; intraday maximum one entry per session. "
            + "Parameters: breakout=20 bars; SMA=20/50; volume>=prior20 mean; ATR=14-bar simple true-range mean; "
            + "stop=" + config.getAtrMultiple() + "*ATR; target1=" + config.getTargetR()
            + "R (rounded up to price tick 0.05); planned risk>=0.2% and <= "
            + config.getMaxIntradayRiskPct() + "% intraday / " + config.getMaxSwingRiskPct() + "% swing; "
            + "entry=signal high+0.05 rounded up; maximum opening gap=0.25R; one-bar trigger validity; "
            + "warmup=first 30% with minimum 60 bars. Costs: adverse slippage=" + config.getSlippageBps()
            + " basis points on EACH entry and exit; fees=" + config.getFeeBps()
            + " basis points on EACH executed entry and exit notional. Net R denominator=actual slipped fill minus stop. "
            + "Success percentage is historical frequency, NOT calibrated probability for the current entry. " + METHOD;
    }

    public static TradeEvidence unavailable(String reason) {
        return new TradeEvidence("UNAVAILABLE", null, null, null, 0, 0, 0, 0, null, null,
            null, null, METHOD, List.of(reason));
    }
    public TradeEvidence unavailableEvidence(String reason) {
        return new TradeEvidence("UNAVAILABLE", null, null, null, 0, 0, 0, 0, null, null,
            null, null, method("one requested symbol / one mode / RANGE_BREAKOUT / LONG"), List.of(reason));
    }
    private static BigDecimal percent(double fraction) {
        return BigDecimal.valueOf(fraction * 100).setScale(2, RoundingMode.HALF_UP);
    }
    private static String stamp(Candle c) {
        return c.getTimestamp().atZone(com.stock.marketscreener.util.MarketTimeUtil.INDIA_ZONE).toOffsetDateTime().toString();
    }
}
