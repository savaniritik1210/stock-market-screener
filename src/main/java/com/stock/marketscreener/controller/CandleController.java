package com.stock.marketscreener.controller;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Timeframe;
import com.stock.marketscreener.service.MarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CandleController {
    private final MarketDataService marketDataService;

    public CandleController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/candles/{symbol}")
    public List<Candle> getCandles(@PathVariable String symbol) {
        return marketDataService.getHistoricalCandles(symbol, Timeframe.FIVE_MINUTE, LocalDateTime.now().minusDays(7), LocalDateTime.now());
    }
}
