package com.stock.marketscreener.indicator;

import com.stock.marketscreener.model.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class MacdCalculator {
    public BigDecimal calculate(List<Candle> candles, int fast, int slow) {
        if (candles == null || candles.size() < slow) return null;
        EmaCalculator ema = new EmaCalculator();
        BigDecimal fastEma = ema.calculate(candles, fast);
        BigDecimal slowEma = ema.calculate(candles, slow);
        if (fastEma == null || slowEma == null) return null;
        return fastEma.subtract(slowEma).setScale(4, RoundingMode.HALF_UP);
    }
}
