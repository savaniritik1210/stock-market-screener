package com.stock.marketscreener.controller;

import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.service.TradeComparisonService;
import com.stock.marketscreener.model.TradeComparison;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    private final TradeComparisonService comparisonService;

    public AnalysisController(TradeComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/analysis/{symbol}")
    public StockSignal analyze(@PathVariable String symbol) {
        return comparisonService.compare(symbol).intraday().signal();
    }

    @GetMapping("/analysis/{symbol}/comparison")
    public TradeComparison compare(@PathVariable String symbol) {
        return comparisonService.compare(symbol);
    }
}
