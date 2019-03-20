package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import java.util.ArrayList;
import java.util.List;

public class ChartDateLabelsValuesComputer implements Function<ChartRange<Long>, List<Long>> {
    private static final int SUBDIVISIONS = 6;

    private List<Long> valuesCache = new ArrayList<>();

    @NonNull
    @Override
    public List<Long> apply(@NonNull ChartRange<Long> range) {
        long step = (range.getTo() - range.getFrom()) / SUBDIVISIONS;

        valuesCache.clear();
        long value = range.getFrom() + step / 2L;

        for (int index = 0; index < SUBDIVISIONS; index++, value += step) {
            valuesCache.add(value);
        }

        return valuesCache;
    }
}
