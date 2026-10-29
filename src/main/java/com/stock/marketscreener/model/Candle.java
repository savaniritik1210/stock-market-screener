package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Candle {
    private Long instrumentId;
    private String symbol;
    private String exchange;
    private Timeframe timeframe;
    private LocalDateTime timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private Long openInterest;
    private String dataSource = "UNKNOWN";
    private boolean live = false;

    public Candle() {}

    public Candle(Long instrumentId, String symbol, String exchange, Timeframe timeframe, LocalDateTime timestamp,
                 BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume, Long openInterest) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.timeframe = timeframe;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.openInterest = openInterest;
    }

    public Candle(Long instrumentId, String symbol, String exchange, Timeframe timeframe, LocalDateTime timestamp,
                 BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume, Long openInterest,
                 String dataSource, boolean live) {
        this(instrumentId, symbol, exchange, timeframe, timestamp, open, high, low, close, volume, openInterest);
        this.dataSource = dataSource;
        this.live = live;
    }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public Timeframe getTimeframe() { return timeframe; }
    public void setTimeframe(Timeframe timeframe) { this.timeframe = timeframe; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public Long getOpenInterest() { return openInterest; }
    public void setOpenInterest(Long openInterest) { this.openInterest = openInterest; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }
}

