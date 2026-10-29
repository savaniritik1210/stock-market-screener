package com.stock.marketscreener.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks", uniqueConstraints = {@UniqueConstraint(name = "uk_stock_symbol_exchange", columnNames = {"symbol", "exchange"})})
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String symbol;

    @Column(nullable = false, length = 20)
    private String exchange;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
