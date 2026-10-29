package com.stock.marketscreener.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "instruments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_instrument_exchange_symbol", columnNames = {"exchange", "symbol"}),
        @UniqueConstraint(name = "uk_instrument_key", columnNames = {"instrument_key"})
    },
    indexes = {@Index(name = "idx_instrument_active", columnList = "active, tradable")})
public class InstrumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String exchange;

    @Column(name = "instrument_key", nullable = false, length = 150, unique = true)
    private String instrumentKey;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(length = 20)
    private String isin;

    @Column(length = 100)
    private String sector;

    @Column(length = 150)
    private String industry;

    @Column(name = "instrument_type", nullable = false, length = 30)
    private String instrumentType;

    @Column(length = 10)
    private String currency = "INR";

    @Column(name = "lot_size")
    private Integer lotSize;

    @Column(name = "tick_size", precision = 19, scale = 6)
    private BigDecimal tickSize;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean tradable = true;

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
    public String getInstrumentKey() { return instrumentKey; }
    public void setInstrumentKey(String instrumentKey) { this.instrumentKey = instrumentKey; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getLotSize() { return lotSize; }
    public void setLotSize(Integer lotSize) { this.lotSize = lotSize; }
    public BigDecimal getTickSize() { return tickSize; }
    public void setTickSize(BigDecimal tickSize) { this.tickSize = tickSize; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isTradable() { return tradable; }
    public void setTradable(boolean tradable) { this.tradable = tradable; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
