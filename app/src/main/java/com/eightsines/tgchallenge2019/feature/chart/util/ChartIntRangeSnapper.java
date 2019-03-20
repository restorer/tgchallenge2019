package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;

public class ChartIntRangeSnapper implements Consumer<ChartRange<Integer>> {
    private int snap;
    private int offset;

    public ChartIntRangeSnapper(int snap) {
        this(snap, snap);
    }

    public ChartIntRangeSnapper(int snap, int offset) {
        this.snap = snap;
        this.offset = offset;
    }

    @Override
    public void accept(@NonNull ChartRange<Integer> range) {
        range.setRange(snapLower(range.getFrom() - offset, snap),
                snapUpper(range.getTo() + offset, snap));
    }

    private static int snapLower(int value, int snap) {
        return ((value - snap + 1) / snap) * snap;
    }

    private static int snapUpper(int value, int snap) {
        return ((value + snap - 1) / snap) * snap;
    }
}
