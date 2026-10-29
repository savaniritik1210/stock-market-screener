package com.stock.marketscreener.controller;

import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.service.SignalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @GetMapping("/signals/{symbol}/latest")
    public StockSignal latest(@PathVariable String symbol) {
        return signalService.analyze(symbol, List.of());
    }
}
