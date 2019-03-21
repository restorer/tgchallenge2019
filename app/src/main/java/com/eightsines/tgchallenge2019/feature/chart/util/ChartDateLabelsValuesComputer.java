package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import com.eightsines.tgchallenge2019.feature.util.AppTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class ChartDateLabelsValuesComputer implements Function<ChartRange<Long>, List<Long>> {
    private static final int SUBDIVISIONS = 3;

    private long step = AppTimeUtils.DAY_MS;
    private List<Long> valuesCache = new ArrayList<>();

    long getStep() {
        return step;
    }

    @SuppressWarnings("MagicNumber")
    @NonNull
    @Override
    public List<Long> apply(@NonNull ChartRange<Long> range) {
        step = Math.max(1L, Long.highestOneBit((range.getTo() - range.getFrom()) / AppTimeUtils.DAY_MS / SUBDIVISIONS))
                * AppTimeUtils.DAY_MS;

        valuesCache.clear();
        long value = (range.getFrom() / step) * step;

        while (value < range.getTo() + step) {
            valuesCache.add(value);
            value += step;
        }

        return valuesCache;
    }
}
