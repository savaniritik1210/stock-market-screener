package com.stock.marketscreener.model;

public record TradeComparison(String symbol, String generatedAt, TradeAssessment intraday,
                              TradeAssessment swing, String preferredMode, String comparisonReason) {}
