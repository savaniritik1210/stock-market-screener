package com.stock.marketscreener.repository;

import com.stock.marketscreener.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, Long> {
    Optional<StockEntity> findBySymbolAndExchange(String symbol, String exchange);
    List<StockEntity> findByActiveTrue();
    List<StockEntity> findBySymbolContainingIgnoreCase(String symbol);
}
