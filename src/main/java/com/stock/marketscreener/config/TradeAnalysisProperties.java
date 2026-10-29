package com.stock.marketscreener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.analysis")
public class TradeAnalysisProperties {
    private int minSamples = 30;
    private int cacheSeconds = 60;
    private int cacheSize = 100;
    private int callsPerMinute = 30;
    private BigDecimal slippageBps = new BigDecimal("5");
    private BigDecimal feeBps = new BigDecimal("10");
    private BigDecimal atrMultiple = new BigDecimal("1.5");
    private BigDecimal targetR = new BigDecimal("1.5");
    private BigDecimal maxIntradayRiskPct = new BigDecimal("3");
    private BigDecimal maxSwingRiskPct = new BigDecimal("8");

    public int getMinSamples() { return minSamples; }
    public void setMinSamples(int value) { minSamples = Math.max(30, value); }
    public int getCacheSeconds() { return cacheSeconds; }
    public void setCacheSeconds(int value) { cacheSeconds = Math.max(1, Math.min(300, value)); }
    public int getCacheSize() { return cacheSize; }
    public void setCacheSize(int value) { cacheSize = Math.max(1, Math.min(1000, value)); }
    public int getCallsPerMinute() { return callsPerMinute; }
    public void setCallsPerMinute(int value) { callsPerMinute = Math.max(1, Math.min(120, value)); }
    public BigDecimal getSlippageBps() { return slippageBps; }
    public void setSlippageBps(BigDecimal value) { slippageBps = bounded(value, "0", "100"); }
    public BigDecimal getFeeBps() { return feeBps; }
    public void setFeeBps(BigDecimal value) { feeBps = bounded(value, "0", "100"); }
    public BigDecimal getAtrMultiple() { return atrMultiple; }
    public void setAtrMultiple(BigDecimal value) { atrMultiple = bounded(value, "1", "5"); }
    public BigDecimal getTargetR() { return targetR; }
    public void setTargetR(BigDecimal value) { targetR = bounded(value, "1", "5"); }
    public BigDecimal getMaxIntradayRiskPct() { return maxIntradayRiskPct; }
    public void setMaxIntradayRiskPct(BigDecimal value) { maxIntradayRiskPct = bounded(value, "0.2", "10"); }
    public BigDecimal getMaxSwingRiskPct() { return maxSwingRiskPct; }
    public void setMaxSwingRiskPct(BigDecimal value) { maxSwingRiskPct = bounded(value, "0.2", "20"); }
    private BigDecimal bounded(BigDecimal value, String min, String max) {
        if (value == null || value.compareTo(new BigDecimal(min)) < 0 || value.compareTo(new BigDecimal(max)) > 0) {
            throw new IllegalArgumentException("Analysis risk parameter outside supported range");
        }
        return value;
    }
}
