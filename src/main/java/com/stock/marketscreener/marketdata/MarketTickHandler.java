package com.stock.marketscreener.marketdata;

import com.stock.marketscreener.model.Tick;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MarketTickHandler {
    private final List<Tick> tickCache = new CopyOnWriteArrayList<>();

    public void handle(Tick tick) {
        if (tick != null) {
            tickCache.add(tick);
        }
    }

    public List<Tick> getRecentTicks() {
        return new ArrayList<>(tickCache);
    }
}
