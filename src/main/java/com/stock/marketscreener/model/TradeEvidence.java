package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.util.List;

public record TradeEvidence(String status, BigDecimal successRate, BigDecimal confidenceLow,
                            BigDecimal confidenceHigh, int sampleSize, int wins, int losses, int timeouts,
                            BigDecimal netExpectancyR, BigDecimal profitFactor, String from, String to,
                            String method, List<String> warnings) {}
