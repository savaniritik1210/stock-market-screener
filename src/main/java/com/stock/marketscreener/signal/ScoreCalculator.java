package com.stock.marketscreener.signal;

public class ScoreCalculator {
    public int calculate(int trend, int structure, int pattern, int volume, int vwap, int momentum, int higherTf, int rr) {
        return Math.min(100, trend + structure + pattern + volume + vwap + momentum + higherTf + rr);
    }
}
