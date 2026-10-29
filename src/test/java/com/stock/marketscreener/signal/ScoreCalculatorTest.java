package com.stock.marketscreener.signal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {
    @Test
    void calculatesScore() {
        assertEquals(80, new ScoreCalculator().calculate(20, 15, 15, 10, 10, 5, 5, 0));
    }
}
