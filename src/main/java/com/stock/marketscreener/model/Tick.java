package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Tick {
    private Long instrumentId;
    private String symbol;
    private LocalDateTime timestamp;
    private BigDecimal lastPrice;
    private Long lastQuantity;
    private Long totalVolume;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Long bidQuantity;
    private Long askQuantity;

    public Tick() {}

    public Tick(Long instrumentId, String symbol, LocalDateTime timestamp, BigDecimal lastPrice, Long lastQuantity,
                Long totalVolume, BigDecimal bidPrice, BigDecimal askPrice, Long bidQuantity, Long askQuantity) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.lastPrice = lastPrice;
        this.lastQuantity = lastQuantity;
        this.totalVolume = totalVolume;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.bidQuantity = bidQuantity;
        this.askQuantity = askQuantity;
    }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    public Long getLastQuantity() { return lastQuantity; }
    public void setLastQuantity(Long lastQuantity) { this.lastQuantity = lastQuantity; }
    public Long getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Long totalVolume) { this.totalVolume = totalVolume; }
    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }
    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }
    public Long getBidQuantity() { return bidQuantity; }
    public void setBidQuantity(Long bidQuantity) { this.bidQuantity = bidQuantity; }
    public Long getAskQuantity() { return askQuantity; }
    public void setAskQuantity(Long askQuantity) { this.askQuantity = askQuantity; }
}
