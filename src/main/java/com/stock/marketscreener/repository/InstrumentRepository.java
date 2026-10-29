package com.stock.marketscreener.repository;

import com.stock.marketscreener.entity.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<InstrumentEntity, Long> {
    Optional<InstrumentEntity> findByExchangeAndSymbol(String exchange, String symbol);
    Optional<InstrumentEntity> findByInstrumentKey(String instrumentKey);
    List<InstrumentEntity> findByActiveTrueOrderBySymbolAsc();
    List<InstrumentEntity> findBySymbolContainingIgnoreCase(String symbol);
}
