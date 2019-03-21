package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;

public class ChartIntRangeSnapper implements Consumer<ChartRange<Integer>> {
    private static final int SUBDIVISIONS = 6;

    private int snap;
    private int offset;

    public ChartIntRangeSnapper(int snap, int offset) {
        this.snap = snap;
        this.offset = offset;
    }

    @Override
    public void accept(@NonNull ChartRange<Integer> range) {
        int lower = snapLower(range.getFrom() - offset, snap);
        int last = snapUpper(lower + (range.getTo() + offset - lower) / SUBDIVISIONS * (SUBDIVISIONS - 1), snap);
        int upper = lower + (last - lower) / (SUBDIVISIONS - 1) * SUBDIVISIONS;

        range.setRange(lower, upper);
    }

    private static int snapLower(int value, int snap) {
        return (value / snap) * snap;
    }

    private static int snapUpper(int value, int snap) {
        return ((value + snap - 1) / snap) * snap;
    }
}
