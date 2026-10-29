package com.stock.marketscreener.repository;

import com.stock.marketscreener.entity.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CandleRepository extends JpaRepository<CandleEntity, Long> {
    Optional<CandleEntity> findByInstrumentIdAndTimeframeAndCandleTime(Long instrumentId, String timeframe, LocalDateTime candleTime);
    List<CandleEntity> findByInstrumentIdAndTimeframeOrderByCandleTimeAsc(Long instrumentId, String timeframe);
    List<CandleEntity> findByInstrumentIdOrderByCandleTimeDesc(Long instrumentId);
}
