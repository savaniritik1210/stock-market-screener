package com.stock.marketscreener.analysis;

import com.stock.marketscreener.model.PatternResult;
import com.stock.marketscreener.model.SetupType;
import com.stock.marketscreener.model.TradeDirection;

import java.util.List;

public class SetupDetector {
    public SetupType detect(List<PatternResult> patterns, TradeDirection direction) {
        if (patterns == null || patterns.isEmpty()) return SetupType.NO_SETUP;
        for (PatternResult pattern : patterns) {
            if (pattern.getDirection() == direction && pattern.getPatternType().name().contains("VWAP")) return SetupType.VWAP_BREAKOUT;
            if (pattern.getDirection() == direction && pattern.getPatternType().name().contains("ORB")) return SetupType.ORB_BREAKOUT;
            if (pattern.getPatternType().name().contains("GAP")) return SetupType.GAP_BREAKOUT;
            if (pattern.getPatternType().name().contains("VOLUME")) return SetupType.VOLUME_BREAKOUT;
            if (pattern.getPatternType().name().contains("FLAG")) return SetupType.BULL_FLAG;
        }
        return SetupType.NO_SETUP;
    }
}
