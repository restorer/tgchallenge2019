package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;

public class ChartLongRangeSnapper implements Consumer<ChartRange<Long>> {
    private long snap;
    private long offset;

    public ChartLongRangeSnapper(long snap, long offset) {
        this.snap = snap;
        this.offset = offset;
    }

    @Override
    public void accept(@NonNull ChartRange<Long> range) {
        range.setRange(snapLower(range.getFrom() - offset, snap), snapUpper(range.getTo() + offset, snap));
    }

    private static long snapLower(long value, long snap) {
        return ((value - snap + 1L) / snap) * snap;
    }

    private static long snapUpper(long value, long snap) {
        return ((value + snap - 1L) / snap) * snap;
    }
}
