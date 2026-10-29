package com.stock.marketscreener.signal;

import com.stock.marketscreener.analysis.BreakoutStrategy;
import com.stock.marketscreener.analysis.CompletedCandles;
import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.model.*;
import com.stock.marketscreener.util.MarketTimeUtil;
import com.stock.marketscreener.util.SymbolValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SignalEngine {
    private final BreakoutStrategy strategy;

    public SignalEngine() { this(new BreakoutStrategy(new TradeAnalysisProperties())); }
    @org.springframework.beans.factory.annotation.Autowired
    public SignalEngine(BreakoutStrategy strategy) { this.strategy = strategy; }

    public StockSignal evaluate(String symbol, List<Candle> candles) {
        String normalized = SymbolValidator.normalize(symbol);
        List<Candle> completed = CompletedCandles.filter(candles == null ? List.of() : candles,
            LocalDateTime.now(MarketTimeUtil.INDIA_ZONE));
        TradeMode mode = !completed.isEmpty() && completed.getLast().getTimeframe() == Timeframe.ONE_DAY
            ? TradeMode.SWING : TradeMode.INTRADAY;
        StockSignal signal = strategy.signal(normalized, completed, mode);
        signal.setDataFreshness(completed.isEmpty() ? "UNAVAILABLE" :
            completed.getLast().getDataSource() + " / completed through " + CompletedCandles.end(completed.getLast())
                + "+05:30 / research only; entry disabled");
        return signal;
    }
}
