package com.stock.marketscreener;

import com.stock.marketscreener.model.Candle;
import com.stock.marketscreener.model.SignalReason;
import com.stock.marketscreener.model.StockSignal;
import com.stock.marketscreener.model.Timeframe;
import com.stock.marketscreener.service.MarketDataService;
import com.stock.marketscreener.service.SignalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive terminal prompt for ad-hoc stock analysis. Disabled by default so the
 * app runs as a normal REST API (for the Angular frontend). Enable with:
 *   --app.console.enabled=true   (or APP_CONSOLE_ENABLED=true)
 */
@Component
public class StockConsoleRunner implements CommandLineRunner {

    private final MarketDataService marketDataService;
    private final SignalService signalService;

    @Value("${app.console.enabled:false}")
    private boolean consoleEnabled;

    public StockConsoleRunner(MarketDataService marketDataService, SignalService signalService) {
        this.marketDataService = marketDataService;
        this.signalService = signalService;
    }

    @Override
    public void run(String... args) {
        if (!consoleEnabled) {
            return;
        }

        System.out.println("\n=== Stock Market Screener Console ===");
        System.out.println("Type the stock symbol and press Enter. Type EXIT to stop.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (!scanner.hasNextLine()) {
                    return;
                }

                System.out.print("Enter stock symbol: ");
                String input = scanner.nextLine();
                if (input == null) {
                    continue;
                }

                String symbol = input.trim();
                if (symbol.isEmpty()) {
                    continue;
                }
                if (symbol.equalsIgnoreCase("EXIT") || symbol.equalsIgnoreCase("QUIT")) {
                    System.out.println("Goodbye!");
                    return;
                }

                printAnalysis(symbol);
            }
        }
    }

    private void printAnalysis(String symbol) {
        try {
            String upperSymbol = symbol.toUpperCase();
            List<Candle> candles = marketDataService.getHistoricalCandles(
                upperSymbol,
                Timeframe.FIVE_MINUTE,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
            );

            if (candles == null || candles.isEmpty()) {
                System.out.println("\nNo market data available for " + upperSymbol + ".");
                return;
            }

            StockSignal signal = signalService.analyze(upperSymbol, candles);
            Candle last = candles.get(candles.size() - 1);

            System.out.println("\n" + repeat("=", 72));
            System.out.println("Stock: " + upperSymbol);
            System.out.println("Data Source: " + safe(last.getDataSource()) + (last.isLive() ? " (real-time)" : " (delayed/simulated)"));
            System.out.println("Last Candle Time: " + safe(last.getTimestamp()));
            System.out.println("Last Price: " + fmt(last.getClose()));
            System.out.println("Signal Type: " + safe(signal.getSignalType()));
            System.out.println("Direction: " + safe(signal.getDirection()));
            System.out.println("Setup: " + safe(signal.getSetup()));
            System.out.println("Score: " + safe(signal.getScore()));
            System.out.println("Confidence: " + safe(signal.getConfidence()));
            System.out.println("Summary: " + safe(signal.getSummary()));
            System.out.println("Entry: " + fmt(signal.getEntry()));
            System.out.println("Stop Loss: " + fmt(signal.getStopLoss()));
            System.out.println("Target 1: " + fmt(signal.getTarget1()));
            System.out.println("Target 2: " + fmt(signal.getTarget2()));
            System.out.println("Target 3: " + fmt(signal.getTarget3()));
            System.out.println("Risk/Reward: " + fmt(signal.getRiskReward()));

            if (signal.getReasons() != null && !signal.getReasons().isEmpty()) {
                System.out.println("\nReasons:");
                int index = 1;
                for (SignalReason reason : signal.getReasons()) {
                    System.out.println("  " + index + ". " + reason.getDescription());
                    index++;
                }
            }

            System.out.println(repeat("=", 72) + "\n");
        } catch (Exception ex) {
            System.out.println("\nError while analyzing " + symbol + ": " + ex.getMessage());
        }
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private String fmt(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String repeat(String s, int count) {
        return String.valueOf(s).repeat(Math.max(0, count));
    }
}
