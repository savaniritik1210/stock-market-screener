package com.stock.marketscreener.controller;

import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.service.ScreenerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ScreenerController {
    private static final List<String> DEFAULT_UNIVERSE =
        List.of("RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK", "SBIN", "ITC", "LT");

    private final ScreenerService screenerService;

    public ScreenerController(ScreenerService screenerService) {
        this.screenerService = screenerService;
    }

    @PostMapping("/screener/scan")
    @SuppressWarnings("unchecked")
    public List<StockSignal> scan(@RequestBody(required = false) Map<String, Object> request) {
        List<String> symbols = DEFAULT_UNIVERSE;
        if (request != null && request.get("symbols") instanceof List<?> requested && !requested.isEmpty()) {
            symbols = (List<String>) requested;
        }
        return screenerService.scan(symbols);
    }
}
