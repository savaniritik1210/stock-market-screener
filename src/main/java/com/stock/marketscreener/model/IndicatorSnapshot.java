package com.stock.marketscreener.model;

import java.math.BigDecimal;

public class IndicatorSnapshot {
    private BigDecimal ema9;
    private BigDecimal ema20;
    private BigDecimal ema50;
    private BigDecimal ema100;
    private BigDecimal ema200;
    private BigDecimal sma20;
    private BigDecimal sma50;
    private BigDecimal sma200;
    private BigDecimal vwap;
    private BigDecimal rsi14;
    private BigDecimal macd;
    private BigDecimal macdSignal;
    private BigDecimal macdHistogram;
    private BigDecimal atr14;
    private BigDecimal adx14;
    private BigDecimal plusDi;
    private BigDecimal minusDi;
    private BigDecimal bollingerUpper;
    private BigDecimal bollingerMiddle;
    private BigDecimal bollingerLower;
    private BigDecimal volumeSma20;
    private BigDecimal relativeVolume;
    private BigDecimal obv;

    public BigDecimal getEma9() { return ema9; }
    public void setEma9(BigDecimal ema9) { this.ema9 = ema9; }
    public BigDecimal getEma20() { return ema20; }
    public void setEma20(BigDecimal ema20) { this.ema20 = ema20; }
    public BigDecimal getEma50() { return ema50; }
    public void setEma50(BigDecimal ema50) { this.ema50 = ema50; }
    public BigDecimal getEma100() { return ema100; }
    public void setEma100(BigDecimal ema100) { this.ema100 = ema100; }
    public BigDecimal getEma200() { return ema200; }
    public void setEma200(BigDecimal ema200) { this.ema200 = ema200; }
    public BigDecimal getSma20() { return sma20; }
    public void setSma20(BigDecimal sma20) { this.sma20 = sma20; }
    public BigDecimal getSma50() { return sma50; }
    public void setSma50(BigDecimal sma50) { this.sma50 = sma50; }
    public BigDecimal getSma200() { return sma200; }
    public void setSma200(BigDecimal sma200) { this.sma200 = sma200; }
    public BigDecimal getVwap() { return vwap; }
    public void setVwap(BigDecimal vwap) { this.vwap = vwap; }
    public BigDecimal getRsi14() { return rsi14; }
    public void setRsi14(BigDecimal rsi14) { this.rsi14 = rsi14; }
    public BigDecimal getMacd() { return macd; }
    public void setMacd(BigDecimal macd) { this.macd = macd; }
    public BigDecimal getMacdSignal() { return macdSignal; }
    public void setMacdSignal(BigDecimal macdSignal) { this.macdSignal = macdSignal; }
    public BigDecimal getMacdHistogram() { return macdHistogram; }
    public void setMacdHistogram(BigDecimal macdHistogram) { this.macdHistogram = macdHistogram; }
    public BigDecimal getAtr14() { return atr14; }
    public void setAtr14(BigDecimal atr14) { this.atr14 = atr14; }
    public BigDecimal getAdx14() { return adx14; }
    public void setAdx14(BigDecimal adx14) { this.adx14 = adx14; }
    public BigDecimal getPlusDi() { return plusDi; }
    public void setPlusDi(BigDecimal plusDi) { this.plusDi = plusDi; }
    public BigDecimal getMinusDi() { return minusDi; }
    public void setMinusDi(BigDecimal minusDi) { this.minusDi = minusDi; }
    public BigDecimal getBollingerUpper() { return bollingerUpper; }
    public void setBollingerUpper(BigDecimal bollingerUpper) { this.bollingerUpper = bollingerUpper; }
    public BigDecimal getBollingerMiddle() { return bollingerMiddle; }
    public void setBollingerMiddle(BigDecimal bollingerMiddle) { this.bollingerMiddle = bollingerMiddle; }
    public BigDecimal getBollingerLower() { return bollingerLower; }
    public void setBollingerLower(BigDecimal bollingerLower) { this.bollingerLower = bollingerLower; }
    public BigDecimal getVolumeSma20() { return volumeSma20; }
    public void setVolumeSma20(BigDecimal volumeSma20) { this.volumeSma20 = volumeSma20; }
    public BigDecimal getRelativeVolume() { return relativeVolume; }
    public void setRelativeVolume(BigDecimal relativeVolume) { this.relativeVolume = relativeVolume; }
    public BigDecimal getObv() { return obv; }
    public void setObv(BigDecimal obv) { this.obv = obv; }
}
