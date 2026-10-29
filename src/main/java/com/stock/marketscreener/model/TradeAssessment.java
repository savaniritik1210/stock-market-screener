package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.util.List;

public record TradeAssessment(TradeMode mode, String timeframe, String holdingPeriod, StockSignal signal,
                              String dataSource, boolean isLive, BigDecimal currentPrice, String asOf,
                              boolean entryEligible, String dataWarning, TradeEvidence evidence,
                              List<Candle> candles) {}
