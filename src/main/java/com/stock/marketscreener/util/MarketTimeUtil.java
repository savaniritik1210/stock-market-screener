package com.stock.marketscreener.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class MarketTimeUtil {
    public static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private MarketTimeUtil() {}

    public static boolean isMarketOpen(LocalDateTime time) {
        if (time == null) return false;
        LocalDateTime local = time.atZone(INDIA_ZONE).toLocalDateTime();
        DayOfWeek day = local.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;
        LocalTime open = LocalTime.of(9, 15);
        LocalTime close = LocalTime.of(15, 30);
        LocalTime current = local.toLocalTime();
        return !current.isBefore(open) && !current.isAfter(close);
    }

    public static boolean isWeekend(LocalDateTime time) {
        if (time == null) return false;
        DayOfWeek day = time.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
