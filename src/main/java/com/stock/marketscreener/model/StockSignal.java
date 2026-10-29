package com.stock.marketscreener.model;

import java.math.BigDecimal;
import java.util.List;

public class StockSignal {
    private String symbol;
    private String instrument;
    private SignalType signalType;
    private TradeDirection direction;
    private SetupType setup;
    private BigDecimal entry;
    private BigDecimal stopLoss;
    private BigDecimal target1;
    private BigDecimal target2;
    private BigDecimal target3;
    private BigDecimal riskReward;
    private int score;
    private int confidence;
    private String summary;
    private List<SignalReason> reasons;
    private List<TimeframeAnalysis> timeframeAnalysis;
    private String dataFreshness;

    public StockSignal() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
    public SignalType getSignalType() { return signalType; }
    public void setSignalType(SignalType signalType) { this.signalType = signalType; }
    public TradeDirection getDirection() { return direction; }
    public void setDirection(TradeDirection direction) { this.direction = direction; }
    public SetupType getSetup() { return setup; }
    public void setSetup(SetupType setup) { this.setup = setup; }
    public BigDecimal getEntry() { return entry; }
    public void setEntry(BigDecimal entry) { this.entry = entry; }
    public BigDecimal getStopLoss() { return stopLoss; }
    public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
    public BigDecimal getTarget1() { return target1; }
    public void setTarget1(BigDecimal target1) { this.target1 = target1; }
    public BigDecimal getTarget2() { return target2; }
    public void setTarget2(BigDecimal target2) { this.target2 = target2; }
    public BigDecimal getTarget3() { return target3; }
    public void setTarget3(BigDecimal target3) { this.target3 = target3; }
    public BigDecimal getRiskReward() { return riskReward; }
    public void setRiskReward(BigDecimal riskReward) { this.riskReward = riskReward; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<SignalReason> getReasons() { return reasons; }
    public void setReasons(List<SignalReason> reasons) { this.reasons = reasons; }
    public List<TimeframeAnalysis> getTimeframeAnalysis() { return timeframeAnalysis; }
    public void setTimeframeAnalysis(List<TimeframeAnalysis> timeframeAnalysis) { this.timeframeAnalysis = timeframeAnalysis; }
    public String getDataFreshness() { return dataFreshness; }
    public void setDataFreshness(String dataFreshness) { this.dataFreshness = dataFreshness; }
}
