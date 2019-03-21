package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartDateLabelsValuesComputer implements Function<ChartRange<Long>, List<Long>> {
    private static final long SUBDIVISIONS = AppTimeUtils.DAY_MS * 35L / 10L;

    private ChartRange<Long> fullRange;
    private long step = AppTimeUtils.DAY_MS;
    private List<Long> valuesCache = new ArrayList<>();

    public ChartDateLabelsValuesComputer(ChartRange<Long> fullRange) {
        this.fullRange = fullRange;
    }

    long getStep() {
        return step;
    }

    @SuppressWarnings("MagicNumber")
    @NonNull
    @Override
    public List<Long> apply(@NonNull ChartRange<Long> range) {
        step = Math.max(1L, Long.highestOneBit((range.getTo() - range.getFrom()) / SUBDIVISIONS)) * AppTimeUtils.DAY_MS;

        valuesCache.clear();
        long value = fullRange.getTo() - ((fullRange.getTo() - range.getTo()) / step) * step;

        while (value >= range.getFrom()) {
            valuesCache.add(value);
            value -= step;
        }

        return valuesCache;
    }
}
