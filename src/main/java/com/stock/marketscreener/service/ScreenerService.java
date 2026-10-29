package com.stock.marketscreener.service;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.model.Timeframe;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.stock.marketscreener.exception.InvalidAnalysisRequestException;
import com.stock.marketscreener.util.SymbolValidator;
import com.stock.marketscreener.util.MarketTimeUtil;

@Service
public class ScreenerService {
    private final SignalService signalService;
    private final MarketDataService marketDataService;

    public ScreenerService(SignalService signalService, MarketDataService marketDataService) {
        this.signalService = signalService;
        this.marketDataService = marketDataService;
    }

    public List<StockSignal> scan(List<String> symbols) {
        if (symbols == null || symbols.isEmpty() || symbols.size() > 20) {
            throw new InvalidAnalysisRequestException("Scan requires 1–20 symbols");
        }
        List<String> normalized = symbols.stream().map(SymbolValidator::normalize).distinct().toList();
        List<StockSignal> signals = new ArrayList<>();
        for (String symbol : normalized) {
            List<Candle> candles = marketDataService.getHistoricalCandles(
                symbol, Timeframe.ONE_DAY, LocalDateTime.now(MarketTimeUtil.INDIA_ZONE).minusDays(180), LocalDateTime.now(MarketTimeUtil.INDIA_ZONE));
            signals.add(signalService.analyze(symbol, candles));
        }
        return signals;
    }
}
