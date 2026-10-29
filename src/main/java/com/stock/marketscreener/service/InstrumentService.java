package com.stock.marketscreener.service;

import com.stock.marketscreener.entity.InstrumentEntity;
import com.stock.marketscreener.exception.ResourceNotFoundException;
import com.stock.marketscreener.repository.InstrumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {
    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public List<InstrumentEntity> search(String q) {
        if (q == null || q.isBlank()) {
            return instrumentRepository.findByActiveTrueOrderBySymbolAsc();
        }
        return instrumentRepository.findBySymbolContainingIgnoreCase(q.trim());
    }

    public InstrumentEntity findBySymbol(String symbol) {
        return instrumentRepository.findByExchangeAndSymbol("NSE", symbol.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Instrument not found: " + symbol));
    }
}
