package com.stock.marketscreener.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.Tick;
import com.stock.marketscreener.model.Timeframe;
import com.stock.marketscreener.exception.MarketDataException;
import com.stock.marketscreener.util.SymbolValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Real market-data provider backed by the public Yahoo Finance chart API.
 * Data is REAL (sourced from an actual exchange feed relayed by Yahoo) but is
 * typically delayed ~15-20 minutes for NSE listed instruments and therefore
 * MUST NOT be treated as tick-accurate real-time / trading-grade data.
 *
 * dataSource = "YAHOO_FINANCE_DELAYED", live = false (delayed, not tick real-time).
 */
public class YahooFinanceMarketDataClient implements MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceMarketDataClient.class);
    private static final String DATA_SOURCE = "YAHOO_FINANCE_DELAYED";
    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private final int callsPerMinute;
    private final java.util.ArrayDeque<Instant> calls = new java.util.ArrayDeque<>();

    public YahooFinanceMarketDataClient() { this(30); }
    public YahooFinanceMarketDataClient(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }

    private synchronized void reserveCall() {
        Instant now = Instant.now();
        while (!calls.isEmpty() && calls.peekFirst().isBefore(now.minusSeconds(60))) calls.removeFirst();
        if (calls.size() >= callsPerMinute) throw new MarketDataException("Market-data call budget exhausted; retry after one minute");
        calls.addLast(now);
    }

    private volatile boolean lastCallSucceeded = false;

    @Override
    public List<Candle> getHistoricalCandles(String instrumentKey, Timeframe timeframe, LocalDateTime from, LocalDateTime to) {
        lastCallSucceeded = false;
        String yahooSymbol = toYahooSymbol(instrumentKey);
        if (from == null || to == null || !from.isBefore(to)) {
            throw new com.stock.marketscreener.exception.InvalidAnalysisRequestException("Invalid candle time range");
        }
        if (timeframe != Timeframe.ONE_DAY && timeframe != Timeframe.ONE_WEEK
            && Duration.between(from, to).toDays() > (timeframe == Timeframe.ONE_MINUTE ? 7 : 59)) {
            throw new com.stock.marketscreener.exception.InvalidAnalysisRequestException("Intraday range exceeds provider retention");
        }
        reserveCall();
        String interval = toYahooInterval(timeframe);
        long period1 = from.atZone(INDIA_ZONE).toEpochSecond();
        long period2 = to.atZone(INDIA_ZONE).toEpochSecond();

        String url = String.format(
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=%s&period1=%d&period2=%d",
            URLEncoder.encode(yahooSymbol, StandardCharsets.UTF_8), interval, period1, period2
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (compatible; StockScreener/1.0)")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                lastCallSucceeded = false;
                log.warn("Yahoo Finance returned status {} for {}", response.statusCode(), yahooSymbol);
                throw new MarketDataException("Yahoo Finance returned HTTP " + response.statusCode() + " for " + yahooSymbol);
            }

            List<Candle> candles = parseChartResponse(response.body(), instrumentKey, timeframe);
            if (candles.isEmpty()) throw new MarketDataException("Yahoo Finance returned no usable candles for " + yahooSymbol);
            lastCallSucceeded = true;
            return candles;
        } catch (IOException | InterruptedException ex) {
            lastCallSucceeded = false;
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to fetch historical candles from Yahoo Finance for {}: {}", instrumentKey, ex.getMessage());
            throw new MarketDataException("Yahoo Finance request failed for " + yahooSymbol + ": " + ex.getClass().getSimpleName());
        }
    }

    @Override
    public List<Candle> getLatestCandles(String instrumentKey, Timeframe timeframe, int limit) {
        if (limit < 1 || limit > 10000) throw new com.stock.marketscreener.exception.InvalidAnalysisRequestException("Candle limit must be 1–10000");
        LocalDateTime to = LocalDateTime.now(INDIA_ZONE);
        LocalDateTime from = switch (timeframe) {
            case ONE_MINUTE -> to.minusDays(5);
            case FIVE_MINUTE, FIFTEEN_MINUTE -> to.minusDays(30);
            case ONE_HOUR -> to.minusDays(55);
            case ONE_DAY -> to.minusYears(2);
            case ONE_WEEK -> to.minusYears(5);
        };
        List<Candle> candles = getHistoricalCandles(instrumentKey, timeframe, from, to);
        if (candles.size() <= limit) {
            return candles;
        }
        return candles.subList(candles.size() - limit, candles.size());
    }

    @Override
    public void subscribe(Set<String> instrumentKeys, Consumer<Tick> tickConsumer) {
        log.info("Yahoo Finance client does not support push subscriptions; use polling via getLatestCandles instead.");
    }

    @Override
    public void unsubscribe(Set<String> instrumentKeys) {
        // No-op: push subscriptions are not supported by this provider.
    }

    @Override
    public boolean isConnected() {
        return lastCallSucceeded;
    }

    List<Candle> parseChartResponse(String body, String instrumentKey, Timeframe timeframe) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        if (root == null) throw new MarketDataException("Yahoo Finance response is empty");
        if (!root.path("chart").path("error").isMissingNode() && !root.path("chart").path("error").isNull()) {
            throw new MarketDataException("Yahoo Finance chart error: " + root.path("chart").path("error").path("code").asText("unknown"));
        }
        JsonNode resultArray = root.path("chart").path("result");
        if (!resultArray.isArray() || resultArray.isEmpty()) {
            throw new MarketDataException("Yahoo Finance response is missing chart results");
        }

        JsonNode result = resultArray.get(0);
        JsonNode timestamps = result.path("timestamp");
        JsonNode quote = result.path("indicators").path("quote");
        if (!quote.isArray() || quote.isEmpty() || !timestamps.isArray()) {
            throw new MarketDataException("Yahoo Finance response is missing timestamps or quotes");
        }
        JsonNode ohlc = quote.get(0);
        JsonNode opens = ohlc.path("open");
        JsonNode highs = ohlc.path("high");
        JsonNode lows = ohlc.path("low");
        JsonNode closes = ohlc.path("close");
        JsonNode volumes = ohlc.path("volume");

        String exchange = "NSE";
        String plainSymbol = instrumentKey.toUpperCase().replace(".NS", "");

        List<Candle> candles = new ArrayList<>();
        int skippedPlaceholderRows = 0;
        for (int i = 0; i < timestamps.size(); i++) {
            JsonNode openNode = opens.get(i);
            JsonNode highNode = highs.get(i);
            JsonNode lowNode = lows.get(i);
            JsonNode closeNode = closes.get(i);
            JsonNode volumeNode = volumes.get(i);

            boolean openMissing = openNode == null || openNode.isNull() || !openNode.isNumber();
            boolean highMissing = highNode == null || highNode.isNull() || !highNode.isNumber();
            boolean lowMissing = lowNode == null || lowNode.isNull() || !lowNode.isNumber();
            boolean closeMissing = closeNode == null || closeNode.isNull() || !closeNode.isNumber();
            boolean volumeMissing = volumeNode == null || volumeNode.isNull()
                || !volumeNode.isIntegralNumber() || !volumeNode.canConvertToLong();

            // Yahoo Finance intraday charts commonly emit fully-null placeholder rows for
            // non-trading intervals (e.g. pre-market slots, holidays within the requested
            // range). These are benign and safe to skip. We only fail closed when a row has
            // a PARTIAL mix of present/missing fields, which indicates genuinely malformed data.
            if (openMissing && highMissing && lowMissing && closeMissing && volumeMissing) {
                skippedPlaceholderRows++;
                continue;
            }
            if (openMissing || highMissing || lowMissing || closeMissing || volumeMissing
                || !timestamps.get(i).isIntegralNumber() || !timestamps.get(i).canConvertToLong()) {
                throw new MarketDataException("Yahoo Finance response contains missing or invalid OHLC/volume");
            }

            long epochSeconds = timestamps.get(i).asLong();
            LocalDateTime candleTime;
            try {
                candleTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), INDIA_ZONE);
            } catch (java.time.DateTimeException ex) {
                throw new MarketDataException("Yahoo Finance returned an invalid candle timestamp");
            }

            BigDecimal open = openNode.decimalValue();
            BigDecimal high = highNode.decimalValue();
            BigDecimal low = lowNode.decimalValue();
            BigDecimal close = closeNode.decimalValue();
            long volume = volumeNode.longValue();
            if (low.signum() <= 0 || volume < 0 || high.compareTo(open.max(close)) < 0 || low.compareTo(open.min(close)) > 0) {
                throw new MarketDataException("Yahoo Finance response contains inconsistent OHLC/volume");
            }

            candles.add(new Candle(null, plainSymbol, exchange, timeframe, candleTime,
                open, high, low, close, volume, 0L, DATA_SOURCE, false));
        }
        if (skippedPlaceholderRows > 0) {
            log.debug("Skipped {} fully-null placeholder row(s) in Yahoo Finance {} response for {}",
                skippedPlaceholderRows, timeframe, instrumentKey);
        }
        return candles;
    }

    private String toYahooSymbol(String instrumentKey) {
        return SymbolValidator.normalize(instrumentKey) + ".NS";
    }

    private String toYahooInterval(Timeframe timeframe) {
        return switch (timeframe) {
            case ONE_MINUTE -> "1m";
            case FIVE_MINUTE -> "5m";
            case FIFTEEN_MINUTE -> "15m";
            case ONE_HOUR -> "60m";
            case ONE_DAY -> "1d";
            case ONE_WEEK -> "1wk";
        };
    }
}
