package com.stock.marketscreener.controller;

import com.stock.marketscreener.entity.InstrumentEntity;
import com.stock.marketscreener.service.InstrumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final InstrumentService instrumentService;

    public StockController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping("/search")
    public List<InstrumentEntity> search(@RequestParam String q) {
        return instrumentService.search(q);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<InstrumentEntity> getBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(instrumentService.findBySymbol(symbol));
    }
}
