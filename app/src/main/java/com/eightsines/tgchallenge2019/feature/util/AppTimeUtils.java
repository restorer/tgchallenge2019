package com.eightsines.tgchallenge2019.feature.util;

public final class AppTimeUtils {
    public static final long SECOND_MS = 1000L;
    public static final long MINUTE_MS = SECOND_MS * 60L;
    public static final long HOUR_MS = MINUTE_MS * 60L;
    public static final long DAY_MS = HOUR_MS * 24L;
    public static final long WEEK_MS = DAY_MS * 7;

    private AppTimeUtils() {
    }
}
