package com.eightsines.tgchallenge2019.feature.chart.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import com.eightsines.tgchallenge2019.feature.chart.data.ChartRange;
import java.util.ArrayList;
import java.util.List;

public class ChartIntLabelsValuesComputer implements Function<ChartRange<Integer>, List<Integer>> {
    private static final int SUBDIVISIONS = 6;

    private List<Integer> valuesCache = new ArrayList<>();

    @NonNull
    @Override
    public List<Integer> apply(@NonNull ChartRange<Integer> range) {
        int step = (range.getTo() - range.getFrom()) / SUBDIVISIONS;
        valuesCache.clear();

        for (int index = 0, value = range.getFrom(); index < SUBDIVISIONS; index++, value += step) {
            valuesCache.add(value);
        }

        return valuesCache;
    }
}
