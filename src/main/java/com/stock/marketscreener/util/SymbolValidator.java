package com.stock.marketscreener.util;

import com.stock.marketscreener.exception.InvalidAnalysisRequestException;
import java.util.Locale;

public final class SymbolValidator {
    private SymbolValidator() {}
    public static String normalize(String input) {
        if (input == null) throw new InvalidAnalysisRequestException("An NSE equity symbol is required");
        String symbol = input.trim().toUpperCase(Locale.ROOT);
        if (symbol.endsWith(".NS")) symbol = symbol.substring(0, symbol.length() - 3);
        if (!symbol.matches("[A-Z0-9][A-Z0-9&-]{0,29}")) {
            throw new InvalidAnalysisRequestException("Use an NSE equity symbol, for example RELIANCE");
        }
        return symbol;
    }
}
