package com.stock.marketscreener.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "signals",
    indexes = {@Index(name = "idx_signal_symbol_time", columnList = "symbol, created_at")})
public class SignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String symbol;

    @Column(length = 20)
    private String exchange = "NSE";

    @Column(name = "signal_type", nullable = false, length = 30)
    private String signalType;

    @Column(nullable = false, length = 20)
    private String direction;

    @Column(length = 50)
    private String setup;

    @Column(precision = 19, scale = 4)
    private BigDecimal entryPrice;

    @Column(name = "stop_loss", precision = 19, scale = 4)
    private BigDecimal stopLoss;

    @Column(name = "target_1", precision = 19, scale = 4)
    private BigDecimal target1;

    @Column(name = "target_2", precision = 19, scale = 4)
    private BigDecimal target2;

    @Column(name = "target_3", precision = 19, scale = 4)
    private BigDecimal target3;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getSetup() { return setup; }
    public void setSetup(String setup) { this.setup = setup; }
    public BigDecimal getEntryPrice() { return entryPrice; }
    public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
    public BigDecimal getStopLoss() { return stopLoss; }
    public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
    public BigDecimal getTarget1() { return target1; }
    public void setTarget1(BigDecimal target1) { this.target1 = target1; }
    public BigDecimal getTarget2() { return target2; }
    public void setTarget2(BigDecimal target2) { this.target2 = target2; }
    public BigDecimal getTarget3() { return target3; }
    public void setTarget3(BigDecimal target3) { this.target3 = target3; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}