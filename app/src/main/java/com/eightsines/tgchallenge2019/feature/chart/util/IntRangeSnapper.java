package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;

public class IntRangeSnapper implements Function<ChartRange<Integer>, ChartRange<Integer>> {
    private int snap;
    private int offset;

    public IntRangeSnapper(int snap) {
        this(snap, snap);
    }

    public IntRangeSnapper(int snap, int offset) {
        this.snap = snap;
        this.offset = offset;
    }

    @NonNull
    @Override
    public ChartRange<Integer> apply(@Nullable ChartRange<Integer> range) {
        if (range == null) {
            return new ChartRange<>(-snap, snap);
        }

        range.setFrom(snapLower(range.getFrom() - offset, snap));
        range.setTo(snapUpper(range.getTo() + offset, snap));

        return range;
    }

    private static int snapLower(int value, int snap) {
        return ((value - snap + 1) / snap) * snap;
    }

    private static int snapUpper(int value, int snap) {
        return ((value + snap - 1) / snap) * snap;
    }
}
