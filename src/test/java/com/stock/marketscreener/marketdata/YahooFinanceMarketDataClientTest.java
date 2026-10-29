package com.stock.marketscreener.marketdata;

import com.stock.marketscreener.exception.InvalidAnalysisRequestException;
import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.model.Timeframe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class YahooFinanceMarketDataClientTest {
    private final YahooFinanceMarketDataClient client = new YahooFinanceMarketDataClient();
    private String json(String high, String volume) {
        return """
            {"chart":{"error":null,"result":[{"timestamp":[1736135100],"indicators":{"quote":[{
            "open":[100.1234567890123456789],"high":[%s],"low":[99],"close":[101],"volume":[%s]}]}}]}}
            """.formatted(high, volume);
    }
    @Test void pricesParsedAsExactDecimalsAndSourceIsDelayed() throws Exception {
        var bars = client.parseChartResponse(json("102", "1000"), "TEST", Timeframe.FIFTEEN_MINUTE);
        assertEquals(new BigDecimal("100.1234567890123456789"), bars.getFirst().getOpen());
        assertEquals("YAHOO_FINANCE_DELAYED", bars.getFirst().getDataSource());
        assertFalse(bars.getFirst().isLive());
    }
    @Test void missingHighOrVolumeFailsRatherThanFabricatingZero() {
        assertThrows(MarketDataException.class, () -> client.parseChartResponse(json("null", "1000"), "TEST", Timeframe.ONE_DAY));
        assertThrows(MarketDataException.class, () -> client.parseChartResponse(json("102", "null"), "TEST", Timeframe.ONE_DAY));
        assertThrows(MarketDataException.class, () -> client.parseChartResponse(json("50", "1000"), "TEST", Timeframe.ONE_DAY));
    }
    @Test void providerErrorIsSurfaced() {
        assertThrows(MarketDataException.class, () -> client.parseChartResponse(
            "{\"chart\":{\"result\":null,\"error\":{\"code\":\"Not Found\"}}}", "TEST", Timeframe.ONE_DAY));
    }
    @Test void badSymbolsAndUnboundedIntradayRequestsFailBeforeNetwork() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        assertThrows(InvalidAnalysisRequestException.class, () -> client.getHistoricalCandles("../BAD", Timeframe.ONE_DAY, now.minusDays(1), now));
        assertThrows(InvalidAnalysisRequestException.class, () -> client.getHistoricalCandles("TEST", Timeframe.FIFTEEN_MINUTE, now.minusDays(60), now));
    }
    @Test void exhaustedCallBudgetFailsWithoutNetwork() {
        YahooFinanceMarketDataClient limited = new YahooFinanceMarketDataClient(0);
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);
        MarketDataException error = assertThrows(MarketDataException.class,
            () -> limited.getHistoricalCandles("TEST", Timeframe.ONE_DAY, now.minusDays(1), now));
        assertTrue(error.getMessage().contains("budget"));
        assertFalse(limited.isConnected());
    }
    @Test void invalidTimestampAndEmptyDocumentFailClosed() {
        assertThrows(MarketDataException.class, () -> client.parseChartResponse(
            json("102", "1000").replace("1736135100", "9223372036854775807"), "TEST", Timeframe.ONE_DAY));
        assertThrows(MarketDataException.class, () -> client.parseChartResponse("", "TEST", Timeframe.ONE_DAY));
    }
}
