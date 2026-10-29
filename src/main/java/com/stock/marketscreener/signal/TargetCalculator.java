package com.stock.marketscreener.signal;

import java.math.BigDecimal;

public class TargetCalculator {
    public BigDecimal target1(BigDecimal entry, BigDecimal rr) {
        return entry.add(entry.multiply(rr).multiply(BigDecimal.valueOf(0.75)));
    }
    public BigDecimal target2(BigDecimal entry, BigDecimal rr) {
        return entry.add(entry.multiply(rr));
    }
    public BigDecimal target3(BigDecimal entry, BigDecimal rr) {
        return entry.add(entry.multiply(rr).multiply(BigDecimal.valueOf(1.5)));
    }
}
