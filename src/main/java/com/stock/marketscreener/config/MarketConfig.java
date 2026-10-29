package com.stock.marketscreener.config;

import com.stock.marketscreener.marketdata.MarketDataClient;
import com.stock.marketscreener.marketdata.MockMarketDataClient;
import com.stock.marketscreener.marketdata.YahooFinanceMarketDataClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MarketConfig {

    @Bean
    public MarketDataClient marketDataClient(@Value("${app.market-data.mock-enabled:false}") boolean mockEnabled,
                                             TradeAnalysisProperties properties) {
        return mockEnabled ? new MockMarketDataClient() : new YahooFinanceMarketDataClient(properties.getCallsPerMinute());
    }
}
