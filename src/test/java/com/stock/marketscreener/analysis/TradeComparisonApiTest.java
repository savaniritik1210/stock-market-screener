package com.stock.marketscreener.analysis;

import com.stock.marketscreener.config.TradeAnalysisProperties;
import com.stock.marketscreener.controller.AnalysisController;
import com.stock.marketscreener.exception.GlobalExceptionHandler;
import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.marketdata.MarketDataClient;
import com.stock.marketscreener.model.*;
import com.stock.marketscreener.service.TradeComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TradeComparisonApiTest {
    private MarketDataClient provider;
    private TradeComparisonService service;
    private MockMvc mvc;
    @BeforeEach void setup() {
        provider = mock(MarketDataClient.class);
        TradeAnalysisProperties config = new TradeAnalysisProperties();
        BreakoutStrategy strategy = new BreakoutStrategy(config);
        service = new TradeComparisonService(provider, strategy, new HistoricalEvaluator(strategy, config), config,
            Clock.fixed(Instant.parse("2026-09-30T08:00:00Z"), ZoneId.of("Asia/Kolkata")));
        mvc = MockMvcBuilders.standaloneSetup(new AnalysisController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        when(provider.getHistoricalCandles(anyString(), any(), any(), any())).thenReturn(List.of());
    }
    @Test void endpointValidatesSymbolBeforeProviderCalls() throws Exception {
        mvc.perform(get("/api/analysis/INVALID!/comparison")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verifyNoInteractions(provider);
    }
    @Test void contractKeepsNullableEvidenceAndIndiaTimestamp() throws Exception {
        mvc.perform(get("/api/analysis/RELIANCE/comparison")).andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("RELIANCE"))
            .andExpect(jsonPath("$.generatedAt").value("2026-09-30T13:30+05:30"))
            .andExpect(jsonPath("$.preferredMode").value("NO_CLEAR_WINNER"))
            .andExpect(jsonPath("$.intraday.mode").value("INTRADAY"))
            .andExpect(jsonPath("$.swing.mode").value("SWING"))
            .andExpect(jsonPath("$.intraday.isLive").value(false))
            .andExpect(jsonPath("$.intraday.entryEligible").value(false))
            .andExpect(jsonPath("$.intraday.candles").isArray())
            .andExpect(jsonPath("$.intraday.candles").isEmpty())
            .andExpect(jsonPath("$.intraday.evidence.status").value("UNAVAILABLE"))
            .andExpect(jsonPath("$.intraday.evidence.successRate").isEmpty());
    }
    @Test void oldAnalysisEndpointRemainsSignalShaped() throws Exception {
        mvc.perform(get("/api/analysis/RELIANCE")).andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("RELIANCE"))
            .andExpect(jsonPath("$.signalType").value("NO_SETUP"))
            .andExpect(jsonPath("$.entry").isEmpty());
    }
    @Test void fetchesTwoBoundedRangesOnceAndCachesNormalizedSymbol() {
        TradeComparison first = service.compare("reliance.ns");
        assertSame(first, service.compare("RELIANCE"));
        verify(provider).getHistoricalCandles(eq("RELIANCE"), eq(Timeframe.FIFTEEN_MINUTE),
            eq(LocalDateTime.of(2026, 8, 6, 13, 30)), eq(LocalDateTime.of(2026, 9, 30, 13, 30)));
        verify(provider).getHistoricalCandles(eq("RELIANCE"), eq(Timeframe.ONE_DAY),
            eq(LocalDateTime.of(2021, 9, 30, 13, 30)), eq(LocalDateTime.of(2026, 9, 30, 13, 30)));
        verifyNoMoreInteractions(provider);
    }
    @Test void perModeProviderErrorsDoNotSuppressOtherMode() {
        when(provider.getHistoricalCandles(anyString(), eq(Timeframe.FIFTEEN_MINUTE), any(), any()))
            .thenThrow(new MarketDataException("Provider rate limited"));
        when(provider.getHistoricalCandles(anyString(), eq(Timeframe.ONE_DAY), any(), any()))
            .thenReturn(TradeAnalysisTest.daily(70));
        TradeComparison result = service.compare("RELIANCE");
        assertEquals("UNAVAILABLE", result.intraday().evidence().status());
        assertEquals("INSUFFICIENT", result.swing().evidence().status());
        assertNotNull(result.swing().currentPrice());
        assertFalse(result.swing().entryEligible());
        assertTrue(result.swing().dataWarning().contains("stale"));
    }
    @Test void boundedCacheEvictsOldestSymbol() {
        TradeAnalysisProperties config = new TradeAnalysisProperties();
        config.setCacheSize(1);
        BreakoutStrategy strategy = new BreakoutStrategy(config);
        var limited = new TradeComparisonService(provider, strategy, new HistoricalEvaluator(strategy, config), config);
        limited.compare("A"); limited.compare("B"); limited.compare("A");
        verify(provider, times(4)).getHistoricalCandles(eq("A"), any(), any(), any());
    }
    @Test void chartReturnsAtMostTwoHundredCompletedModeBarsWithoutExtraFetch() {
        List<Candle> daily = TradeAnalysisTest.daily(250);
        when(provider.getHistoricalCandles(anyString(), eq(Timeframe.ONE_DAY), any(), any())).thenReturn(daily);
        TradeComparison result = service.compare("TEST");
        assertEquals(200, result.swing().candles().size());
        assertSame(daily.get(50), result.swing().candles().getFirst());
        assertSame(daily.getLast(), result.swing().candles().getLast());
        assertTrue(result.swing().candles().stream().allMatch(c -> c.getTimeframe() == Timeframe.ONE_DAY));
        assertTrue(result.swing().holdingPeriod().contains("fifth session close"));
        assertTrue(result.intraday().holdingPeriod().contains("no overnight carry"));
        assertEquals(SignalType.WATCH, result.swing().signal().getSignalType());
        assertFalse(result.swing().entryEligible());
        verify(provider, times(2)).getHistoricalCandles(anyString(), any(), any(), any());
        assertTrue(result.swing().evidence().method().contains("not thereby validated or calibrated"));
    }
    @Test void serializedAssessmentAndEvidenceTimesIncludeIndiaOffset() throws Exception {
        when(provider.getHistoricalCandles(anyString(), eq(Timeframe.ONE_DAY), any(), any()))
            .thenReturn(TradeAnalysisTest.daily(250));
        mvc.perform(get("/api/analysis/TEST/comparison"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generatedAt").value(org.hamcrest.Matchers.endsWith("+05:30")))
            .andExpect(jsonPath("$.swing.asOf").value(org.hamcrest.Matchers.endsWith("+05:30")))
            .andExpect(jsonPath("$.swing.evidence.from").value(org.hamcrest.Matchers.endsWith("+05:30")))
            .andExpect(jsonPath("$.swing.evidence.to").value(org.hamcrest.Matchers.endsWith("+05:30")));
    }
}
