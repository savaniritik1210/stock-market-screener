package com.stock.marketscreener.service;

import com.stock.marketscreener.entity.CandleEntity;
import com.stock.marketscreener.repository.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleService {
    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public List<CandleEntity> getCandles(Long instrumentId) {
        return candleRepository.findByInstrumentIdOrderByCandleTimeDesc(instrumentId);
    }
}
