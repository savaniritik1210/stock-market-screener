package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Timeframe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SmaCalculatorTest {
    @Test
    void calculatesSma() {
        List<Candle> candles = List.of(
            new Candle(1L, "RELIANCE", "NSE", Timeframe.ONE_MINUTE, LocalDateTime.now().minusMinutes(4), new BigDecimal("100"), new BigDecimal("101"), new BigDecimal("99"), new BigDecimal("100.5"), 1000L, 0L),
            new Candle(1L, "RELIANCE", "NSE", Timeframe.ONE_MINUTE, LocalDateTime.now().minusMinutes(3), new BigDecimal("100.5"), new BigDecimal("101.5"), new BigDecimal("100"), new BigDecimal("101"), 1100L, 0L),
            new Candle(1L, "RELIANCE", "NSE", Timeframe.ONE_MINUTE, LocalDateTime.now().minusMinutes(2), new BigDecimal("101"), new BigDecimal("102"), new BigDecimal("100.8"), new BigDecimal("101.7"), 1200L, 0L),
            new Candle(1L, "RELIANCE", "NSE", Timeframe.ONE_MINUTE, LocalDateTime.now().minusMinutes(1), new BigDecimal("101.7"), new BigDecimal("102.5"), new BigDecimal("101.4"), new BigDecimal("102.2"), 1300L, 0L),
            new Candle(1L, "RELIANCE", "NSE", Timeframe.ONE_MINUTE, LocalDateTime.now(), new BigDecimal("102.2"), new BigDecimal("103.0"), new BigDecimal("101.8"), new BigDecimal("102.8"), 1400L, 0L)
        );
        assertNotNull(new SmaCalculator().calculate(candles, 3));
    }
}
